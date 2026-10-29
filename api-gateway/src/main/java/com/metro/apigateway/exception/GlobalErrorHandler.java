package com.metro.apigateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;


import java.util.Map;

@Configuration
public class GlobalErrorHandler extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable error = getError(request);

        if (isServiceUnavailable(error)) {
            errorAttributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorAttributes.put("error", "Service Unavailable");
            errorAttributes.put("message", "The requested service is currently unavailable. Please try again later.");
        }

        return errorAttributes;
    }

    private boolean isServiceUnavailable(Throwable error) {
        String errorClassName = error.getClass().getSimpleName();
        return errorClassName.contains("NotFoundException")
                || errorClassName.contains("ConnectException")
                || errorClassName.contains("TimeoutException");
    }
}