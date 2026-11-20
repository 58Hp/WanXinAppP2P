package cn.itcast.wanxinp2p.account.controller;

import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.api.account.AccountAPI;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "统一账户的Controller", description = "统一账户API")
public class AccountController implements AccountAPI {

    @Autowired
    AccountService accountService;

    @Override
    @Operation(
            summary = "获取手机验证码",
            parameters = {
                    @Parameter(name = "mobile", description = "手机号码", required = true, example = "135xxxx9685")
            }
    )
    @GetMapping("/sms/{mobile}")
    public RestResponse getSMSCode(@PathVariable(name = "mobile") String mobile) {
        return accountService.getSMSCode(mobile);
    }

    @Operation(
            summary = "校验手机号和验证码",
            parameters = {
                    @Parameter(name = "mobile", description = "手机号", required = true),
                    @Parameter(name = "key", description = "校验标识", required = true),
                    @Parameter(name = "code", description = "验证码", required = true)
            }
    )
    @GetMapping(value = "/mobiles/{mobile}/key/{key}/code/{code}")
    @Override
        public RestResponse<Integer> checkMobile(@PathVariable String mobile,@PathVariable String key,@PathVariable String code) {
            return RestResponse.success(accountService.checkMobile(mobile, key, code));
        }


}
