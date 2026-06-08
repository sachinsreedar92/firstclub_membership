package com.firstclub.membership;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MembershipApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full application context (JPA, caching, async, resilience, web) wires up.
    }
}
