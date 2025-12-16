package com.littlebook.user;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }

    @Test
    void annotatedWithSpringBootApplication() {
        SpringBootApplication annotation = UserApplication.class.getAnnotation(SpringBootApplication.class);
        Assertions.assertNotNull(annotation, "UserApplication should be annotated with @SpringBootApplication");
        String[] packages = annotation.scanBasePackages();
        Assertions.assertArrayEquals(new String[] {"com.littlebook.user"}, packages, "scanBasePackages should contain the application package");
    }

    @Test
    void mainRunsWithoutThrowing() {
        Assertions.assertDoesNotThrow(() -> UserApplication.main(new String[0]));
    }
}
