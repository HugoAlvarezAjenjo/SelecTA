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
                // CSRF habilitado por defecto (HttpSessionCsrfTokenRepository).
                // Thymeleaf inyecta el token como campo oculto _csrf en todos los <form>.
                // Para AJAX: el token se expone en un meta tag que JS puede leer.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // REST endpoints usan autenticación por sesión pero envían token vía header
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y páginas públicas
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/js/**",
                                "/css/**",
                                "/error/**"
                        ).permitAll()
                        // Vistas de lectura pública (cualquiera puede ver asignaturas)
                        .requestMatchers(
                                "/subjects",
                                "/subject/{id}"
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
