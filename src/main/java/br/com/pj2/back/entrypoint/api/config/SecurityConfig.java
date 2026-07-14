package br.com.pj2.back.entrypoint.api.config;

import br.com.pj2.back.core.domain.enumerated.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
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
    private final AuthenticationProvider authenticationProvider;
    private final AuthFilter authFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth ->
                        auth
                                // Public Routes
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs.yaml").permitAll()

                                // Teacher Routes
                                .requestMatchers(HttpMethod.GET, "/monitoring/schedules/filter**").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.PATCH,"/monitoring/schedules/{id}/approve").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.PATCH,"/monitoring/schedules/{id}/deny").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.POST, "/monitoring").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.PUT, "/monitoring/{id}").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.GET, "/monitoring/teacher").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.GET, "/monitoring/{id}").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.DELETE, "/monitoring/{id}").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.GET, "/monitoring/teacher/details").hasAnyRole(Role.TEACHER.name())
                                .requestMatchers(HttpMethod.DELETE, "/monitoring/{monitoringId}/students/{studentRegistration}").hasAnyRole(Role.TEACHER.name())



                                // Student Routes
                                .requestMatchers(HttpMethod.POST, "/monitoring/schedules").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers(HttpMethod.GET, "/monitoring/schedules/me").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers(HttpMethod.GET, "/monitoring/schedules/{id}").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers("/monitoring/sessions/started").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers("/monitoring/sessions/start").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers("/monitoring/sessions/finish").hasAnyRole(Role.STUDENT.name())
                                .requestMatchers("/pdf/month-workloads").hasAnyRole(Role.STUDENT.name())

                                .anyRequest().authenticated()
                ).authenticationProvider(authenticationProvider)
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.addExposedHeader(HttpHeaders.CONTENT_DISPOSITION);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
