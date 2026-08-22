package by.delmark.portal.labor_cost_bot.storage;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CacheStorage {

    private final CaffeineCacheManager cacheManager;
    
    @Value("${spring.cache.caffeine.spec}")
    private String caffeineSpec;

    public <T> int putExternalIdIfAbsent(String cacheName, T externalId) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            var caffeineCache = Caffeine.from(caffeineSpec).build();
            cacheManager.registerCustomCache(cacheName, caffeineCache);
            cache = cacheManager.getCache(cacheName);
        }
        int externalIdHash = externalId.hashCode();
        assert cache != null;
        cache.putIfAbsent(externalIdHash, externalId);
        return externalIdHash;
    }

    public <T> T getExternalId(String cacheName, int hash) {
        return Optional.ofNullable(cacheManager.getCache(cacheName))
                .map(cache -> (T) cache.get(hash))
                .orElse(null);
    }
}
