package vn.com.lifesup.base.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CacheStats {
    private final String cacheName;
    private final long size;
    private final long ttl;
}
