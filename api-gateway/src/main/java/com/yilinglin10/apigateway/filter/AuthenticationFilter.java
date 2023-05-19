package com.yilinglin10.apigateway.filter;

import com.yilinglin10.apigateway.dto.ValidateTokenResponse;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;
    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER_NAME = "loggedInUser";
    private static final String USERNAME_HEADER_NAME = "username";
    private static final String USER_ROLES_HEADER_NAME = "roles";

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // bypass the secured endpoints
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                // check header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }

//                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
                    // truncate the authorization header prefix
                    authHeader = authHeader.substring(7);
                }
                try {
                    // REST call to Auth Service
                    String token = authHeader;

                    // Pass userId in request header["loggedInUser"]
                    // fix error according to https://github.com/spring-cloud/spring-cloud-gateway/issues/1090#issuecomment-499990656
                    return webClientBuilder.build().get()
                            .uri("http://auth-service/auth/validateToken", uriBuilder -> uriBuilder.queryParam("token", token).build())
                            .retrieve()
                            .bodyToMono(ValidateTokenResponse.class)
                            .flatMap(response -> {
                                ServerHttpRequest request = exchange.getRequest()
                                        .mutate()
                                        .header(USER_ID_HEADER_NAME, String.valueOf(response.getUserId()))
                                        .header(USERNAME_HEADER_NAME, response.getUsername())
                                        .header(USER_ROLES_HEADER_NAME, String.join(",", response.getAuthorities()))
                                        .build();
                                return chain.filter(exchange.mutate().request(request).build());
                            });
                } catch (Exception e) {
                    System.out.println("Invalid access...");
                    throw new RuntimeException("an unauthorized access to the application");
                }
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
