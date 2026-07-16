package com.raulrobinson.driven.iam.adapter;

import com.raulrobinson.exception.IamAccessDeniedException;
import com.raulrobinson.exception.IamBadRequestException;
import com.raulrobinson.exception.IamClientException;
import com.raulrobinson.model.IamRole;
import com.raulrobinson.model.IamRolesResult;
import com.raulrobinson.model.IamUser;
import com.raulrobinson.model.IamUsersResult;
import com.raulrobinson.ports.out.IamGateway;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListRolesRequest;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.iam.model.User;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletionException;

@Component
public class IamAdapter implements IamGateway {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Mono<IamUsersResult> listUsers(String accessKeyId, String secretAccessKey,
                                          String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    IamAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllUsers(client, null, new ArrayList<>())
                            .map(users -> users.stream()
                                    .map(this::toModel)
                                    .sorted(Comparator.comparing(u -> u.userName().toLowerCase()))
                                    .toList())
                            .map(IamUsersResult::new)
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    @Override
    public Mono<IamRolesResult> listRoles(String accessKeyId, String secretAccessKey,
                                          String sessionToken, String region) {
        return validate(accessKeyId, secretAccessKey, region)
                .then(Mono.defer(() -> {
                    IamAsyncClient client = buildClient(region, accessKeyId, secretAccessKey, sessionToken);
                    return listAllRoles(client, null, new ArrayList<>())
                            .map(roles -> roles.stream()
                                    .map(this::toModel)
                                    .sorted(Comparator.comparing(r -> r.roleName().toLowerCase()))
                                    .toList())
                            .map(IamRolesResult::new)
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    private Mono<List<User>> listAllUsers(IamAsyncClient client, String marker, List<User> acc) {
        ListUsersRequest.Builder builder = ListUsersRequest.builder().maxItems(1000);
        if (marker != null) builder.marker(marker);

        return Mono.fromFuture(client.listUsers(builder.build()))
                .flatMap(resp -> {
                    acc.addAll(resp.users());
                    if (Boolean.TRUE.equals(resp.isTruncated()) && resp.marker() != null) {
                        return listAllUsers(client, resp.marker(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    private Mono<List<Role>> listAllRoles(IamAsyncClient client, String marker, List<Role> acc) {
        ListRolesRequest.Builder builder = ListRolesRequest.builder().maxItems(1000);
        if (marker != null) builder.marker(marker);

        return Mono.fromFuture(client.listRoles(builder.build()))
                .flatMap(resp -> {
                    acc.addAll(resp.roles());
                    if (Boolean.TRUE.equals(resp.isTruncated()) && resp.marker() != null) {
                        return listAllRoles(client, resp.marker(), acc);
                    }
                    return Mono.just(acc);
                });
    }

    private IamUser toModel(User user) {
        return new IamUser(
                user.userName(),
                user.arn(),
                user.path(),
                user.createDate() != null ? user.createDate().toString() : null
        );
    }

    private IamRole toModel(Role role) {
        IrsaInfo irsa = parseIrsaInfo(role.assumeRolePolicyDocument());
        return new IamRole(
                role.roleName(),
                role.arn(),
                role.path(),
                role.createDate() != null ? role.createDate().toString() : null,
                irsa.irsaEnabled(),
                irsa.oidcProviders()
        );
    }

    private IrsaInfo parseIrsaInfo(String encodedTrustPolicy) {
        if (encodedTrustPolicy == null || encodedTrustPolicy.isBlank()) {
            return new IrsaInfo(false, List.of());
        }

        try {
            String decoded = URLDecoder.decode(encodedTrustPolicy, StandardCharsets.UTF_8);
            JsonNode root = MAPPER.readTree(decoded);
            JsonNode statements = root.path("Statement");

            List<JsonNode> statementList = new ArrayList<>();
            if (statements.isArray()) {
                statements.forEach(statementList::add);
            } else if (statements.isObject()) {
                statementList.add(statements);
            }

            boolean hasWebIdentityAction = false;
            LinkedHashSet<String> oidcProviders = new LinkedHashSet<>();

            for (JsonNode stmt : statementList) {
                if (!containsWebIdentityAssumeAction(stmt.path("Action"))) {
                    continue;
                }
                hasWebIdentityAction = true;

                JsonNode federated = stmt.path("Principal").path("Federated");
                if (federated.isTextual()) {
                    String value = federated.asText();
                    if (value.contains(":oidc-provider/")) oidcProviders.add(value);
                } else if (federated.isArray()) {
                    federated.forEach(node -> {
                        if (node.isTextual()) {
                            String value = node.asText();
                            if (value.contains(":oidc-provider/")) oidcProviders.add(value);
                        }
                    });
                }
            }

            return new IrsaInfo(hasWebIdentityAction && !oidcProviders.isEmpty(), List.copyOf(oidcProviders));
        } catch (Exception ignored) {
            return new IrsaInfo(false, List.of());
        }
    }

    private boolean containsWebIdentityAssumeAction(JsonNode action) {
        if (action == null || action.isMissingNode()) return false;

        if (action.isTextual()) {
            return "sts:AssumeRoleWithWebIdentity".equalsIgnoreCase(action.asText());
        }
        if (action.isArray()) {
            for (JsonNode node : action) {
                if (node.isTextual() && "sts:AssumeRoleWithWebIdentity".equalsIgnoreCase(node.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private IamAsyncClient buildClient(String region, String accessKeyId,
                                       String secretAccessKey, String sessionToken) {
        var credentials = (sessionToken != null && !sessionToken.isBlank())
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return IamAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Mono<Void> validate(String accessKeyId, String secretAccessKey, String region) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new IamBadRequestException("Falta accessKeyId"));
        }
        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new IamBadRequestException("Falta secretAccessKey"));
        }
        if (region == null || region.isBlank()) {
            return Mono.error(new IamBadRequestException("Falta region AWS"));
        }
        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof IamBadRequestException
                || cause instanceof IamClientException
                || cause instanceof IamAccessDeniedException) {
            return cause;
        }

        if (cause instanceof IamException iamEx) {
            if (iamEx.statusCode() == 403) {
                return new IamAccessDeniedException("Acceso denegado en IAM", cause);
            }
            if (iamEx.statusCode() >= 400 && iamEx.statusCode() < 500) {
                return new IamBadRequestException("Solicitud inválida para IAM: " + iamEx.getMessage(), cause);
            }
            return new IamClientException("Error de AWS IAM: " + iamEx.getMessage(), cause);
        }

        if (cause instanceof SdkClientException) {
            return new IamClientException("Error de comunicación con AWS IAM", cause);
        }

        return new IamClientException("Error inesperado consultando AWS IAM", cause);
    }

    private Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        if (error.getCause() instanceof CompletionException ce && ce.getCause() != null) {
            return ce.getCause();
        }
        return error;
    }

    private record IrsaInfo(boolean irsaEnabled, List<String> oidcProviders) {
    }
}
