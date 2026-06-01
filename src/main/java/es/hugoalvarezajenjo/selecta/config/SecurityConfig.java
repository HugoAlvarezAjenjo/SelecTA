package es.hugoalvarezajenjo.selecta.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RoleBasedSuccessHandler roleBasedSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF habilitado globalmente. Thymeleaf inyecta el token automáticamente en <form>.
                // Solo se exime a las rutas de API REST puras (consumidas por fetch/AJAX desde JS).
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/enrollment-list/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y páginas públicas
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/js/**",
                                "/css/**",
                                "/error/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Vistas de lectura pública (cualquiera puede ver asignaturas y recomendador)
                        .requestMatchers(
                                "/subjects",
                                "/subject/{id}",
                                "/recommender",
                                "/api/resources/*/download",
                                "/user/{id}"
                        ).permitAll()
                        // Panel de administración
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(roleBasedSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
