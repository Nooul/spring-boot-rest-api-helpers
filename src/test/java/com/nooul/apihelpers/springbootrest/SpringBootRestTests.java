package com.nooul.apihelpers.springbootrest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {TestSpringBootApp.class})
public class SpringBootRestTests {

    @Test
    public void contextLoads() {
    }



}
