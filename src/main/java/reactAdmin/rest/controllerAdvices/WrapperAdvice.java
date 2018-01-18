package reactAdmin.rest.controllerAdvices;

import lombok.Data;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;

//https://stackoverflow.com/a/40333275/986160
@ControllerAdvice
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
                (body instanceof byte[]) ||
                (body instanceof LinkedHashMap && ((LinkedHashMap)(body)).containsKey("exception"))
            ))
        {
            return page(body);
        }
        return body;
    }


    public static <T> Page<T> page(T obj) {
        if (obj == null) {
            return new PageImpl<>(new ArrayList<>());
        }
        return new PageImpl<>(new ArrayList<>(Arrays.asList(obj)));
    }

    public static <T> Page<T> pageList(List<T> list) {
        if (list == null) {
            return new PageImpl<>(new ArrayList<>());
        }
        return new PageImpl<>(new ArrayList<>(list));
    }

    public static <T> Page<T> pagePage(Page page) {
        if (page == null) {
            return new PageImpl<>(new ArrayList<>());
        }
        return page;
    }
}