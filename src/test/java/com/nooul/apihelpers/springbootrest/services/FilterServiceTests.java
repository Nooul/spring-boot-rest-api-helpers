package com.nooul.apihelpers.springbootrest.services;

import com.nooul.apihelpers.springbootrest.AbstractSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class FilterServiceTests extends AbstractSpringBootTest {

    private FilterService filterService;

    @BeforeEach
    void init(){
        filterService = new FilterService();
    }

    @Test
    void given_a_snake_case_string_conversion_should_succeed() {
        String snakeCase = "this_is_a_snake_case";
        String camelCase = filterService.convertToCamelCase(snakeCase);
        Assertions.assertEquals("thisIsASnakeCase", camelCase);
    }

}