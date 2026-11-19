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
                    @Parameter(name = "mobile", description = "手机号码", required = true, example = "136xxxx9685")
            }
    )
    @GetMapping("/sms/{mobile}")
    public RestResponse getSMSCode(@PathVariable(name = "mobile") String mobile) {
        return accountService.getSMSCode(mobile);
    }
}
