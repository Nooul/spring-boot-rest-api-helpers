package com.nooul.apihelpers.springbootrest.testapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@Configuration
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestAppConfiguration {
}
