package cn.itcast.wanxinp2p.uaa.domain;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

/**
 * 短信验证码扩展授权（grant_type=urn:wanxinp2p:oauth:grant-type:sms）认证令牌
 */
@Getter
public class SmsCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 自定义扩展授权类型：短信验证码登录
     */
    public static final AuthorizationGrantType SMS_GRANT_TYPE =
            new AuthorizationGrantType("urn:wanxinp2p:oauth:grant-type:sms");

    private final String mobile;
    private final String code;
    private final Set<String> scopes;

    public SmsCodeAuthenticationToken(String mobile,
                                      String code,
                                      Authentication clientPrincipal,
                                      Set<String> scopes,
                                      Map<String, Object> additionalParameters) {
        super(SMS_GRANT_TYPE, clientPrincipal, additionalParameters);
        this.mobile = mobile;
        this.code = code;
        this.scopes = scopes;
    }

}
