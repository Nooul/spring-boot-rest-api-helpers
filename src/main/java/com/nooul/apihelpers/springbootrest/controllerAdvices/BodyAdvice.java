package com.nooul.apihelpers.springbootrest.controllerAdvices;

import lombok.NonNull;
import lombok.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.nooul.apihelpers.springbootrest.exceptions.NotFoundException;

import java.util.Arrays;

//https://stackoverflow.com/a/40333275/986160
//https://stackoverflow.com/a/59294075/986160
//extend them and add @ControllerAdvice
public class BodyAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) {
            throw new NotFoundException("Resource was not found!");
        }
        if (isArray(body)) {
            return new Wrapper(Arrays.asList(body));
        }
        if (body instanceof Iterable && !(body instanceof Page)) {
            return new Wrapper((Iterable)body);
        }
        return body;
    }

    @Value
    private class Wrapper {
        private final @NonNull Iterable content;

    }

    public static boolean isArray(Object obj)
    {
        return obj != null && (obj.getClass().isArray() || obj instanceof Iterable) && !(obj instanceof byte[]);
    }
}