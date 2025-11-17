package com.littlebook.book;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;




@SpringBootTest
class BookApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }

    @Test 
    void main() {
        // Test the main method by calling it with null args
        BookApplication.main(new String[]{});
    }

}