package com.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Confirms the full Spring context (security, JPA, WebSocket, JWT beans)
 * wires up cleanly. Run with `mvn test`. Uses the "dev" profile (H2
 * in-memory) so it needs no external database or Docker.
 */
@SpringBootTest
@ActiveProfiles("dev")
class CodeArenaApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context fails to start, this test fails —
        // that alone catches most wiring mistakes (missing beans,
        // bad property bindings, security config errors).
    }
}
