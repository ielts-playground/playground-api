package org.ielts.playground.security.filter;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.ielts.playground.common.annotation.AdminPermitted;
import org.ielts.playground.common.annotation.Permitted;

@Component
public class ServletRequestChecker {

    private final ApplicationContext applicationContext;

    public ServletRequestChecker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Tries to retrieve the annotation associated with the {@link HandlerMethod}
     * of a specific {@link HttpServletRequest}.
     *
     * @param <T> the annotation type.
     * @param request the request.
     * @param annotationClass the annotation's class.
     * @return the annotation if it exists; otherwise, {@code null}.
     */
    @Nullable
    private <T extends Annotation> T retrieveHandlerAnnotation(
            @NotNull HttpServletRequest request,
            @NotNull Class<T> annotationClass) {
        for (Map.Entry<String, HandlerMapping> entry : this.applicationContext
                .getBeansOfType(HandlerMapping.class).entrySet()) {
            try {
                HandlerExecutionChain executionChain = entry.getValue().getHandler(request);
                T annotation = Optional.ofNullable(executionChain)
                        .map(HandlerExecutionChain::getHandler)
                        .map(HandlerMethod.class::cast)
                        .map(HandlerMethod::getMethod)
                        .map(method -> method.getAnnotation(annotationClass))
                        .orElse(null);

                if (annotation != null) {
                    return annotation;
                }
            } catch (Exception ex) {
                // don't care!
            }
        }
        return null;
    }

    /**
     * Checks if an {@link HttpServletRequest} is publicly permitted or not.
     *
     * @param request the request.
     * @return {@code true} if the request is handled from a method annotated
     *         with {@link Permitted}; otherwise, {@code false}.
     * @throws Exception when something wrong occurs.
     * @see Permitted
     */
    public boolean isAllPermitted(HttpServletRequest request) {
        return !Objects.isNull(this.retrieveHandlerAnnotation(
                request, Permitted.class));
    }

    /**
     * Like the {@link #isAdminPermitted(HttpServletRequest)} method
     * but for the administrated requests only.
     */
    public boolean isAdminPermitted(HttpServletRequest request) {
        return !Objects.isNull(this.retrieveHandlerAnnotation(
                request, AdminPermitted.class));
    }

    /**
     * Retrieves the {@code Authentication} from an {@link HttpServletRequest}.
     *
     * @param request the request.
     */
    public Authentication retrievePublicPermission(HttpServletRequest request) {
        if (this.isAllPermitted(request)) {
            return new GuestAuthenticationToken();
        }
        return null;
    }
}
