
# Commerce Backend

### Java 21 · Spring Boot · REST APIs · MySQL · Hibernate · Redis · Payment Processing · AWS

A modular e-commerce backend demonstrating practical Java backend engineering, relational database design, RESTful API development, payment processing, asynchronous communication, performance optimization, and cloud-native architecture.

The system combines product catalog management, category management, advanced database querying, pagination, searching, sorting, payment gateway integration, webhook processing, reconciliation, background scheduling, and Redis caching.

The engineering focus is on building a backend that is **correct, maintainable, testable, observable, and designed for growth**.

## Engineering Highlights

- Designed layered Spring Boot services with constructor-based dependency injection and clear API contracts.
- Implemented product and category management using Spring Data JPA and Hibernate.
- Modeled relational associations, identifier strategies, and inheritance scenarios.
- Investigated N+1 query behavior and optimized association retrieval using fetch joins.
- Implemented database-backed pagination, case-insensitive search, and multi-field sorting.
- Designed provider-independent payment integration using interfaces and dependency injection.
- Implemented simulated payment initiation, callbacks, webhooks, idempotency, and reconciliation.
- Built scheduled background processing for unresolved payment transactions.
- Integrated Redis caching with cache-key design, expiration, and invalidation.
- Used Docker for reproducible local infrastructure.
- Applied request validation, exception handling, API documentation, and automated testing.
- Designed an AWS reference architecture covering compute, storage, networking, database, deployment, and monitoring.

---

# 1. System Architecture

Commerce Backend separates application responsibilities into distinct components.

```text
                         CLIENT APPLICATIONS
                                  |
                                  v
                           REST API LAYER
                                  |
                 +----------------+----------------+
                 |                |                |
                 v                v                v
           PRODUCT API       CATEGORY API      PAYMENT API
                 |                |                |
                 v                v                v
           PRODUCT SERVICE  CATEGORY SERVICE  PAYMENT SERVICE
                 |                |                |
                 |                |          PAYMENT GATEWAY
                 |                |             ADAPTER
                 |                |                |
                 |                |          MOCK PROVIDERS
                 |                |                |
                 +----------------+----------------+
                                  |
                           PERSISTENCE LAYER
                                  |
                           JPA / HIBERNATE
                                  |
                                MYSQL

                       SUPPORTING COMPONENTS

                         REDIS CACHE
                         WEBHOOK HANDLER
                         RECONCILIATION JOB
                         VALIDATION LAYER
                         EXCEPTION HANDLER
                         APPLICATION LOGGING
```

## Architectural Principles

### Separation of Concerns

Each layer has a specific responsibility:

| Layer | Responsibility |
|---|---|
| Controller | Receive HTTP requests and produce responses |
| Service | Execute and coordinate business operations |
| Repository | Perform persistence operations |
| Entity | Represent persistent domain data |
| DTO | Define API request and response contracts |
| Integration | Communicate with external systems |
| Scheduler | Execute periodic background operations |

### Dependency Injection and Inversion of Control

Spring manages application components and supplies their dependencies.

Constructor injection is used to make component dependencies explicit.

```java
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
```

The application uses:

- Spring IoC container
- Spring-managed beans
- Constructor injection
- `@Component`
- `@Service`
- `@Repository`
- `@RestController`
- `@Configuration`
- `@Bean`
- `@Qualifier`
- Configuration properties

### Modular Design

Product, category, payment, and caching responsibilities are separated so that each component can evolve independently.

The payment integration boundary is designed to support alternative gateway implementations without rewriting payment business logic.

---

# 2. HTTP and REST API Engineering

The backend uses Spring MVC to process incoming HTTP requests.

## HTTP Request Lifecycle

```text
HTTP Client
     |
     v
Embedded Web Server
     |
     v
DispatcherServlet
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
Database
     |
     v
Response DTO
     |
     v
HTTP Response
```

Spring MVC maps incoming requests to controller methods using annotations.

### HTTP Concepts

- Client-server communication
- Request-response lifecycle
- Stateless HTTP requests
- URL and URI structure
- HTTP methods
- Request headers
- Request bodies
- Query parameters
- Path variables
- JSON serialization and deserialization
- HTTP status codes
- REST resource design

### Supported HTTP Methods

| Method | Usage |
|---|---|
| GET | Retrieve resources |
| POST | Create resources or initiate operations |
| PUT | Update resources |
| DELETE | Remove resources |

### Spring MVC Annotations

```java
@RestController
@RequestMapping("/api/products")
```

```java
@GetMapping("/{id}")
```

```java
@PostMapping
```

```java
@PutMapping("/{id}")
```

```java
@DeleteMapping("/{id}")
```

```java
@PathVariable
@RequestParam
@RequestBody
```

These annotations connect incoming HTTP requests to Java methods and map request data to application objects.

---

# 3. Product Management

The product catalog exposes RESTful CRUD operations.

## Capabilities

- Product creation
- Product retrieval
- Product updates
- Product deletion
- Product-category associations
- Price-based filtering
- Category-based filtering
- Search by product name
- Database pagination
- Multi-field sorting

## Product API Reference

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/products` | Create product |
| GET | `/api/products` | Retrieve products |
| GET | `/api/products/{id}` | Retrieve product by ID |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |
| GET | `/api/products/category/{categoryId}` | Filter by category |
| GET | `/api/products/above-price?price=5000` | Filter by price |
| GET | `/api/products/search?name=laptop` | Search by name |
| GET | `/api/products/details` | Retrieve products with category details |

## Request Validation

Product creation and update requests are validated using Jakarta Bean Validation.

```java
public record ProductRequest(
    @NotBlank String name,
    @NotNull @Positive Double price,
    Long categoryId
) {}
```

This prevents invalid input from reaching the persistence layer.

---

# 4. Category Management and Relational Modeling

Categories organize products through relational database associations.

## Entity Relationship

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```

A product belongs to a category, while one category may be associated with multiple products.

## Relationship Concepts

- One-to-one
- One-to-many
- Many-to-one
- Many-to-many
- Primary keys
- Foreign keys
- Owning side of relationships
- Inverse side of relationships
- Join columns
- Entity references
- Relational integrity

The design maps Java domain objects to normalized relational database structures.

---

# 5. JPA, Hibernate, and ORM Engineering

Spring Data JPA and Hibernate provide the persistence layer.

## ORM Responsibilities

Object-Relational Mapping translates between Java objects and relational database records.

The application uses:

- JPA entities
- Repository interfaces
- Entity identifier generation
- Entity relationships
- Persistence context
- Hibernate SQL generation
- Derived repository queries
- Custom JPQL queries
- Lazy and eager loading

## Repository Abstraction

```java
public interface ProductRepository
        extends JpaRepository<Product, Long> {
}
```

`JpaRepository` provides standard CRUD operations without manually writing SQL for each operation.

## Derived Queries

```java
List<Product> findByCategoryId(Long categoryId);
```

Spring Data JPA interprets the method name and constructs the corresponding database query.

## Custom JPQL

```java
@Query("""
    SELECT p
    FROM Product p
    WHERE p.price > :price
    """)
List<Product> findProductsAbovePrice(
    @Param("price") Double price
);
```

JPQL operates on entity names and properties rather than raw database tables.

## Entity Identifier Strategies

The persistence design examines:

- Numeric primary keys
- Database-generated identity values
- UUID identifiers
- Identifier uniqueness
- Primary-key and foreign-key relationships
- Identifier-generation trade-offs

## JPA Inheritance

The entity-modeling work includes the three primary inheritance approaches:

### SINGLE_TABLE

An inheritance hierarchy is represented in one database table.

### JOINED

Common parent attributes and subclass-specific attributes are stored in related tables.

### TABLE_PER_CLASS

Concrete entity classes are represented using separate tables.

These strategies demonstrate how object-oriented inheritance can be represented in relational storage.

---

# 6. Hibernate Fetching and the N+1 Problem

ORM fetching behavior directly affects database performance.

## Lazy Loading

Associated entities are loaded when the application accesses them.

## Eager Loading

Associated entities are retrieved according to the configured eager-fetching behavior.

## N+1 Query Problem

An N+1 problem occurs when fetching a collection of entities causes additional queries for their associations.

```text
Fetch Products
      |
      v
Query Product Table
      |
      v
Access Product Categories
      |
      v
Additional Category Queries
```

This can create significant database overhead as the result set grows.

## Fetch Modes

The ORM investigation covers:

- Select fetching
- Join fetching
- Subselect fetching
- Lazy fetching
- Eager fetching

Fetch strategies are evaluated according to SQL behavior and endpoint requirements.

## Fetch Join Optimization

```java
@Query("""
    SELECT p
    FROM Product p
    LEFT JOIN FETCH p.category
    """)
List<Product> findAllWithCategory();
```

A fetch join retrieves the required product-category associations in the same query.

## Performance Principles

- Inspect generated SQL
- Identify unnecessary queries
- Compare query counts
- Avoid indiscriminate eager fetching
- Fetch only the associations required by an endpoint
- Understand the effect of the persistence context
- Choose appropriate strategies for different data-access patterns

---

# 7. Database Design and Schema Evolution

The application uses MySQL as its persistent relational database.

## Database Engineering

- Table and column mapping
- Primary-key constraints
- Foreign-key constraints
- Entity relationships
- SQL queries
- Joins
- Schema generation
- Schema updates
- Data integrity
- Schema compatibility considerations

The persistence layer is designed so that application logic remains separate from database implementation details.

Database changes require attention to compatibility, existing records, and deployment coordination.

---

# 8. Pagination, Searching, and Sorting

The product API retrieves data efficiently without loading complete result sets into application memory.

## Pagination

```http
GET /api/products?page=0&size=10
```

Spring Data JPA uses `Pageable` and `Page<T>`.

```java
Page<Product> findAll(Pageable pageable);
```

A paginated response contains:

- Current page
- Requested page size
- Matching records
- Total elements
- Total pages
- First-page indicator
- Last-page indicator

## SQL Pagination

```sql
SELECT *
FROM product
ORDER BY id
LIMIT 10 OFFSET 0;
```

The database returns only the requested records.

## Sorting

Ascending:

```http
GET /api/products?sort=price,asc
```

Descending:

```http
GET /api/products?sort=price,desc
```

Multiple sorting fields:

```http
GET /api/products?sort=name,asc&sort=price,asc
```

## Searching

Case-insensitive substring matching:

```java
Page<Product> findByNameContainingIgnoreCase(
    String name,
    Pageable pageable
);
```

Combined search:

```http
GET /api/products/search?name=laptop&page=0&size=10&sort=price,asc
```

## Important Design Considerations

- Database-level result limiting
- Stable sorting
- Empty result sets
- Out-of-range pages
- Default pagination parameters
- Maximum page-size validation
- Filtering before pagination
- Predictable response metadata

---

# 9. Search Architecture: SQL and Full-Text Search

Different search requirements require different query strategies.

## Relational Database Search

MySQL and Spring Data JPA are appropriate for:

- Exact matches
- Category filters
- Numeric comparisons
- Structured queries
- Basic substring searches
- Paginated database results

## Full-Text Search

Full-text search engines such as Elasticsearch are designed for search workloads involving:

- Text indexing
- Searching titles and descriptions
- Relevance-oriented results
- Larger searchable document collections
- Search-oriented data structures

## Elasticsearch and OpenSearch

Elasticsearch provides distributed search capabilities through APIs.

Amazon OpenSearch Service provides a managed search and analytics option on AWS.

The architecture distinguishes relational transaction storage from specialized full-text indexing.

MySQL remains the authoritative persistence layer for product and payment records.

---

# 10. Third-Party REST API Integration

The backend integrates with external HTTP services through dedicated client components.

## Integration Flow

```text
Client
   |
   v
Application Controller
   |
   v
Application Service
   |
   v
External API Client
   |
   v
Third-Party REST API
   |
   v
External Response
   |
   v
Internal DTO Mapping
   |
   v
Client Response
```

## Engineering Concepts

- `RestTemplate`
- HTTP requests and responses
- External API endpoints
- Request headers
- JSON response mapping
- API request parameters
- External DTOs
- Connection failures
- HTTP error handling
- Exception translation
- Configuration-driven endpoints

## Example Integration Pattern

A product API can delegate to an external product provider and transform the provider response into its own API contract.

```text
GET /api/products/{id}
          |
          v
Product Service
          |
          v
External Product API
          |
          v
Product DTO
          |
          v
JSON Response
```

This separates the application's public API from the representation used by the external provider.

---

# 11. API Documentation and Exception Handling

## Swagger / OpenAPI

API documentation describes available endpoints, parameters, request bodies, and responses.

Swagger UI provides an interactive interface for exploring and testing APIs.

## Centralized Exception Handling

Application exceptions are handled consistently instead of allowing uncontrolled internal errors to reach API clients.

### Error Scenarios

- Missing resources
- Invalid input
- Validation failures
- External API errors
- Database failures
- Invalid application state

### HTTP Response Semantics

- `200 OK`
- `201 Created`
- `204 No Content`
- `400 Bad Request`
- `404 Not Found`
- `500 Internal Server Error`

Errors are mapped to appropriate HTTP statuses and useful response information.

---

# 12. Payment Processing Architecture

The payment component separates transaction management from payment-provider-specific APIs.

## Service Boundaries

```text
Client
   |
   +--------------------+
   |                    |
   v                    v
Product API         Payment API
   |                    |
   v                    v
Product Service     Payment Service
                        |
                        v
                 Payment Gateway
                     Interface
                        |
              +---------+---------+
              |                   |
              v                   v
         Razorpay-Style      Stripe-Style
         Gateway Adapter     Gateway Adapter
              |                   |
              +---------+---------+
                        |
                  Mock Providers
```

The gateway implementations use simulated responses, allowing payment behavior to be tested locally without initiating real financial transactions.

## Why Separate Payment Logic?

Payment processing has different requirements from product catalog management.

It must manage:

- Transaction state
- External gateway communication
- Retry behavior
- Asynchronous notifications
- Security-sensitive operations
- Payment consistency
- Reconciliation

A dedicated payment boundary allows payment logic to evolve independently from product APIs.

---

# 13. Payment Gateway Abstraction

The application uses interface-based design to support alternative payment providers.

## Payment Gateway Interface

```java
public interface PaymentGateway {

    PaymentInitiationResult initiatePayment(
        PaymentRequest request
    );

    PaymentStatus fetchPaymentStatus(
        String externalPaymentId
    );
}
```

Provider-specific implementations handle the corresponding integration behavior.

## Dependency Injection

Spring can manage multiple implementations of the same interface.

`@Qualifier` identifies which implementation should be injected when multiple candidates exist.

```java
public PaymentService(
    @Qualifier("mockRazorpayGateway")
    PaymentGateway paymentGateway
) {
    this.paymentGateway = paymentGateway;
}
```

## Configuration

Application settings are externalized through configuration properties and environment variables.

Concepts include:

- `@Value`
- `@Configuration`
- `@Bean`
- `@Qualifier`
- Provider configuration
- API keys
- Secret credentials
- Environment-specific settings

The integration boundary isolates gateway-specific SDKs and request formats from payment business logic.

---

# 14. Complete Payment Flow

## Step 1 — Payment Initiation

```text
Customer Clicks Pay
        |
        v
Payment API
        |
        v
Create Local Payment Record
        |
        v
Payment Gateway Adapter
        |
        v
Create Gateway Payment / Payment Link
        |
        v
Return Payment Instructions
        |
        v
Customer Completes Payment
```

Payment initiation does not automatically imply payment success.

The application records the payment as pending until authoritative confirmation is available.

## Step 2 — Payment Completion

A payment provider processes the transaction and produces an outcome.

Possible states include:

- Created
- Pending
- Authorized
- Captured / Successful
- Failed

Transitions depend on the payment provider and the payment operation.

## Step 3 — Payment Confirmation

The application receives or retrieves authoritative payment status information.

This may happen through:

- Callback handling
- Webhooks
- Payment-status API requests
- Reconciliation

---

# 15. Payment Links and Hosted Checkout

Payment providers can offer hosted payment experiences and payment links.

## Payment Link Flow

```text
Client
   |
   v
Payment API
   |
   v
Gateway Adapter
   |
   v
Generate Payment Link
   |
   v
Return Link to Client
   |
   v
Customer Opens Hosted Payment Page
   |
   v
Customer Completes Payment
   |
   v
Gateway Notifies Application
```

## Integration Concepts

- Payment amount
- Currency
- Payment description
- Provider payment identifier
- Callback URL
- Hosted checkout
- Payment status
- Gateway response data

A hosted payment page reduces the need for the application to directly handle sensitive payment instrument details.

---

# 16. Callbacks, Webhooks, and Polling

Payment completion is frequently asynchronous.

## Callback

A callback provides a mechanism for reporting the outcome of an earlier operation.

## Webhook

A webhook is an HTTP notification sent by a provider to the application's configured endpoint.

## Webhook Flow

```text
Payment Provider
       |
       v
Webhook Endpoint
       |
       v
Validate Request
       |
       v
Verify Authenticity
       |
       v
Find Local Payment
       |
       v
Validate State Transition
       |
       v
Update MySQL
       |
       v
Return HTTP Acknowledgement
```

## Polling

The client may periodically request the current payment status.

```text
Frontend
   |
   v
GET Payment Status
   |
   v
Payment Service
   |
   v
MySQL
   |
   v
Current Status
```

Polling is useful when the client is waiting for an asynchronous payment result.

The payment initiation endpoint and the webhook endpoint perform different responsibilities.

---

# 17. Payment Idempotency and Reliability

Payment providers may retry notifications when acknowledgements are lost or delayed.

Repeated delivery must not create duplicate business effects.

## Example

```text
Payment Success Event
          |
          v
Webhook Received
          |
          v
Payment Marked Successful

Same Event Delivered Again
          |
          v
Duplicate Identified
          |
          v
No Duplicate Business Effect
```

## Reliability Controls

- Unique payment identifiers
- Unique external-event identifiers
- Database uniqueness constraints
- Transactional status updates
- Valid state transitions
- Safe retry behavior
- Duplicate-event detection
- Event authenticity validation

Database constraints protect invariants even when concurrent requests or retries occur.

Idempotency is treated as a correctness requirement rather than an optional optimization.

---

# 18. Payment Reconciliation

Webhook delivery and client callbacks cannot be assumed to succeed in every situation.

Reconciliation provides a recovery mechanism.

## Reconciliation Flow

```text
Scheduled Reconciliation Job
             |
             v
Find Unresolved Payments
             |
             v
Query Payment Gateway
             |
             v
Compare Gateway and Local Status
             |
             v
Identify Discrepancies
             |
             v
Apply Valid State Corrections
             |
             v
Persist Results
```

## Failure Scenarios

- Payment succeeds but webhook is missed
- Application is temporarily unavailable
- Gateway request times out
- Client closes the payment page
- Callback arrives late
- Duplicate event arrives
- Local database state differs from gateway state

The gateway's authoritative status is used to reconcile unresolved local records.

---

# 19. Background Scheduling and Cron

Spring Scheduling runs recurring background jobs.

## Scheduling Concepts

- `@EnableScheduling`
- `@Scheduled`
- Cron expressions
- Fixed-rate scheduling
- Fixed-delay scheduling
- Background processing
- Periodic reconciliation

## Example

```java
@Scheduled(cron = "0 */10 * * * *")
public void reconcilePayments() {
    reconciliationService.reconcilePendingPayments();
}
```

The example runs every ten minutes using the scheduler's configured time zone.

## Reliability Requirements

Scheduled tasks must handle:

- Repeated execution
- Temporary failures
- Retry behavior
- Concurrent processing
- Valid payment-state updates
- Duplicate prevention

---

# 20. Redis Caching

Redis provides an in-memory data store that can be used to cache frequently requested application data.

The application uses Redis to reduce repeated database reads for suitable product queries.

## Cache-Aside Architecture

```text
Product API
    |
    v
Product Service
    |
    v
Redis Cache
   /   \
 HIT   MISS
  |      |
  |      v
  |    MySQL
  |      |
  |      v
  |  Populate Cache
  |      |
  +------+
     |
     v
API Response
```

## Spring Cache Integration

The application uses Spring's cache abstraction.

Important annotations include:

- `@EnableCaching`
- `@Cacheable`
- `@CachePut`
- `@CacheEvict`

## Redis Concepts

- In-memory storage
- Key-value data model
- Redis keys
- Cached values
- Cache hits
- Cache misses
- Expiration
- Time-to-live
- Cache invalidation
- Database fallback

## Cache Consistency

When product data changes, cached results may become outdated.

Write operations therefore require appropriate cache eviction or refresh behavior.

The relational database remains the authoritative source of product data.

---

# 21. Redis Performance and Scaling Concepts

Caching reduces repeated work when the same data is requested frequently.

## Database Request Without Cache

```text
Client → Application → MySQL → Application → Client
```

## Request With Cache Hit

```text
Client → Application → Redis → Application → Client
```

## Design Considerations

- Suitable cache keys
- Cache expiration policies
- Memory usage
- Cache invalidation
- Cache misses
- Cache correctness
- Redis instance capacity
- Data distribution

## Distributed Cache Concepts

The design also considers:

- Cache partitioning
- Sharding
- Hash-based distribution
- Consistent hashing
- Balancing keys across cache instances

These concepts become relevant when a single cache instance cannot efficiently handle the required data volume or workload.

---

# 22. Docker-Based Local Infrastructure

Docker provides isolated environments for application dependencies.

## Development Architecture

```text
Local Development Environment
            |
            +---- Spring Boot
            |
            +---- MySQL
            |
            +---- Redis Container
            |
            +---- Mock Payment Provider
```

## Docker Concepts

- Images
- Containers
- Container lifecycle
- Docker Hub
- Image pulling
- Port mapping
- Environment variables
- Container networking
- Persistent volumes
- Docker Compose

## Redis Container Example

```bash
docker pull redis
```

```bash
docker run -d \
  --name commerce-redis \
  -p 6379:6379 \
  redis
```

## Useful Docker Commands

```bash
docker ps
docker ps -a
docker logs commerce-redis
docker stop commerce-redis
docker start commerce-redis
```

Docker allows supporting services to run without installing every dependency directly on the development machine.

---

# 23. AWS Reference Deployment Architecture

The system has a deployment architecture designed around AWS compute, networking, storage, relational databases, load balancing, and monitoring.

## Architecture

```text
                       INTERNET USERS
                              |
                              v
                       ROUTE 53 DNS
                              |
                              v
                     INTERNET-FACING ALB
                              |
               +--------------+--------------+
               |                             |
               v                             v
        PRIVATE APP SUBNET A          PRIVATE APP SUBNET B
               |                             |
               v                             v
          EC2 INSTANCE A                EC2 INSTANCE B
          SPRING BOOT                   SPRING BOOT
          EBS STORAGE                   EBS STORAGE
               |                             |
               +--------------+--------------+
                              |
                              v
                         RDS MYSQL
                       PRIVATE SUBNETS

                   REDIS CACHING LAYER

                   CLOUDWATCH MONITORING
```

The VPC provides the network boundary, and an Internet Gateway provides internet connectivity for the public load balancer.

The architecture illustrates application instances distributed across Availability Zones. Database high availability requires its own appropriate configuration.

---

# 24. AWS Regions and Availability Zones

## Region

A geographic AWS infrastructure location.

## Availability Zone

A distinct infrastructure location within a Region, consisting of one or more data centers.

## Multi-AZ Architecture

Multiple Availability Zones can reduce dependency on a single infrastructure location.

High availability is achieved through intentional application and infrastructure configuration, not merely by creating one EC2 instance.

---

# 25. EC2 — Elastic Compute Cloud

EC2 provides virtual computing capacity.

## Application Runtime

```text
EC2 Instance
     |
     +---- Operating System
     |
     +---- CPU
     |
     +---- RAM
     |
     +---- Java Virtual Machine
     |
     +---- Spring Boot Application
```

## EC2 Concepts

### Amazon Machine Image

Defines the operating system and initial software environment.

### Instance Type

Determines compute characteristics such as CPU and memory.

### Key Pair

Supports secure SSH authentication.

### Security Group

Controls permitted network traffic.

### Public and Private IP Addresses

Determine network addressing and connectivity options.

### Instance Lifecycle

- Launch
- Start
- Stop
- Restart
- Terminate

Stopping an EC2 instance stops its running processes.

Persistent storage behavior depends on the attached storage configuration.

---

# 26. EBS — Elastic Block Store

EBS provides persistent block storage for EC2 instances.

## Storage Responsibilities

- Operating system files
- Application JAR files
- Configuration files
- Application-generated data
- Persistent storage volumes

## EC2 and EBS

```text
EC2 CPU + RAM
     |
     | Executes Spring Boot
     |
     v
EBS Storage
     |
     | Stores persistent files
```

EBS-backed storage normally survives an instance stop.

Whether volumes survive instance termination depends on their configured deletion behavior.

---

# 27. RDS — Relational Database Service

RDS provides managed relational database infrastructure.

## Database Architecture

```text
Spring Boot on EC2
       |
       | JDBC
       | MySQL Port 3306
       v
RDS MySQL
```

## RDS Responsibilities

- Database hosting
- Managed maintenance capabilities
- Backups
- Database monitoring
- Storage management
- Configurable availability options

## Application Responsibilities

- SQL queries
- Schema design
- Data integrity
- Index decisions
- Application transactions
- Query performance

Using RDS does not remove the application's responsibility for database correctness and query design.

---

# 28. AWS VPC and Networking

## Virtual Private Cloud

A VPC is a logically isolated AWS network.

Example CIDR:

```text
10.0.0.0/16
```

## Subnets

Subnets divide the VPC IP address space.

Each subnet belongs to one Availability Zone.

### Public Subnets

Used for internet-facing resources such as public load balancers and public NAT Gateways.

### Private Application Subnets

Used for application instances that should not be directly reachable from the public internet.

### Private Database Subnets

Used for database resources.

---

# 29. Route Tables

Route tables determine how network traffic is directed.

A route table is associated with a subnet.

## Public Subnet Routing

```text
Destination       Target

10.0.0.0/16       local
0.0.0.0/0         Internet Gateway
```

## Private Subnet Routing

```text
Destination       Target

10.0.0.0/16       local
0.0.0.0/0         NAT Gateway
```

### Important Distinction

**Route Table:** Selects a network path.

**Security Group:** Controls whether network traffic is permitted.

**Load Balancer:** Selects an application target.

These components perform different functions.

---

# 30. Internet Gateway and NAT Gateway

## Internet Gateway

An Internet Gateway connects a VPC to the internet.

Appropriate routing, addressing, and security rules are still required.

## NAT Gateway

A public NAT Gateway enables resources in private subnets to initiate outbound internet connections without assigning public IP addresses to those resources.

## Outbound Payment API Flow

```text
Spring Boot on Private EC2
            |
            v
Private Subnet Route Table
            |
            v
NAT Gateway
            |
            v
Internet Gateway
            |
            v
External Payment API
```

The response returns through the established connection.

NAT does not provide unrestricted inbound access to private instances.

---

# 31. AWS Security Groups and IAM

## Security Groups

Security groups are stateful virtual firewalls that control permitted inbound and outbound connections.

### Example Rules

**ALB Security Group**

```text
Inbound: HTTPS 443 from internet
```

**EC2 Security Group**

```text
Inbound: Application port from ALB security group
```

**RDS Security Group**

```text
Inbound: MySQL 3306 from application security group
```

## IAM — Identity and Access Management

IAM controls permissions for AWS users, roles, and services.

## Security Principles

- Least-privilege access
- Restricted inbound traffic
- Private application networking
- Private database networking
- Externalized secrets
- Appropriate IAM permissions
- Secure SSH authentication
- Monitoring and auditing

---

# 32. Complete AWS Incoming Request Flow

Example:

```http
GET https://api.myshop.com/products
```

## Request Sequence

```text
Customer Browser
       |
       v
Route 53 DNS Resolution
       |
       v
Public ALB Destination
       |
       v
Internet Gateway / VPC Connectivity
       |
       v
Application Load Balancer
       |
       v
ALB Selects Healthy EC2 Target
       |
       v
EC2 Security Group
       |
       v
Spring Boot Application
       |
       v
ProductController
       |
       v
ProductService
       |
       v
ProductRepository
       |
       v
RDS MySQL
       |
       v
Product DTO
       |
       v
JSON Response
```

## Responsibility Breakdown

1. **Route 53** resolves the domain name.
2. **Internet Gateway** provides internet connectivity to the VPC.
3. **Route tables** determine applicable network paths.
4. **Security groups** permit authorized network traffic.
5. **ALB** forwards the request to a healthy application target.
6. **EC2** executes the Spring Boot application.
7. **Hibernate/JPA** performs the required database operations.
8. **RDS** returns the requested data.
9. **Spring Boot** returns the JSON response.

DNS resolution is a lookup operation, not an HTTP proxy hop.

---

# 33. Elastic Load Balancing and Availability

An Application Load Balancer receives HTTP/HTTPS requests and forwards them to healthy application targets.

## Responsibilities

- Listener configuration
- HTTP routing
- Target groups
- Health checks
- Distribution of incoming requests
- Multiple-target support

## Request Distribution

```text
Application Load Balancer
          |
          +--------> EC2 A
          |
          +--------> EC2 B
```

The design reduces reliance on a single application server.

Application availability and database availability must be addressed separately.

---

# 34. Elastic Beanstalk and Deployment

AWS Elastic Beanstalk provides managed application deployment environments.

## Deployment Flow

```text
Java Source Code
       |
       v
Maven Build
       |
       v
Executable JAR
       |
       v
Deployment Package
       |
       v
Elastic Beanstalk Environment
       |
       v
EC2 Application Runtime
       |
       v
Spring Boot API
```

## Deployment Concepts

- Java runtime selection
- Application packaging
- Environment configuration
- IAM service roles
- Instance profiles
- Networking configuration
- Application ports
- Load balancer configuration
- Deployment health
- Environment rebuilding
- Deployment logs

## Deployment Troubleshooting

Application deployments may fail because of:

- Incorrect application port
- Missing environment variables
- Missing runtime dependencies
- Incorrect database configuration
- Insufficient IAM permissions
- Incorrect network permissions
- Application startup failures

Elastic Beanstalk simplifies environment orchestration while retaining the need for infrastructure knowledge.

---

# 35. Route 53 and DNS

Route 53 provides DNS capabilities for resolving domain names and routing users to application destinations.

## DNS Resolution

```text
api.myshop.com
      |
      v
Route 53
      |
      v
Load Balancer Destination
```

## Concepts

- DNS records
- Domain-name resolution
- AWS resource aliases
- Routing policies
- Health-check-based routing

Route 53 resolves names; it does not execute Spring Boot requests.

---

# 36. CloudWatch and Operational Monitoring

CloudWatch provides monitoring through metrics, logs, dashboards, and alarms.

## EC2 Monitoring

- CPU utilization
- Network traffic
- Instance status checks

## RDS Monitoring

- Database CPU utilization
- Database connections
- Storage utilization
- Database performance indicators

## Application Monitoring

- Application logs
- Startup failures
- Error conditions
- Request behavior
- Health information

## Monitoring Flow

```text
Application / AWS Resources
             |
             v
Metrics and Logs
             |
             v
CloudWatch
             |
             v
Dashboards and Alarms
             |
             v
Operational Investigation
```

Monitoring supports troubleshooting and infrastructure health analysis.

---

# 37. Cloud Cost and Resource Lifecycle

Cloud infrastructure requires explicit cost management.

## Cost Areas

- EC2 compute
- EBS storage
- RDS databases
- Load balancers
- NAT Gateways
- Network transfer
- Monitoring services

## Operational Practices

- Evaluate resource pricing
- Select appropriate instance sizes
- Monitor estimated charges
- Use budgets and billing alerts
- Stop unused compute resources
- Remove unused storage and networking resources
- Understand the difference between stopping and terminating resources

Billing alerts are monitoring controls, not guaranteed spending caps.

---

# 38. Testing Strategy

The application supports testing at several levels.

## Unit Testing

JUnit and Mockito are used to isolate business logic and replace external dependencies.

## Repository Testing

Verifies:

- Persistence operations
- Derived queries
- Custom JPQL
- Entity relationships
- Pagination
- Sorting
- Searching

## Controller Testing

Verifies:

- Endpoint mappings
- Request validation
- HTTP status codes
- Response DTOs
- Error responses

## Integration Testing

Exercises application components working together.

## Payment Testing

Uses a simulated payment gateway for:

- Successful payment
- Failed payment
- Missing notification
- Duplicate notification
- Delayed confirmation
- Invalid notification
- Idempotency
- Reconciliation
- Repeated scheduled execution

## Redis Testing

Verifies:

- Initial cache miss
- Repeated cache hit
- Cache expiration
- Cache invalidation after writes
- Database fallback

---

# 39. Git and Development Workflow

Git provides source-control history, collaboration, and change tracking.

## Git Concepts

- Working directory
- Staging area
- Commits
- Commit history
- Branching
- Checkout
- HEAD
- Merge
- Merge conflicts
- Rebase
- Clone
- Fetch
- Pull
- Push
- Remote repositories
- Origin and upstream
- Forks
- Pull requests
- Code review

## Collaboration Flow

```text
Create Feature Branch
        |
        v
Implement Changes
        |
        v
Run Tests
        |
        v
Commit
        |
        v
Push to Remote
        |
        v
Create Pull Request
        |
        v
Review
        |
        v
Merge
```

Branching and pull requests allow changes to be reviewed and integrated systematically.

---

# 40. Engineering Decisions and Trade-offs

## Monolith vs Microservices

A modular application is simpler to develop and operate.

Separating services becomes useful when independent scaling, deployment, resource isolation, or ownership requirements justify the additional complexity.

Payment processing is a candidate for a dedicated service boundary because of its external integrations and reliability requirements.

## Lazy Loading vs Eager Loading

Lazy loading avoids unnecessary association retrieval.

Eager fetching can be appropriate when related data is consistently required.

Fetch joins provide query-specific control.

## Derived Queries vs JPQL

Derived queries reduce boilerplate for straightforward conditions.

JPQL provides explicit control for more complex entity queries.

## MySQL vs Redis

MySQL stores authoritative relational data.

Redis accelerates suitable repeated reads.

Cached data must remain consistent with persistent data.

## SQL Search vs Full-Text Search

Relational queries work well for structured filtering and simple matching.

Specialized search engines are better suited to large-scale relevance-oriented text search.

## Callbacks vs Reconciliation

Callbacks and webhooks provide timely asynchronous notifications.

Reconciliation repairs state discrepancies when notifications are missing or delayed.

## Payment Gateway Coupling

Provider-independent interfaces prevent payment business logic from depending directly on one gateway implementation.

## Cloud Public vs Private Networking

Public entry points are separated from private application and database resources.

Application-to-database traffic remains on private network paths.

---

# 41. Running the Application Locally

## Prerequisites

- Java 21
- MySQL
- Maven Wrapper
- Docker
- Redis

## Clone Repository

```bash
git clone https://github.com/DennyM55/commerce-backend.git
cd commerce-backend
```

## Create Database

```sql
CREATE DATABASE IF NOT EXISTS commerce_db;
```

## Start Supporting Services

```bash
docker compose up -d
```

## Run Application

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

```powershell
.\mvnw.cmd test
```

Environment-specific database, Redis, and payment-provider configuration is supplied through application configuration and environment variables.

---

# 42. Example API Requests

## Retrieve Products

```bash
curl "http://localhost:8080/api/products"
```

## Pagination

```bash
curl "http://localhost:8080/api/products?page=0&size=10"
```

## Sorting

```bash
curl "http://localhost:8080/api/products?sort=price,desc"
```

## Multi-Field Sorting

```bash
curl "http://localhost:8080/api/products?sort=name,asc&sort=price,asc"
```

## Searching

```bash
curl "http://localhost:8080/api/products/search?name=laptop"
```

## Combined Searching, Pagination, and Sorting

```bash
curl "http://localhost:8080/api/products/search?name=laptop&page=0&size=10&sort=price,asc"
```

## Price Filtering

```bash
curl "http://localhost:8080/api/products/above-price?price=5000"
```

---

# 43. Technical Competencies Demonstrated

## Java and Spring Engineering

Java 21, Spring Boot, Spring MVC, dependency injection, inversion of control, Spring-managed beans, configuration, controller/service/repository design, DTOs, validation, and exception handling.

## REST and Integration Engineering

HTTP, JSON, CRUD APIs, status codes, path variables, query parameters, `RestTemplate`, external API communication, SDK integrations, and Swagger/OpenAPI documentation.

## Relational Database Engineering

MySQL, SQL, JPA, Hibernate, entity mapping, relationship cardinalities, primary and foreign keys, inheritance mapping, UUIDs, derived queries, JPQL, lazy loading, fetch joins, query inspection, and database pagination.

## Payment and Reliability Engineering

Payment initiation, payment links, provider abstraction, Razorpay/Stripe-style gateway adapters, callbacks, webhooks, payment status polling, duplicate-event handling, idempotency, payment-state transitions, reconciliation, and scheduled jobs.

## Performance Engineering

N+1 query analysis, Hibernate fetching strategies, database-level pagination, sorting, searching, Redis caching, cache expiration, invalidation, and cache distribution concepts.

## Cloud Architecture

AWS Regions, Availability Zones, EC2, AMIs, instance types, SSH key pairs, EBS, RDS, VPC, subnets, CIDR, route tables, Internet Gateways, NAT Gateways, security groups, IAM, Application Load Balancers, Elastic Beanstalk, Route 53, and CloudWatch.

## Development and Testing

Maven, Git, GitHub, branching, merging, rebasing, forks, pull requests, Docker, Docker Compose, JUnit, Mockito, API testing, integration testing, logging, and troubleshooting.

---

# 44. Engineering Outcomes

Commerce Backend demonstrates an end-to-end understanding of backend application development:

1. Designing and exposing REST APIs.
2. Structuring Spring Boot applications using dependency injection.
3. Persisting and retrieving data through JPA and Hibernate.
4. Modeling relational database associations.
5. Identifying and optimizing inefficient queries.
6. Building paginated, searchable, and sortable APIs.
7. Integrating external systems behind well-defined interfaces.
8. Handling asynchronous payment notifications safely.
9. Recovering from missed payment events through reconciliation.
10. Running recurring background operations.
11. Reducing repeated database reads through Redis.
12. Running supporting infrastructure with Docker.
13. Testing application behavior and failure scenarios.
14. Explaining the network and infrastructure required to operate the application on AWS.
15. Managing code changes through Git collaboration workflows.

The central engineering principle is to maintain correctness while making the system easier to understand, extend, test, and operate.

---

**Java 21 · Spring Boot · Spring Data JPA · Hibernate · MySQL · Redis · REST APIs · Payment Engineering · AWS Architecture**

**Repository:** https://github.com/DennyM55/commerce-backend
