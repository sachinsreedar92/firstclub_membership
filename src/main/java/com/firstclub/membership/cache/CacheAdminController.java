package com.firstclub.membership.cache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cache")
@Tag(name = "Cache", description = "Reload configuration caches without a restart")
public class CacheAdminController {

    private final CacheReloadService cacheReloadService;

    public CacheAdminController(CacheReloadService cacheReloadService) {
        this.cacheReloadService = cacheReloadService;
    }

    @GetMapping("/names")
    @Operation(summary = "List reloadable cache names")
    public List<String> names() {
        return cacheReloadService.cacheNames();
    }

    @PostMapping("/reload")
    @Operation(summary = "Evict and re-warm a single named cache")
    public Map<String, String> reload(@RequestParam String name) {
        cacheReloadService.reload(name);
        return Map.of("status", "reloaded", "cache", name);
    }

    @PostMapping("/reload-all")
    @Operation(summary = "Evict and re-warm all configuration caches")
    public Map<String, String> reloadAll() {
        cacheReloadService.reloadAll();
        return Map.of("status", "reloaded", "cache", "ALL");
    }
}
