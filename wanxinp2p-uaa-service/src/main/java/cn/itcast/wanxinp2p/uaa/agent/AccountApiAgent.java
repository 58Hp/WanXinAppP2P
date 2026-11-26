package cn.itcast.wanxinp2p.uaa.agent;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "account-service", path = "/account")
public interface AccountApiAgent {

    /**
     * 根据手机号查询账户信息（用于判断账号是否存在）
     */
    @GetMapping("/mobiles/{mobile}")
    RestResponse<AccountDTO> getAccountByMobile(@PathVariable("mobile") String mobile);

    /**
     * 用户登录（获取账户信息）
     */
    @PostMapping("/l/accounts/session")
    RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO);
}
