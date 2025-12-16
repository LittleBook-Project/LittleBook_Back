package com.littlebook.user;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.autoconfigure.SpringBootApplication;




class UserApplicationTest {

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