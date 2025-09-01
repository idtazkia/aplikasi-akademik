# Spring Cache Implementation untuk DetailTahunAkademik

## Overview

Implementasi caching untuk menghindari query berulang ketika mencari `DetailTahunAkademik` yang berlaku untuk mahasiswa tertentu.

## Files yang Ditambahkan/Diubah

### 1. Dependencies (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 2. Main Application (`SmilemahasiswaApplication.java`)

-   Ditambahkan `@EnableCaching` annotation

### 3. Service Layer (`DetailTahunAkademikService.java`)

Service dengan cache annotations:

-   `@Cacheable` - untuk cache hasil query
-   `@CacheEvict` - untuk clear cache ketika data berubah

### 4. Cache Configuration (`CacheConfig.java`)

Konfigurasi cache manager dengan dua cache:

-   `detailTahunAkademik` - cache untuk data detail per mahasiswa
-   `detailTahunAkademikByTahun` - cache untuk data per tahun akademik

### 5. Application Properties

```properties
spring.cache.type=simple
spring.cache.cache-names=detailTahunAkademik,detailTahunAkademikByTahun
logging.level.org.springframework.cache=DEBUG
```

## Cara Penggunaan

### 1. Inject Service di Controller/Component

```java
@Autowired
private DetailTahunAkademikService detailTahunAkademikService;
```

### 2. Panggil Method untuk Mendapatkan Data (Akan di-cache otomatis)

```java
DetailTahunAkademik detail = detailTahunAkademikService
    .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
```

### 3. Clear Cache Ketika Ada Update Data

```java
// Clear semua cache
detailTahunAkademikService.clearAllCache();

// Clear cache untuk mahasiswa tertentu
detailTahunAkademikService.clearCacheForMahasiswa(tahunAkademik, mahasiswa);
```

## Cache Keys

Cache key dibuat berdasarkan:

-   `tahunAkademik.id`
-   `mahasiswa.idProdi.id`
-   `mahasiswa.idProgram.id`
-   `mahasiswa.angkatan`

Format: `"ta-2025_prodi-1_program-1_2023"`

## Benefits

1. **Performance**: Mengurangi query database untuk data yang sama
2. **Scalability**: Aplikasi bisa handle lebih banyak concurrent users
3. **Resource Optimization**: Mengurangi beban database server

## Cache Invalidation Strategy

-   Data di-cache sampai ada perubahan data DetailTahunAkademik
-   Auto clear cache ketika save/delete DetailTahunAkademik
-   Manual clear cache available melalui service methods

## Production Considerations

Untuk production, pertimbangkan:

1. **Redis Cache**: Ganti `ConcurrentMapCacheManager` dengan `RedisCacheManager`
2. **TTL Configuration**: Set time-to-live untuk cache entries
3. **Cache Size Limits**: Set maksimum cache size
4. **Distributed Cache**: Untuk multiple server instances

### Contoh Redis Configuration

```java
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1)) // Cache expire dalam 1 jam
        .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .build();
}
```

## Testing

Unit test disediakan di `DetailTahunAkademikServiceTest.java` untuk memverifikasi:

-   Cache behavior
-   Data retrieval accuracy
-   Cache eviction functionality
