package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    private SmsService smsService;

    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSMSCode(mobile);
    }

    @Override
    public Integer checkMobile(String mobile, String key, String code) {
        // 校验验证码
        smsService.verifySmsCode(key,code);
        // 用户是否存在
        LambdaQueryWrapper<Account> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Account::getMobile,mobile);
        long count=count(wrapper);
        return count > 0 ? 1 : 0;
    }
}
