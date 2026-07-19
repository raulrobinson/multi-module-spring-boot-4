# 

```groovy
GET /api/v1/posts
  → RetrievePostsHandler
    → RetrievePostsUseCase
      → RetrievePostsService
        → PostsGateway
          → JsonPlaceholderPostsAdapter
            → GenericApiClient
              → GET https://jsonplaceholder.typicode.com/posts
```

Solución que mantiene arquitectura hexagonal en donde el caso de uso ordena el consumo, pero la construcción del request HTTP permanece en infraestructura.

