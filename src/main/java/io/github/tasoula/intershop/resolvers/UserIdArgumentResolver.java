package io.github.tasoula.intershop.resolvers;

import io.github.tasoula.intershop.annotations.UserId;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

import java.util.NoSuchElementException;
import java.util.UUID;

public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final String cookieName;

    public UserIdArgumentResolver(String cookieName) {
        this.cookieName = cookieName;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class) &&
                parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String userIdString =  (String) request.getAttribute(cookieName);

        if (userIdString == null || userIdString.isEmpty()) {
            throw new NoSuchElementException("userId is undefined");
        }

        return UUID.fromString(userIdString);
    }
}
