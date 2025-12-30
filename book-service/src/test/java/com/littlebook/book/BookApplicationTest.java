package com.littlebook.book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;




@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Disabled("Avoid starting Spring context during unit test runs on developer machine")
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