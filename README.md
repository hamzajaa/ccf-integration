# Consistency Contract Framework (CCF) Integration

## Overview

The **Consistency Contract Framework (CCF)** is a lightweight library designed to enforce consistency contracts in polyglot persistence environments. It enables microservices to define, validate, and enforce distributed consistency guarantees through declarative contracts.

### Key Features

-  **Declarative Contracts**: Define consistency requirements using a Domain-Specific Language (DSL)
-  **Distributed Validation**: Automatic contract validation across multiple services via Kafka
-  **Version Control**: Track contract versions and detect mismatches
-  **Hash-based Verification**: Ensure contract content consistency using SHA-256 hashing
-  **Saga Pattern Support**: Built-in support for distributed transaction compensation
-  **Runtime Enforcement**: Block operations when contracts are incompatible
-  **Polyglot Persistence**: Support for PostgreSQL, MongoDB, MySQL, and Cassandra

---

## Architecture

```
┌─────────────────┐         ┌─────────────────┐
│  Order Service  │         │ Payment Service │
│  (PostgreSQL)   │         │   (MongoDB)     │
│                 │         │                 │
│  ┌───────────┐  │         │  ┌───────────┐  │
│  │ CCF Lib   │  │◄───────►│  │ CCF Lib   │  │
│  │(Embedded) │  │  Kafka  │  │(Embedded) │  │
│  └───────────┘  │         │  └───────────┘  │
└─────────────────┘         └─────────────────┘
         │                           │
         └───────────┬───────────────┘
                     │
              ┌──────▼──────┐
              │    Kafka    │
              │  - order-events
              │  - payment-events
              │  - contract-registrations
              └─────────────┘
```

---

## Project Structure

```
ccf-integration/
├── ccf-framework/              # CCF Library (Maven artifact)
│   ├── src/main/java/ccf/ccf/
│   │   ├── enforcement/        # Transaction enforcement
│   │   ├── exception/          # Custom exceptions
│   │   ├── mapping/            # Data source mapping
│   │   ├── specification/      # Contract parsing & validation
│   │   │   ├── ContractParser.java
│   │   │   ├── ContractRegistry.java
│   │   │   ├── ContractRepository.java
│   │   │   └── ContractValidator.java
│   │   └── verification/       # Consistency verification
│   │       ├── ConsistencyVerifier.java
│   │       └── ConsistencyMonitorAspect.java
│   └── pom.xml
│
├── order-ms/                   # Order Microservice
│   ├── src/main/java/com/order/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── kafka/
│   │   └── ccf/
│   │       └── OrderCcfIntegration.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── contracts/
│   │       └── order-payment-contract.ccf
│   └── pom.xml
│
└── payment-ms/                 # Payment Microservice
    ├── src/main/java/com/payment/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── model/
    │   ├── kafka/
    │   └── ccf/
    │       └── PaymentCcfIntegration.java
    ├── src/main/resources/
    │   ├── application.yml
    │   └── contracts/
    │       └── order-payment-contract.ccf
    └── pom.xml
```

---

## Prerequisites

### Required Software

- **Java**: 17 or higher
- **Maven**: 3.8+
- **Docker**: 20.10+ (for infrastructure)
- **Docker Compose**: 1.29+ (optional)

### Infrastructure Components

- **PostgreSQL**: 14+ (Order Service database)
- **MongoDB**: 5.0+ (Payment Service database)
- **Apache Kafka**: 3.0+ (Event streaming)
- **Zookeeper**: 3.8+ (Kafka coordination)

---

## Quick Start

### Step 1: Start Infrastructure

#### Option A: Using Docker Compose (Recommended)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    container_name: postgres-order
    environment:
      POSTGRES_DB: orderdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  mongodb:
    image: mongo:5.0
    container_name: mongo-payment
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  postgres-data:
  mongo-data:
```

**Start all services:**
```bash
docker-compose up -d
```

#### Option B: Using Individual Docker Commands

```bash
# PostgreSQL
docker run --name postgres-order \
  -e POSTGRES_DB=orderdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  -d postgres:14

# MongoDB
docker run --name mongo-payment \
  -p 27017:27017 \
  -d mongo:5.0

# Zookeeper
docker run --name zookeeper \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -d confluentinc/cp-zookeeper:latest

# Kafka
docker run --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -d confluentinc/cp-kafka:latest
```

### Step 2: Create Kafka Topics

```bash
# Create order-events topic
docker exec -it kafka kafka-topics --create \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1

# Create payment-events topic
docker exec -it kafka kafka-topics --create \
  --topic payment-events \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1

# Create contract-registrations topic
docker exec -it kafka kafka-topics --create \
  --topic contract-registrations \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1

# Verify topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Step 3: Build CCF Framework

```bash
cd ccf-framework
mvn clean install
```

**Expected output:**
```
[INFO] Installing ccf-1.0.0.jar to C:\Users\jaaha\.m2\repository\ccf\ccf\1.0.0\ccf-1.0.0.jar
[INFO] BUILD SUCCESS
```

### Step 4: Build Microservices

```bash
# Build Order Service
cd order-ms
mvn clean package

# Build Payment Service
cd ../payment-ms
mvn clean package
```

### Step 5: Start Microservices

**Terminal 1 - Order Service:**
```bash
cd order-ms
mvn spring-boot:run
```

**Expected logs:**
```
Initializing CCF integration for Order Service
Successfully loaded contract: OrderPaymentConsistency version 1.0.0
Registering contract OrderPaymentConsistency for service OrderService
✅ Contract OrderPaymentConsistency now registered by services: [OrderService]
Tomcat started on port 8081
```

**Terminal 2 - Payment Service:**
```bash
cd payment-ms
mvn spring-boot:run
```

**Expected logs:**
```
Initializing CCF integration for Payment Service
Successfully loaded contract: OrderPaymentConsistency version 1.0.0
Registering contract OrderPaymentConsistency for service PaymentService
✅ Contract OrderPaymentConsistency now registered by services: [OrderService, PaymentService]
Tomcat started on port 8082
```

---

## Creating a Consistency Contract

### Contract DSL Syntax

Create a contract file: `src/main/resources/contracts/order-payment-contract.ccf`

```
CONTRACT OrderPaymentConsistency {
  
  VERSION: 1.0.0
  
  SERVICES: [OrderService, PaymentService]
  
  CONSISTENCY_LEVEL: CAUSAL
  
  INVARIANTS: {
    - Order.status = CONFIRMED IMPLIES Payment.status = AUTHORIZED
    - Order.total = Payment.amount
    - Order.status = CANCELLED IMPLIES Payment.status IN [DECLINED, REFUNDED]
  }
  
  SAGA: {
    STEP CreateOrder
    STEP AuthorizePayment
    STEP ConfirmOrder
  }
}
```

### Contract Elements Explained

| Element | Description | Example |
|---------|-------------|---------|
| **CONTRACT** | Contract name/identifier | `OrderPaymentConsistency` |
| **VERSION** | Semantic version | `1.0.0` |
| **SERVICES** | Participating microservices | `[OrderService, PaymentService]` |
| **CONSISTENCY_LEVEL** | Consistency guarantee | `CAUSAL`, `EVENTUAL`, `STRONG` |
| **INVARIANTS** | Consistency rules that must hold | See above |
| **SAGA** | Distributed transaction steps | See above |

---

## Integrating CCF into a Microservice

### Step 1: Add CCF Dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>ccf</groupId>
    <artifactId>ccf</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 2: Configure Application

Update `application.yml`:

```yaml
spring:
  application:
    name: order-service

  datasource:
    url: jdbc:postgresql://localhost:5433/orderdb
    username: postgres
    password: postgres

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: order-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8081

logging:
  level:
    com.order: DEBUG
    ccf.ccf: DEBUG
```

### Step 3: Enable Component Scanning

Update main application class:

```java
@SpringBootApplication(scanBasePackages = {"com.order", "ccf.ccf"})
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

### Step 4: Create CCF Integration Class

Create `OrderCcfIntegration.java`:

```java
package com.order.ccf;

import ccf.ccf.specification.*;
import ccf.ccf.specification.model.ConsistencyContract;
import ccf.ccf.verification.ConsistencyVerifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCcfIntegration {

    private final ContractParser contractParser;
    private final ContractValidator contractValidator;
    private final ContractRepository contractRepository;
    private final ContractRegistry contractRegistry;
    private final ConsistencyVerifier consistencyVerifier;

    @PostConstruct
    public void initialize() {
        log.info("Initializing CCF integration for Order Service");
        loadContract();
    }

    private void loadContract() {
        try {
            ConsistencyContract contract = contractParser.parse(
                "src/main/resources/contracts/order-payment-contract.ccf"
            );
            
            contractValidator.validate(contract);
            contractRepository.save(contract);
            contractRegistry.registerContract("OrderService", contract);
            
            log.info("Successfully loaded contract: {} version {}", 
                    contract.getContractName(), contract.getContractVersion());
            
        } catch (Exception e) {
            log.error("Failed to load contract: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateOrderConsistency(Object entity) {
        return consistencyVerifier.verify("OrderPaymentConsistency", entity);
    }
}
```

### Step 5: Use CCF in Service Layer

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderCcfIntegration ccfIntegration;

    @Override
    @MonitorConsistency(contractId = "OrderPaymentConsistency")
    public OrderResponse createOrder(OrderRequest request) {
        Order order = // ... create order logic
        
        order = orderRepository.save(order);
        
        // Validate consistency (throws exception if contract invalid)
        ccfIntegration.validateOrderConsistency(order);
        
        // Publish event
        eventProducer.sendOrderCreatedEvent(order);
        
        return mapToResponse(order);
    }
}
```

---

## Testing the Framework

### Test Scenario 1: Success Transaction ✅

**Create an order:**

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "productId": "PROD-001",
        "quantity": 2,
        "price": 50.00
      }
    ]
  }'
```

**Expected Response (HTTP 200):**
```json
{
  "id": 1,
  "customerId": "CUST-001",
  "totalAmount": 100.00,
  "status": "PENDING",
  "createdAt": "2025-11-30T16:00:00"
}
```

**Flow:**
1. Order created with status `PENDING`
2. `OrderCreatedEvent` published to Kafka
3. Payment Service consumes event
4. Payment processed (70% success rate)
5. `PaymentAuthorizedEvent` published
6. Order Service updates order to `CONFIRMED`

**Verify:**
```bash
curl http://localhost:8081/api/orders/1
```

**Expected:**
```json
{
  "id": 1,
  "customerId": "CUST-001",
  "totalAmount": 100.00,
  "status": "CONFIRMED",
  "createdAt": "2025-11-30T16:00:00"
}
```

### Test Scenario 2: Failed Transaction (Compensation) ❌

Payment fails (30% chance), order should be cancelled.

**Expected logs:**
```
Payment Service:
  Payment declined for order: 2
  Publishing PaymentFailedEvent

Order Service:
  Received payment failed event
  Cancelling order: 2
  Order cancelled: 2
```

**Verify:**
```bash
curl http://localhost:8081/api/orders/2
```

**Expected:**
```json
{
  "id": 2,
  "status": "CANCELLED"
}
```

### Test Scenario 3: Contract Mismatch (Should Block) 🚫

**Update Payment Service contract to different version:**

`payment-ms/src/main/resources/contracts/order-payment-contract.ccf`:
```
VERSION: 2.0.0  # Changed from 1.0.0
```

**Restart Payment Service:**
```bash
cd payment-ms
mvn spring-boot:run
```

**Expected logs in BOTH services:**
```
⚠️ CONTRACT VERSION MISMATCH! Service PaymentService uses v2.0.0 but other services use v1.0.0
❌ Contract OrderPaymentConsistency is INVALID - operations will be BLOCKED!
```

**Try to create order:**
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-003",
    "items": [{"productId": "PROD-001", "quantity": 1, "price": 50.00}]
  }'
```

**Expected Response (HTTP 503):**
```json
{
  "errorCode": "SERVICE_UNAVAILABLE",
  "message": "Contract validation failed: Contract OrderPaymentConsistency is INVALID",
  "suggestion": "Please ensure all services are using the same contract version"
}
```

**✅ Order creation BLOCKED - Framework working correctly!**

---

## Monitoring Contract Status

### Check Contract Registration

```bash
curl http://localhost:8081/api/ccf/contracts/OrderPaymentConsistency
```

**Response:**
```json
{
  "version": "1.0.0",
  "hash": "wuPbQ52vGG8tlwwlFdvcDBzdU4INhSEpSHMBl6tUjFw=",
  "services": ["OrderService", "PaymentService"]
}
```

### Check Contract Validity

```bash
curl http://localhost:8081/api/ccf/contracts/OrderPaymentConsistency/consistent
```

**Response:**
```
true  # Contract is consistent across services
false # Contract mismatch detected
```

---

## Monitoring Kafka Events

### Monitor Order Events

```bash
docker exec -it kafka kafka-console-consumer \
  --topic order-events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Monitor Payment Events

```bash
docker exec -it kafka kafka-console-consumer \
  --topic payment-events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Monitor Contract Registrations

```bash
docker exec -it kafka kafka-console-consumer \
  --topic contract-registrations \
  --from-beginning \
  --bootstrap-server localhost:9092
```

---

## Troubleshooting

### Issue 1: Kafka Connection Failed

**Error:**
```
org.apache.kafka.common.errors.TimeoutException: Failed to update metadata
```

**Solution:**
```bash
# Check Kafka is running
docker ps | grep kafka

# Check Kafka logs
docker logs kafka

# Restart Kafka
docker restart kafka
```

### Issue 2: Database Connection Failed

**Error:**
```
Connection to localhost:5433 refused
```

**Solution:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -p 5433 -U postgres -d orderdb

# Restart PostgreSQL
docker restart postgres-order
```

### Issue 3: Contract Parse Error

**Error:**
```
Failed to load contract: No enum constant ccf.ccf.specification.model.ConsistencyLevel.
```

**Solution:**
- Check contract file syntax
- Ensure `CONSISTENCY_LEVEL` is one of: `CAUSAL`, `EVENTUAL`, `STRONG`
- Verify file location: `src/main/resources/contracts/`

### Issue 4: CCF Classes Not Found

**Error:**
```
Cannot resolve symbol 'ccf'
```

**Solution:**
```bash
# Rebuild CCF framework
cd ccf-framework
mvn clean install

# Reload Maven in IDE
# IntelliJ: Right-click pom.xml → Maven → Reload Project
```

---

## Advanced Configuration

### Custom Consistency Levels

Define custom consistency levels in your contracts:

```
CONSISTENCY_LEVEL: STRONG
```

Options:
- **STRONG**: Immediate consistency (synchronous)
- **CAUSAL**: Causal consistency (ordered events)
- **EVENTUAL**: Eventual consistency (asynchronous)

### Custom Invariants

Add domain-specific invariants:

```
INVARIANTS: {
  - Order.total > 0
  - Payment.amount <= Order.total * 1.1
  - Order.items.length > 0
  - Payment.createdAt >= Order.createdAt
}
```

### Saga Compensation

Define compensation steps:

```
SAGA: {
  STEP CreateOrder COMPENSATE CancelOrder
  STEP AuthorizePayment COMPENSATE RefundPayment
  STEP ConfirmOrder COMPENSATE RevertConfirmation
}
```

---

## Performance Considerations

### Optimization Tips

1. **Contract Caching**: Contracts are loaded once at startup
2. **Async Validation**: Contract validation happens asynchronously via Kafka
3. **Local Registry**: Each service maintains a local contract registry
4. **Minimal Overhead**: Contract verification adds ~5-10ms per operation

### Scalability

- **Horizontal Scaling**: Each service instance validates independently
- **Kafka Partitioning**: Use multiple partitions for high throughput
- **Database Sharding**: CCF supports sharded databases

---

## Production Deployment

### Best Practices

1. **Contract Versioning**: Always use semantic versioning
2. **Blue-Green Deployment**: Update contracts during blue-green deployments
3. **Monitoring**: Set up alerts for contract mismatches
4. **Logging**: Enable DEBUG logging for CCF components
5. **Health Checks**: Include contract validation in health endpoints

### Environment Variables

```bash
# Order Service
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/orderdb
export SPRING_KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092

# Payment Service
export SPRING_DATA_MONGODB_URI=mongodb://prod-mongo:27017/paymentdb
export SPRING_KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092
```

---

## API Reference

### Consistency Annotations

#### @MonitorConsistency

Monitors method execution for consistency violations.

```java
@MonitorConsistency(contractId = "OrderPaymentConsistency")
public OrderResponse createOrder(OrderRequest request) {
    // Method implementation
}
```

### Contract Parser API

#### ContractParser.parse()

```java
ConsistencyContract contract = contractParser.parse("path/to/contract.ccf");
```

### Contract Registry API

#### ContractRegistry.registerContract()

```java
contractRegistry.registerContract("ServiceName", contract);
```

#### ContractRegistry.isContractValid()

```java
boolean isValid = contractRegistry.isContractValid("ContractId");
```

### Consistency Verifier API

#### ConsistencyVerifier.verify()

```java
boolean isValid = consistencyVerifier.verify("ContractId", entity);
```

---

## Contributing

### Development Setup

```bash
# Clone repository
git clone https://github.com/hamzajaa/ccf-integration.git

# Build all modules
cd ccf-integration
mvn clean install

# Run tests
mvn test
```

### Adding New Features

1. Create feature branch
2. Implement feature in CCF framework
3. Update microservices to use new feature
4. Add tests
5. Update documentation
6. Submit pull request

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## References

### Research Papers

- Consistency Contracts for Polyglot Persistence (2025)
- Saga Pattern for Distributed Transactions
- CAP Theorem and Eventual Consistency

### Related Technologies

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Apache Kafka**: https://kafka.apache.org/
- **PostgreSQL**: https://www.postgresql.org/
- **MongoDB**: https://www.mongodb.com/

---

## Support

For questions and support:
- **Email**: hamzajaa2017@gmail.com
- **GitHub Issues**: https://github.com/hamzajaa/ccf-integration/issues
- **Documentation**: 

---

## Acknowledgments

This project was developed as part of an MSc Software Engineering dissertation at Western International College London / University of Greater Manchester.

**Supervisor**: Dr. Nuwan Kuruwitaarachchi

**Student**: Hamza Jaa (ID: 2444814)

---

**Last Updated**: November 30, 2025
**Version**: 1.0.0
