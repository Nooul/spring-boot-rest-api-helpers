package reactAdmin.rest.controllerAdvices;

import lombok.Data;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

//https://stackoverflow.com/a/40333275/986160
public class WrapperAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (! ( (body instanceof List) ||
                (body instanceof Set) ||
                (body instanceof Page) ||
                (body instanceof InputStreamResource) ||
                (body instanceof LinkedHashMap && ((LinkedHashMap)(body)).containsKey("exception"))
            ))
        {
            return new SingleObjectWrapper<>(body);
        }
        else if (body instanceof List) {
            return new ListObjectWrapper((List)body);
        }
        return body;
    }


    @Data
    class SingleObjectWrapper<T> {
        private final List<T> content = new ArrayList<>();

        public SingleObjectWrapper(T obj) {
            this.content.add(obj);
        }
    }

    @Data
    class ListObjectWrapper<T> {
        private List<T> content = new ArrayList<>();

        public ListObjectWrapper(final List<T> obj) {
            this.content = obj;
        }
    }
}