package cn.itcast.wanxinp2p.uaa.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 把 /oauth2/token 的密码模式请求转换成 OAuth2PasswordAuthenticationToken
 */
public class OAuth2PasswordAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.PASSWORD.getValue().equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        String username = request.getParameter(OAuth2ParameterNames.USERNAME);
        String password = request.getParameter(OAuth2ParameterNames.PASSWORD);

        Set<String> scopes = Collections.emptySet();
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope)) {
            scopes = new HashSet<>(Arrays.asList(scope.split(" ")));
        }

        // 业务自定义参数：domain / authenticationType（自定义认证 Provider 需要）
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("domain", request.getParameter("domain"));
        additionalParameters.put("authenticationType", request.getParameter("authenticationType"));

        return new OAuth2PasswordAuthenticationToken(username, password, clientPrincipal, scopes, additionalParameters);
    }
}
