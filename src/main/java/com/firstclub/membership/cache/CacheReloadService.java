package com.firstclub.membership.cache;

import java.util.List;

/**
 * Operational support for refreshing configuration caches without a restart. A reload evicts the
 * cache and then re-populates it ("warms") by invoking the owning service, so the next request path
 * stays fast. Useful after bulk data changes to plans/tiers/benefits/rules.
 */

public interface CacheReloadService {

    List<String> cacheNames();
    void reload(String name);
    void reloadAll();
}
