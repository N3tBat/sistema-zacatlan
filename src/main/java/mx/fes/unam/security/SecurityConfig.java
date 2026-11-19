package mx.fes.unam.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Configuración de seguridad del cliente.
 * El login se realiza mediante POST en controlador propio que consume /api/auth/login.
 * La sesión HTTP guarda el accessToken y el SecurityContext con username + ROLE_*.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Sin usuarios locales: la autenticación se delega al backend vía API.
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/bootstrap/**", "/acceso-denegado").permitAll()
                .requestMatchers("/bloque/**", "/vendedor/**", "/producto/**", "/reporte/**").hasRole("ADMIN")
                .requestMatchers("/venta/modificar/**").hasRole("ADMIN")
                .requestMatchers("/venta/**").hasAnyRole("ADMIN", "VENDEDOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedPage("/acceso-denegado")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
