package com.littlebook.user;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD




class UserApplicationTest {

    @Test
=======
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }

    @Test
>>>>>>> d0efca2f7b4792bd4f1e519a36c86f3a1165f275
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
<<<<<<< HEAD
}
=======
}
>>>>>>> d0efca2f7b4792bd4f1e519a36c86f3a1165f275
