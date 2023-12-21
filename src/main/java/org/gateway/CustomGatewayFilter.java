package org.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
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
        return isValidToken(authorizationHeader)
                .flatMap(isValid -> {
                    String requestPath = exchange.getRequest().getPath().toString();

                    // Allow requests with the "/auth/**" path to pass without filtering
                    if (requestPath.contains("/auth/")) {
                        return chain.filter(exchange);
                    }

                    // Continue with the filter chain if the token is valid
                    if (isValid) {
                        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                            // Log response headers here
                            exchange.getResponse().getHeaders().forEach((name, values) ->
                                    logger.info("Response Header {}: {}", name, values));
                        }));
                    }

                    // Return unauthorized response if the token is not valid
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(error -> {
                    // Handle errors, log the error for debugging purposes
                    logger.error("Error processing token validation: {}", error.getMessage());
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }


    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Boolean> isValidToken(String token) {
        String tokenValidationUrl = "http://localhost:8086/auth/validate";
        return WebClient.create(tokenValidationUrl)
                .get()
                .uri(uriBuilder -> uriBuilder.queryParam("token", token).build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    if (responseBody.equals("valid")) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                })
                .onErrorResume(e -> {
                    // Handle exceptions (e.g., communication errors)
                    // Log the error for debugging purposes
                    logger.error("Error validating token: {}", e.getMessage());
                    return Mono.just(false);
                });
    }


}
