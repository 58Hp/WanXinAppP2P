package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.common.domain.RestResponse;

public interface AccountService {

    /**
     * 获取手机短信验证码
     * @param mobile 手机号码
     * @return
     */
    RestResponse getSMSCode(String mobile);

}
