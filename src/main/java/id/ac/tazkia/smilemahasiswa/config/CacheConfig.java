package id.ac.tazkia.smilemahasiswa.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Konfigurasi Cache Manager
     * Menggunakan ConcurrentMapCacheManager untuk simplicity
     * Untuk production, bisa diganti dengan Redis atau EhCache
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // Definisikan nama-nama cache yang akan digunakan
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "detailTahunAkademik",
                "detailTahunAkademikByTahun",
                "feederIdRegistrasiDosen"));

        // Aktifkan dynamic cache creation
        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }
}
