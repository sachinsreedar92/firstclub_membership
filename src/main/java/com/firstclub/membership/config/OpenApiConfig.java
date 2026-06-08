package com.firstclub.membership.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Single unified Swagger UI group.  All domains (Plans, Tiers, Benefits, Subscriptions, Members,
 * Discounts, Deals, Cache, Events) are collected from the @Tag annotations on each controller and
 * displayed in the order declared in TAG_ORDER below.
 */
@Configuration
public class OpenApiConfig {

    private static final Map<String, Integer> TAG_ORDER = Map.of(
            "Plans",         0,
            "Tiers",         1,
            "Benefits",      2,
            "Subscriptions", 3,
            "Members",       4,
            "Discounts",     5,
            "Deals",         6,
            "Cache",         7,
            "Events",        8);

    @Bean
    public OpenAPI membershipOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FirstClub Membership Program API")
                .version("v1")
                .description("Tiered membership program: plans, tiers, configurable benefits, "
                        + "subscription lifecycle, discount eligibility, exclusive deals, "
                        + "cache management and event-driven tier progression.")
                .license(new License().name("Apache 2.0")));
    }

    /**
     * Sort Swagger UI tags by the declared order; unknown tags fall to the end alphabetically.
     */
    @Bean
    public GlobalOpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) return;
            List<Tag> sorted = openApi.getTags().stream()
                    .sorted(Comparator.comparingInt(
                            t -> TAG_ORDER.getOrDefault(t.getName(), Integer.MAX_VALUE)))
                    .toList();
            openApi.setTags(sorted);
        };
    }
}
