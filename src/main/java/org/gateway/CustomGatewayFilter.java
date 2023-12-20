package org.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(CustomGatewayFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log headers here
        exchange.getRequest().getHeaders().forEach((name, values) ->
                logger.info("Request Header {}: {}", name, values));
        // Log the full URL
        logger.info("Request URL: {}", exchange.getRequest().getURI());

        // Get the Authorization header from the incoming request
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Perform Auth0 token validation logic here
        boolean isValid = isValidToken(authorizationHeader);

        String requestPath = exchange.getRequest().getPath().toString();
        // Allow requests with the "/auth/**" path to pass without filtering
        if (requestPath.startsWith("/microadmin/auth/")) {
            logger.info("Allow requests with the \"/auth/**\" path to pass without filtering");
            return chain.filter(exchange);
        }
        if (isValid) {
            // Continue with the filter chain if the token is valid
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // Log response headers here
                exchange.getResponse().getHeaders().forEach((name, values) ->
                        logger.info("Response Header {}: {}", name, values));
            }));
        } else {
            // Return unauthorized response if the token is not valid
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isValidToken(String token) {
        // Implement Auth0 token validation logic here
        // You may use Auth0 SDK or make an API call to Auth0 for validation
        // Return true if the token is valid, false otherwise
        return true;  // For illustration purposes, replace with actual validation logic
    }
}
