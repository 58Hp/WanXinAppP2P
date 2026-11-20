package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.agent.SmsAgent;
import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CommonErrorCode;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    /**
     * 是否启用手机短信验证码
     */
    @Value("${sms.enable}")
    private Boolean smsEnable;

    @Autowired
    private SmsAgent smsAgent;

    public RestResponse getSMSCode(String mobile) {
        if (smsEnable) {
            Map<String, String> request = new HashMap<>();
            request.put("mobile", mobile);
            return smsAgent.generate(SmsAgent.DEFAULT_EFFECTIVE_TIME, SmsAgent.SMS_SERVICE, request);
        }
        return RestResponse.success();
    }

    /**
     * 校验验证码
     * @param key 校验标识  redis中的键
     * @param code 短信验证码
     */
    public void verifySmsCode(String key, String code) {
        if (smsEnable) {
            RestResponse smsResponse = smsAgent.verify(SmsAgent.SMS_SERVICE, key, code);
            if (smsResponse.getCode() != CommonErrorCode.SUCCESS.getCode() ||
                    smsResponse.getResult().toString().equalsIgnoreCase("false")) {
                throw new BusinessException(AccountErrorCode.E_140152);
            }
        }
    }
}
