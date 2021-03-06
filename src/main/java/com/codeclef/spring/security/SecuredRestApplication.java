package com.codeclef.spring.security;

import com.codeclef.spring.security.auth.basic.BasicAuthenticationSuccessHandler;
import com.codeclef.spring.security.auth.bearer.BearerTokenReactiveAuthenticationManager;
import com.codeclef.spring.security.auth.bearer.ServerHttpBearerAuthenticationConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@SpringBootApplication
@EnableWebFluxSecurity
public class SecuredRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecuredRestApplication.class, args);
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsRepository() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("user")
                .roles("USER", "ADMIN")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .authorizeExchange()
                .pathMatchers("/login", "/")
                .authenticated()
            .and()
                .addFilterAt(basicAuthenticationFilter(), SecurityWebFiltersOrder.HTTP_BASIC)
                   .authorizeExchange()
                .pathMatchers("/api/**")
                .authenticated()
            .and()
                .addFilterAt(bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    private AuthenticationWebFilter basicAuthenticationFilter(){
        UserDetailsRepositoryReactiveAuthenticationManager authManager;
        AuthenticationWebFilter basicAuthenticationFilter;
        ServerAuthenticationSuccessHandler successHandler;

        authManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsRepository());
        successHandler = new  BasicAuthenticationSuccessHandler();

        basicAuthenticationFilter = new AuthenticationWebFilter(authManager);
        basicAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        return basicAuthenticationFilter;

    }

    private AuthenticationWebFilter bearerAuthenticationFilter(){
        AuthenticationWebFilter bearerAuthenticationFilter;
        Function<ServerWebExchange, Mono<Authentication>> bearerConverter;
        ReactiveAuthenticationManager authManager;

        authManager  = new BearerTokenReactiveAuthenticationManager();
        bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
        bearerConverter = new ServerHttpBearerAuthenticationConverter();

        bearerAuthenticationFilter.setAuthenticationConverter(bearerConverter);
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));

        return bearerAuthenticationFilter;
    }
}
