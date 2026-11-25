package cn.itcast.wanxinp2p.uaa.config;

import cn.itcast.wanxinp2p.uaa.domain.IntegrationUserDetailsAuthenticationHandler;
import cn.itcast.wanxinp2p.uaa.domain.IntegrationUserDetailsAuthenticationProvider;
import cn.itcast.wanxinp2p.uaa.domain.IntegrationWebAuthenticationDetailsSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public IntegrationUserDetailsAuthenticationHandler integrationUserDetailsAuthenticationHandler() {
        return new IntegrationUserDetailsAuthenticationHandler();
    }

    @Bean
    public IntegrationUserDetailsAuthenticationProvider integrationUserDetailsAuthenticationProvider() {
        return new IntegrationUserDetailsAuthenticationProvider(integrationUserDetailsAuthenticationHandler());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                              IntegrationWebAuthenticationDetailsSource authenticationDetailsSource) throws Exception {
        http
            .authenticationProvider(integrationUserDetailsAuthenticationProvider())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/logout", "/hi", "/druid/**", "/error").permitAll()
                    .anyRequest().authenticated())
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login.do")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .authenticationDetailsSource(authenticationDetailsSource)
                    .defaultSuccessUrl("/oauth2/authorize")
                    .failureUrl("/login?authentication_error=1"))
            .logout(logout -> logout
                    .logoutUrl("/logout.do")
                    .logoutSuccessUrl("/"))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
