package com.nooul.apihelpers.springbootrest.services;

import com.nooul.apihelpers.springbootrest.AbstractJpaDataTest;
import com.nooul.apihelpers.springbootrest.specifications.CustomSpecifications;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import({FilterService.class, CustomSpecifications.class})
class FilterServiceTest extends AbstractJpaDataTest {

    private FilterService filterService;

    @BeforeEach
    void init() {
        filterService = new FilterService();
    }

    @Test
    void given_a_snake_case_string_conversion_should_succeed() {
        String snakeCase = "this_is_a_snake_case";
        String camelCase = filterService.convertToCamelCase(snakeCase);
        Assertions.assertEquals("thisIsASnakeCase", camelCase);
    }

}