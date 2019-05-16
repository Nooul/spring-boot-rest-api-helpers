package springboot.rest.controllerAdvices;

import lombok.NonNull;
import lombok.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;

//https://stackoverflow.com/a/40333275/986160
public class WrapperAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

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