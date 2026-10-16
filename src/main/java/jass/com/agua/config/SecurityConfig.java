package jass.com.agua.config;

import jass.com.agua.entity.Rol;
import jass.com.agua.entity.UsuarioRol;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final FiltroTokenMovil filtroTokenMovil;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var u = usuarioRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            List<String> roles = usuarioRolRepository.buscarRolesConRol(u.getIdUsuario()).stream()
                    .map(UsuarioRol::getRol).map(Rol::getNombre).map(String::toUpperCase).toList();
            return User.withUsername(u.getUsername()).password(u.getPassword())
                    .roles(roles.toArray(String[]::new))
                    .disabled(!"ACTIVO".equalsIgnoreCase(u.getEstado())).build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationProvider authenticationProvider) throws Exception {
        http.authenticationProvider(authenticationProvider)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/movil/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/movil/login").permitAll()
                        .requestMatchers("/api/movil/**").hasRole("OPERADOR")
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**").permitAll()
                        // Se conserva la excepción temporal que ya tenía el proyecto.
                        .requestMatchers("/seguridad/**").hasAnyRole("PRESIDENTA", "SECRETARIA")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .usernameParameter("username").passwordParameter("password")
                        .defaultSuccessUrl("/", true).failureUrl("/login?error=true").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout=true").permitAll())
                .addFilterBefore(filtroTokenMovil, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}