package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ Tắt CSRF vì dùng JWT
            .csrf(AbstractHttpConfigurer::disable)

            // ✅ Bật CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ✅ Stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ Phân quyền API
            .authorizeHttpRequests(auth -> auth

                // ⚠️ BẮT BUỘC: Cho OPTIONS (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ===== PUBLIC API =====
                .requestMatchers(
                        "/api/auth/**",
                        "/api/payment/**",
                        "/images/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/courts/**",
                        "/api/categories/**",
                        "/api/reviews/**",
                        "/api/bookings/check-availability"
                ).permitAll()

                // ===== ADMIN =====
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/bookings/all").hasRole("ADMIN")
                .requestMatchers("/api/bookings/*/confirm").hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/courts/**",
                        "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/courts/**",
                        "/api/categories/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/courts/**",
                        "/api/categories/**"
                ).hasRole("ADMIN")

                // ===== USER / AUTH =====
                .requestMatchers("/api/bookings/*/cancel").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/bookings/**").authenticated()
                .requestMatchers("/api/reviews/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()

                // ===== DEFAULT =====
                .anyRequest().authenticated()
            )

            // ✅ Authentication provider
            .authenticationProvider(authenticationProvider)

            // ✅ JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================
    // ===== CORS CONFIG =======
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Local + Vercel (kể cả preview)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "https://*.vercel.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "x-auth-token"
        ));

        // 👇 Để frontend đọc được Authorization header
        configuration.setExposedHeaders(List.of("Authorization"));

        // ⚠️ Bắt buộc TRUE khi dùng JWT + domain cố định
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
