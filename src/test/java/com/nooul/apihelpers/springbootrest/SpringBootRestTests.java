package com.nooul.apihelpers.springbootrest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {TestSpringBootApp.class})
public class SpringBootRestTests {

    @Test
    public void contextLoads() {
    }



}
