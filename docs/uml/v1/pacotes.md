# Diagrama de Pacotes: Visão Lógica (v1)

Cada bloco é um pacote, e as setas pontilhadas são dependências. Os dois blocos externos são os subsistemas pedidos no enunciado. No padrão MVC, o front-end é a **View**, `controller` é o **Controller**, e `service`, `model` e `repository` formam o **Model**.

```mermaid
flowchart TB
    subgraph FE["Subsistema de construção dinâmica das páginas web (React + Vite)"]
        pages["📁 pages"]
        components["📁 components"]
        hooks["📁 hooks"]
        services["📁 services"]
        utils["📁 utils"]
    end

    subgraph BE["Subsistema de gestão de pedidos e contratos (Spring Boot): br.pucminas.aluguelcarros"]
        controller["📁 controller"]
        dto["📁 dto"]
        service["📁 service"]
        model["📁 model"]
        repository["📁 repository"]
        security["📁 security"]
        config["📁 config"]
        exception["📁 exception"]
    end

    DB[("Supabase (PostgreSQL)")]

    pages -.-> components
    pages -.-> hooks
    pages -.-> services
    pages -.-> utils
    components -.-> hooks
    components -.-> utils
    hooks -.-> services
    services -. "HTTP/JSON + JWT" .-> controller

    controller -.-> service
    controller -.-> dto
    controller -.-> model
    controller -.-> security
    service -.-> model
    service -.-> repository
    service -.-> dto
    service -.-> security
    service -.-> exception
    dto -.-> model
    repository -.-> model
    model -.-> exception
    security -.-> model
    config -.-> security
    config -.-> model
    config -.-> repository
    repository -. "JDBC" .-> DB
```
