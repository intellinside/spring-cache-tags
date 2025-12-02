# Spring Cache Tags

[![Build & Deploy](https://github.com/intellinside/spring-cache-tags/actions/workflows/release.yml/badge.svg)](https://github.com/intellinside/spring-cache-tags/actions/workflows/release.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.intellinside/spring-cache-tags.svg?style=flat-square)](https://search.maven.org/artifact/io.github.intellinside/spring-cache-tags)
[![javadoc](https://javadoc.io/badge2/io.github.intellinside/spring-cache-tags/javadoc.svg)](https://javadoc.io/doc/io.github.intellinside/spring-cache-tags)


**Spring Cache Tags** is a lightweight library for managing Spring Cache using tags.  
It allows you to group cached methods by tags and evict cache entries by one or multiple tags at once.

## Features

- 🏷️ Tag your cacheable methods with `@CacheTags` and evict tagged cache entries with `@EvictTags`
- 🎯 Support for Spring Expression Language (SpEL) in tags for dynamic tag generation
- 🔄 Transparent integration with Spring Cache - no code changes needed
- 💾 Multiple storage backends: In-Memory and Redis
- 🚀 Works with Spring Boot and standard Spring Framework
- 🔒 Thread-safe operations with concurrent access support
- 📦 Automatic Spring Boot auto-configuration

## Quick Start

### Installation

#### Maven
```xml
<dependency>
    <groupId>io.github.intellinside</groupId>
    <artifactId>spring-boot-starter-cache-tags</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Gradle
```groovy
implementation 'io.github.intellinside:spring-boot-starter-cache-tags:0.1.0'
```

### Basic Example

```java
@Service
public class UserService {

    @Cacheable("users")
    @CacheTags("'user:' + #id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = "users", key = "#id")
    @EvictTags("'user:' + #id")
    public void updateUser(Long id, UserDTO dto) {
        userRepository.save(convertToEntity(dto));
    }
}
```

## Core Concepts

### Tags with @CacheTags

The `@CacheTags` annotation is used to associate one or more tags with cached method results. Tags support SpEL expressions.

**Supported variables in SpEL:**
- `#id`, `#name`, etc. - method parameter values (requires `-parameters` compiler flag)
- `#result` - method return value
- `#args` - array of all method arguments
- `#method` - method metadata

**Example:**
```java
@Cacheable("products")
@CacheTags({
    "'product:' + #productId",
    "'category:' + #result.category",
    "'price-range:' + #result.priceRange"
})
public Product getProduct(Long productId) {
    return productRepository.findById(productId).orElse(null);
}
```

### Cache Eviction with @EvictTags

The `@EvictTags` annotation evicts all cache entries associated with specified tags. Eviction happens after successful method execution.

**Example:**
```java
@EvictTags({
    "'product:' + #productId",
    "'category:' + #category",
    "'price-range:' + #result.priceRange"
})
public Product updateProduct(Long productId, String category) {
    // Update product in database
    return productRepository.updateCategory(productId, category);
}
```

## Storage Backends

### In-Memory Storage (Default)

The default implementation stores tag mappings in application memory using `ConcurrentHashMap`.

**Advantages:**
- No external dependencies
- Very fast access
- Simple setup

**Disadvantages:**
- Mappings are lost on application restart
- Not suitable for distributed systems
- Memory usage grows with cache size


### Redis Storage

For distributed systems and production environments, use Redis as the storage backend.

**Setup:**
1. Add Spring Data Redis to your project:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. Configure Redis in your `application.properties`:
```properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

**Advantages:**
- Distributed tag mappings
- Persistent storage
- Shared across multiple instances
- High performance

**Data Structure:**
- Keys: `tag:{tagName}` (Redis Set)
- Values: `{cacheName}:{key}` (serialized cache references)

Example Redis structure:
```
SET tag:user:123 users:user:123
SET tag:user:123 users:permissions:123
SET tag:admin admins:settings:456
```

## Advanced Usage

### Complex Tag Expressions

Use SpEL to create sophisticated tag expressions:

```java
@Service
public class OrderService {

    @Cacheable("orders")
    @CacheTags({
        "'order:' + #orderId",
        "'customer:' + #customerId",
        "'status:' + #result.status"
    })
    public Order getOrder(Long orderId, Long customerId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @EvictTags({
        "'order:' + #orderId",
        "'customer:' + #customerId"
    })
    public void cancelOrder(Long orderId, Long customerId) {
        orderRepository.updateStatus(orderId, "CANCELLED");
    }
}
```

### Bulk Eviction by Pattern

Evict multiple entries with a single tag:

```java
@Service
public class ProductService {

    @EvictTags("'category:' + #categoryId")
    public void updateCategory(Long categoryId, CategoryDTO dto) {
        // All products in this category are evicted
        categoryRepository.update(categoryId, dto);
    }
}
```

### Dynamic Tag Generation

Generate tags based on result properties:

```java
@Cacheable("users")
@CacheTags({
    "'user:' + #id",
    "'role:' + #result.role",
    "'department:' + #result.department"
})
public User getUserWithRoleAndDepartment(Long id) {
    return userRepository.findByIdWithRelations(id).orElse(null);
}
```

## Configuration

### Spring Boot Auto-Configuration

The library automatically configures itself with Spring Boot. No additional configuration is needed for basic functionality.

## Performance Considerations

### In-Memory Storage
- **Best for**: Single-instance applications, development
- **Memory overhead**: Minimal - one entry per cached item
- **Access time**: O(1) average case

### Redis Storage
- **Best for**: Distributed systems, production
- **Network overhead**: Minimal with pipelining
- **Persistence**: Data survives application restarts

### Tips for Optimal Performance
1. Keep tag expressions simple and lightweight
2. Use specific tags rather than broad patterns
3. Consider cache key design to minimize tag entries
4. Monitor memory usage in in-memory mode
5. Use Redis for production distributed systems

## Troubleshooting

### Tags are not being recorded

**Check:**
1. Ensure `-parameters` compiler flag is set (for named parameter access)
2. Verify that `@CacheTags` is used together with `@Cacheable`
3. Check that `@CacheTags` is on the same method as `@Cacheable`
4. Enable debug logging: `logging.level.io.github.intellinside.cache.tags=DEBUG`

### Cache is not being evicted

**Check:**
1. Ensure `@EvictTags` method completes successfully (exceptions prevent eviction)
2. Verify that the evaluated tag matches the tags used in `@CacheTags`
3. Check that cache manager is properly configured
4. Verify Redis connection (if using Redis backend)

### Memory usage is growing

**Solutions:**
1. Review tag cardinality - ensure tags aren't too unique
2. Consider using Redis backend for better memory management
3. Monitor and clean up unused cache entries
4. Review SpEL expressions for correctness

## Requirements

- **Java**: 21 or higher
- **Spring Framework**: 6.x
- **Spring Boot**: 3.x (optional, for auto-configuration)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Support & Documentation

- 📖 [API Documentation](https://javadoc.io/doc/io.github.intellinside/spring-cache-tags)
- 🐛 [Issue Tracker](https://github.com/intellinside/spring-cache-tags/issues)

## Changelog

### Version 0.1.0
- Initial release
- Support for @CacheTags and @EvictTags annotations
- In-Memory and Redis storage backends
- Full Spring Boot auto-configuration
- Complete JavaDoc documentation
