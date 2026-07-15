package com.raulrobinson;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.raulrobinson",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class ArchitectureTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 1: Domain must not depend on infrastructure packages
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule domain_model_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.raulrobinson.driven..",
                            "com.raulrobinson.api.."
                    )
                    .because("Domain model must be independent of infrastructure concerns");

    @ArchTest
    static final ArchRule domain_ports_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.ports..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.raulrobinson.driven..",
                            "com.raulrobinson.api.."
                    )
                    .because("Domain ports must be independent of infrastructure concerns");

    @ArchTest
    static final ArchRule domain_usecase_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.usecase..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.raulrobinson.driven..",
                            "com.raulrobinson.api.."
                    )
                    .because("Use cases must be independent of infrastructure concerns");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 2: Ports must not depend on use cases
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule ports_must_not_depend_on_usecase =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.ports..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.raulrobinson.usecase..")
                    .because("Ports define contracts and must not depend on use case implementations");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 3: Model must not depend on other layers
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule model_must_not_depend_on_ports =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.raulrobinson.ports..",
                            "com.raulrobinson.usecase..",
                            "com.raulrobinson.driven..",
                            "com.raulrobinson.api.."
                    )
                    .because("Domain model must be the innermost layer with no outward dependencies");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 4: Ports naming — output ports must end with "Gateway"
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule output_ports_must_be_named_gateway =
            classes()
                    .that().resideInAnyPackage("com.raulrobinson.ports.out..")
                    .should().haveSimpleNameEndingWith("Gateway")
                    .because("Output port interfaces must follow the Gateway naming convention");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 5: Use cases naming — classes implementing IRedeemLoyaltyPoints must end with "UseCase"
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule use_case_implementations_must_end_with_UseCase =
            classes()
                    .that().resideInAnyPackage("com.raulrobinson.usecase..")
                    .and().implement("com.raulrobinson.ports.in.IRedeemLoyaltyPoints")
                    .should().haveSimpleNameEndingWith("UseCase")
                    .because("Use case implementations must follow the UseCase naming convention");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 6: Adapters naming — driven-adapter classes implementing Gateway ports
    //         must end with "Adapter" or "Repository"
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule driven_adapters_must_be_named_adapter_or_repository =
            classes()
                    .that().resideInAnyPackage("com.raulrobinson.driven..")
                    .and().areNotInterfaces()
                    .and().areAnnotatedWith("org.springframework.stereotype.Component")
                    .should().haveSimpleNameEndingWith("Adapter")
                    .orShould().haveSimpleNameEndingWith("Repository")
                    .because("Driven adapter Spring @Component implementations must follow the Adapter or Repository naming convention");

    @ArchTest
    static final ArchRule driven_repositories_must_be_named_repository =
            classes()
                    .that().resideInAnyPackage("com.raulrobinson.driven..")
                    .and().areNotInterfaces()
                    .and().areAnnotatedWith("org.springframework.stereotype.Repository")
                    .should().haveSimpleNameEndingWith("Repository")
                    .because("Driven adapter Spring @Repository implementations must follow the Repository naming convention");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 7: Entry-points must not depend on driven-adapters
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule entry_points_must_not_depend_on_driven_adapters =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.api..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.raulrobinson.driven..")
                    .because("Entry-points (API handlers) must not directly depend on driven adapters");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 8: Driven-adapters must not depend on entry-points
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule driven_adapters_must_not_depend_on_entry_points =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.driven..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.raulrobinson.api..")
                    .because("Driven adapters must not depend on entry-point (API) classes");

    // ─────────────────────────────────────────────────────────────────────────
    // Rule 9: Use cases must not have @Service annotation (they use @Component)
    // ─────────────────────────────────────────────────────────────────────────

    @ArchTest
    static final ArchRule use_cases_must_not_use_service_annotation =
            noClasses()
                    .that().resideInAnyPackage("com.raulrobinson.usecase..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Service")
                    .because("Use cases should use @Component, not @Service, to avoid Spring service layer coupling");
}
