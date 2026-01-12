package com.littlebook.notification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;




@SpringBootTest
class NotificationApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }

    @Test 
    void main() {
        // Test the main method by calling it with null args
        NotificationApplication.main(new String[]{});
    }

}