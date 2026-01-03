package org.spacelab.housingutilitiessystemchairman.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;

import java.util.concurrent.Callable;
@Component
@Slf4j
public class SecurityContextCallableInterceptor implements CallableProcessingInterceptor {
    private static final String SECURITY_CONTEXT_ATTR = "SECURITY_CONTEXT";
    @Override
    public <T> void beforeConcurrentHandling(NativeWebRequest request, Callable<T> task) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            request.setAttribute(SECURITY_CONTEXT_ATTR, context, NativeWebRequest.SCOPE_REQUEST);
            log.debug("[{}] ✅ SecurityContext сохранен перед async: {}",
                    Thread.currentThread().getName(),
                    context.getAuthentication().getName());
        }
    }
    @Override
    public <T> void preProcess(NativeWebRequest request, Callable<T> task) {
        SecurityContext context = (SecurityContext) request.getAttribute(
                SECURITY_CONTEXT_ATTR, NativeWebRequest.SCOPE_REQUEST);
        if (context != null) {
            SecurityContextHolder.setContext(context);
            log.debug("[{}] ✅ SecurityContext восстановлен в async: {}",
                    Thread.currentThread().getName(),
                    context.getAuthentication().getName());
        }
    }
    @Override
    public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) {
        log.debug("[{}] ✅ Callable завершен", Thread.currentThread().getName());
    }
}
