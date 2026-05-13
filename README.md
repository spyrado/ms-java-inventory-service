# ms-java-inventory-service

Microserviço responsável pelo controle de estoque do sistema e-commerce.

## Tecnologias
- Java 21
- Spring Boot 4.0.6
- Spring Data JPA + Hibernate
- PostgreSQL
- Apache Kafka (consumer)
- RabbitMQ (producer)
- Redis (cache)
- Maven

## Pré-requisitos
- Java 21 instalado
- infra-local rodando (`docker-compose up`)

## Como rodar
```bash
./mvnw spring-boot:run
```

## Porta
Roda na porta `8082`

## Fluxo
1. Consome evento `OrderCreatedEvent` do Kafka
2. Verifica e debita estoque no PostgreSQL (com cache Redis)
3. Publica evento `StockUpdatedEvent` no RabbitMQ

## Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | /inventory/{productId} | Consultar estoque de um produto |
| PUT | /inventory/{productId} | Atualizar estoque manualmente |