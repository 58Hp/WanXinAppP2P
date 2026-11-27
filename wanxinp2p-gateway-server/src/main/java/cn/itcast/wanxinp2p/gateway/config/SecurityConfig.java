package cn.itcast.wanxinp2p.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关资源服务器配置：校验 JWT。
 * 放行登录/短信/注册等无需 token 的接口，其余请求需要携带 Bearer JWT。
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                    // 放行：UAA（登录/token/jwks）、发送短信、校验验证码、注册
                    .pathMatchers(
                            "/uaa/**",
                            "/account/sms/**",
                            "/account/mobiles/*/key/*/code/*",
                            "/consumer/consumers"
                    ).permitAll()
                    .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults()));
        return http.build();
    }
}
