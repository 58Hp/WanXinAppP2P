package cn.itcast.wanxinp2p.uaa.config;

import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.uaa.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.uaa.domain.SmsCodeAuthenticationToken;
import cn.itcast.wanxinp2p.uaa.domain.UnifiedUserDetails;
import cn.itcast.wanxinp2p.uaa.oauth2.SmsCodeAuthenticationConverter;
import cn.itcast.wanxinp2p.uaa.oauth2.SmsCodeAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spring Authorization Server 授权服务器配置（授权码模式 + 短信验证码扩展授权）
 */
@Configuration
public class AuthorizationServerConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 客户端（应用）信息，存 oauth2_registered_client 表
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * 授权信息（授权码、token 等），存 oauth2_authorization 表
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 授权同意书，存 oauth2_authorization_consent 表
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * JWT 签名密钥源（RSA 非对称密钥）
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JwtEncoder jwtEncoder,
                                                            OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * 往 JWT 里塞自定义业务信息（mobile、tenant_id、department_id、user_authorities、payload）
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (context.getPrincipal() instanceof UsernamePasswordAuthenticationToken auth
                    && auth.getPrincipal() instanceof UnifiedUserDetails user) {
                if (user.getMobile() != null) {
                    context.getClaims().claim("mobile", user.getMobile());
                }
                if (user.getTenantId() != null) {
                    context.getClaims().claim("tenant_id", user.getTenantId());
                }
                if (user.getDepartmentId() != null) {
                    context.getClaims().claim("department_id", user.getDepartmentId());
                }
                if (user.getUserAuthorities() != null) {
                    context.getClaims().claim("user_authorities", user.getUserAuthorities());
                }
                if (user.getPayload() != null) {
                    context.getClaims().claim("payload", user.getPayload());
                }
            }
        };
    }

    /**
     * 短信验证码扩展授权处理器（专注短信登录，直接通过 AccountApiAgent 校验验证码并取账户）
     */
    @Bean
    public SmsCodeAuthenticationProvider smsCodeAuthenticationProvider(
            AccountApiAgent accountApiAgent,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<OAuth2Token> tokenGenerator) {
        return new SmsCodeAuthenticationProvider(accountApiAgent, authorizationService, tokenGenerator);
    }

    /**
     * 启动时注册客户端：短信验证码扩展授权 + 授权码模式
     */
    @Bean
    public CommandLineRunner registerClients(RegisteredClientRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            registerIfAbsent(repository, passwordEncoder, "wanxin-p2p-web-h5", "wanxinp2p");
            registerIfAbsent(repository, passwordEncoder, "wanxin-p2p-web-app", "123456");
        };
    }

    private void registerIfAbsent(RegisteredClientRepository repository, PasswordEncoder passwordEncoder,
                                  String clientId, String clientSecret) {
        if (repository.findByClientId(clientId) == null) {
            RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(SmsCodeAuthenticationToken.SMS_GRANT_TYPE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .scope("read")
                    .scope("write")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(2))
                            .refreshTokenTimeToLive(Duration.ofDays(3))
                            .build())
                    .build();
            repository.save(client);
        }
    }

    /**
     * 授权服务器安全过滤链（/oauth2/authorize、/oauth2/token、/oauth2/jwks、/oauth2/introspect 等）
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            SmsCodeAuthenticationProvider smsCodeAuthenticationProvider) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http
                .getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(new SmsCodeAuthenticationConverter())
                        .authenticationProvider(smsCodeAuthenticationProvider)
                        .accessTokenResponseHandler(this::writeAccessTokenResponse)
                        .errorResponseHandler(this::writeAccessTokenErrorResponse))
                .oidc(Customizer.withDefaults());
        http
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * token 成功响应：返回 {code:0, msg:"", access_token, refresh_token, token_type, expires_in}
     */
    private void writeAccessTokenResponse(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException {
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
                (OAuth2AccessTokenAuthenticationToken) authentication;
        OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
        OAuth2RefreshToken refreshToken = accessTokenAuthentication.getRefreshToken();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("access_token", accessToken.getTokenValue());
        data.put("token_type", accessToken.getTokenType().getValue());
        if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
            data.put("expires_in", Duration.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()).getSeconds());
        }
        if (refreshToken != null) {
            data.put("refresh_token", refreshToken.getTokenValue());
        }

        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), RestResponse.success(data));
    }

    /**
     * token 失败响应：返回 {code:-1, msg:错误描述}
     */
    private void writeAccessTokenErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                               AuthenticationException exception) throws IOException {
        OAuth2Error error = exception instanceof OAuth2AuthenticationException oauth2Exception
                ? oauth2Exception.getError() : new OAuth2Error("server_error");
        String msg = error.getDescription() != null ? error.getDescription() : error.getErrorCode();

        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), RestResponse.failure(msg));
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}
