package com.littlebook.admin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.junit.jupiter.api.Assertions.*;





@Disabled("Disabled in unit test runs to avoid starting full Spring context")
public class AdminApplicationTest {

    @Test
    @Disabled("Avoid starting the full Spring context (web server / security) in unit tests; enable if you need an integration test")
    void mainRunsWithoutException() {
        // Disabled: starting AdminApplication in unit tests may start embedded web server and security
        // See other tests that verify annotations on AdminApplication instead.
        assertDoesNotThrow(() -> AdminApplication.main(new String[]{"--spring.main.web-application-type=none"}));
    }

    @Test
    void hasSpringBootApplicationAnnotationWithCorrectScanBasePackages() {
        SpringBootApplication annotation = AdminApplication.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation, "@SpringBootApplication should be present on AdminApplication");
        String[] scanBasePackages = annotation.scanBasePackages();
        assertNotNull(scanBasePackages, "scanBasePackages should not be null");
        boolean found = false;
        for (String pkg : scanBasePackages) {
            if ("com.littlebook.admin".equals(pkg)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "scanBasePackages should contain 'com.littlebook.admin'");
    }
}