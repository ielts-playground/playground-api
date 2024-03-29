package org.ielts.playground.security.filter;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.ielts.playground.common.annotation.RequireClient;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.annotation.PermitAll;

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
     * @param <T>             the annotation type.
     * @param request         the request.
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
     *         with {@link PermitAll}; otherwise, {@code false}.
     * @see PermitAll
     */
    public boolean isAllPermitted(HttpServletRequest request) {
        return Objects.nonNull(this.retrieveHandlerAnnotation(
                request, PermitAll.class));
    }

    /**
     * Like the {@link #isAllPermitted(HttpServletRequest)} method
     * but for the administrated requests only.
     */
    public boolean isAdminRequired(HttpServletRequest request) {
        return Objects.nonNull(this.retrieveHandlerAnnotation(
                request, RequireAdmin.class));
    }

    /**
     * Retrieves all permitted clients for a private-resource request.
     *
     * @param request the request.
     * @return an array of permitted clients' names if the private resource is restricted
     * to a specific group of clients; otherwise, a {@code null} value will be returned
     * with a meaning of allowing all clients to access this resource.
     */
    @Nullable
    public String[] retrievePermittedClients(HttpServletRequest request) {
        return Optional.ofNullable(this.retrieveHandlerAnnotation(
                request, RequireClient.class)
        ).map(RequireClient::name).orElse(null);
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
