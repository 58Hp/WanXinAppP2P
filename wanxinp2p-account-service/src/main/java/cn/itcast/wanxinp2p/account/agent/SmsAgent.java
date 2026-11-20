package cn.itcast.wanxinp2p.account.agent;

import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 短信服务 Feign 客户端
 * url 使用 ${sms.url} 直连外部短信服务（不走服务名负载均衡）
 */
@FeignClient(name = "sms-service", url = "${sms.url}")
public interface SmsAgent {

    /**
     * 默认有效时间
     */
    int DEFAULT_EFFECTIVE_TIME = 300;

    /**
     * 短信服务
     */
    String SMS_SERVICE = "sms";

    /**
     * 调用短信服务生成验证码
     *
     * @param effectiveTime 验证码有效期（秒）
     * @param name          业务标识
     * @param request       请求体（mobile 等参数）
     * @return 短信服务返回结果
     */
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    RestResponse generate(@RequestParam("effectiveTime") int effectiveTime,
                          @RequestParam("name") String name,
                          @RequestBody Map<String, String> request);

    /**
     * 校验短信验证码
     *
     * @param name             业务标识
     * @param verificationKey  校验标识（redis 中的键）
     * @param verificationCode 短信验证码
     * @return 短信服务返回结果
     */
    @PostMapping(value = "/verify")
    RestResponse verify(@RequestParam("name") String name,
                        @RequestParam("verificationKey") String verificationKey,
                        @RequestParam("verificationCode") String verificationCode);
}
