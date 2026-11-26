package cn.itcast.wanxinp2p.uaa.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 把 /oauth2/token 的短信验证码扩展授权请求转换成 SmsCodeAuthenticationToken
 */
public class SmsCodeAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!SmsCodeAuthenticationToken.SMS_GRANT_TYPE.getValue().equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        String mobile = request.getParameter("mobile");
        String code = request.getParameter("code");
        String key = request.getParameter("key");

        Set<String> scopes = Collections.emptySet();
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope)) {
            scopes = new HashSet<>(Arrays.asList(scope.split(" ")));
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("domain", request.getParameter("domain"));
        additionalParameters.put("authenticationType", request.getParameter("authenticationType"));
        additionalParameters.put("key", key);

        return new SmsCodeAuthenticationToken(mobile, code, clientPrincipal, scopes, additionalParameters);
    }
}
