package com.littlebook.book;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;




@org.springframework.boot.test.context.SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }

    @Test 
    void main() {
        // Test the main method but avoid binding to the default port by using server.port=0
        BookApplication.main(new String[]{"--server.port=0"});
    }

}