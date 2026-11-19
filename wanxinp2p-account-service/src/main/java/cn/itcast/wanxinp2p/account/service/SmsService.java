package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.agent.SmsAgent;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

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
}
