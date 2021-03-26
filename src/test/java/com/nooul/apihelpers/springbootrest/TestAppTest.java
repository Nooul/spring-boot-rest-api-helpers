package com.nooul.apihelpers.springbootrest;

import com.nooul.apihelpers.springbootrest.testapp.TestApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = { TestApp.class })
public class TestAppTest {

    @Test
    public void contextLoads() {
    }

}
