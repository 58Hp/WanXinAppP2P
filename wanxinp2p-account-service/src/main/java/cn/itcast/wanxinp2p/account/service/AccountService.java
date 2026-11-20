package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountService extends IService<Account> {

    /**
     * 获取手机短信验证码
     * @param mobile 手机号码
     * @return
     */
    RestResponse getSMSCode(String mobile);

    /**
     * 校验手机号和验证码
     * @param mobile
     * @param key
     * @param code
     * @return
     */
    Integer checkMobile(String mobile, String key, String code);


}
