package cn.itcast.wanxinp2p.uaa.oauth2;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.uaa.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.uaa.domain.SmsCodeAuthenticationToken;
import cn.itcast.wanxinp2p.uaa.domain.UnifiedUserDetails;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 短信验证码扩展授权处理器。
 * <p>
 * 专注短信登录：直接通过 AccountApiAgent 校验短信验证码并获取账户，不再依赖账密登录的
 * IntegrationUserDetailsAuthenticationProvider，两者解耦。
 */
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private final AccountApiAgent accountApiAgent;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public SmsCodeAuthenticationProvider(AccountApiAgent accountApiAgent,
                                         OAuth2AuthorizationService authorizationService,
                                         OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        this.accountApiAgent = accountApiAgent;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken smsAuthentication = (SmsCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(smsAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        // 查询账户（判断账号是否存在；验证码校验已由前端通过 checkMobile 完成）
        RestResponse<AccountDTO> accountResult = accountApiAgent.getAccountByMobile(smsAuthentication.getMobile());
        if (accountResult.getCode() != 0 || accountResult.getResult() == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "用户不存在", ERROR_URI));
        }
        AccountDTO account = accountResult.getResult();

        // 构建认证主体
        UnifiedUserDetails user = new UnifiedUserDetails(account.getUsername(), "",
                AuthorityUtils.createAuthorityList());
        user.setMobile(account.getMobile());
        UsernamePasswordAuthenticationToken userAuthentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // 4. 校验 scope
        Set<String> authorizedScopes = new LinkedHashSet<>(registeredClient.getScopes());
        if (smsAuthentication.getScopes() != null && !smsAuthentication.getScopes().isEmpty()) {
            Set<String> unauthorizedScopes = smsAuthentication.getScopes().stream()
                    .filter(scope -> !registeredClient.getScopes().contains(scope))
                    .collect(Collectors.toSet());
            if (!unauthorizedScopes.isEmpty()) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
            }
            authorizedScopes = new LinkedHashSet<>(smsAuthentication.getScopes());
        }

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userAuthentication)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(SmsCodeAuthenticationToken.SMS_GRANT_TYPE)
                .authorizationGrant(smsAuthentication);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuthentication.getName())
                .authorizationGrantType(SmsCodeAuthenticationToken.SMS_GRANT_TYPE)
                .attribute(Principal.class.getName(), userAuthentication)
                .authorizedScopes(authorizedScopes);

        // 生成 access token
        OAuth2TokenContext accessTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(accessTokenContext);
        if (generatedAccessToken == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", ERROR_URI));
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), accessTokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(accessToken, metadata -> metadata.put(
                    OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        // 生成 refresh token
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            OAuth2TokenContext refreshTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(refreshTokenContext);
            if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the refresh token.", ERROR_URI));
            }
            refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
            authorizationBuilder.refreshToken(refreshToken);
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        Map<String, Object> additionalParameters = Collections.emptyMap();
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
