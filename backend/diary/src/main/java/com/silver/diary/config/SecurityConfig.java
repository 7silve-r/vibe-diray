package com.silver.diary.config;

import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.UserService;
import com.silver.diary.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetails(
            UserService users) {
        return username -> {
            var user =
                    users.lambdaQuery()
                            .eq(com.silver.diary.entity.User::getUsername, username)
                            .one();
            if (user == null)
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "账号不存在");
            return org.springframework.security.core.userdetails.User.withUsername(
                            user.getUsername())
                    .password(user.getPassword())
                    .roles("USER", "ADMIN".equals(user.getRole()) ? "ADMIN" : "USER")
                    .disabled(!Integer.valueOf(0).equals(user.getStatus()))
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain security(
            HttpSecurity http,
            JwtUtil jwt,
            UserService users,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            @Value("${file.access-url-prefix}") String filePrefix)
            throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/error",
                                                "/api/reg",
                                                "/api/login",
                                                "/api/email/code",
                                                "/api/password/reset",
                                                "/public/**",
                                                "/music/public/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, filePrefix + "**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (req, res, ex) ->
                                                        resolver.resolveException(
                                                                req,
                                                                res,
                                                                null,
                                                                new BusinessException(
                                                                        401, "请先登录再操作")))
                                        .accessDeniedHandler(
                                                (req, res, ex) ->
                                                        resolver.resolveException(
                                                                req,
                                                                res,
                                                                null,
                                                                new BusinessException(
                                                                        403, "没有权限执行此操作"))))
                .addFilterBefore(
                        new JwtFilter(jwt, users, resolver),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
