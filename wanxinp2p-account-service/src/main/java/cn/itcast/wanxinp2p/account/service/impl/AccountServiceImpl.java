package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.common.AccountDomain;
import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    /**
     * 是否启用手机短信验证码
     */
    @Value("${sms.enable}")
    private Boolean smsEnable;

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

    /**
     * 账户注册
     * @param registerDTO 注册信息
     * @return
     */
    @Override
    public AccountDTO register(AccountRegisterDTO registerDTO) {
        Account account = new Account();
        account.setUsername(registerDTO.getUsername());
        account.setMobile(registerDTO.getMobile());
        account.setPassword(PasswordUtil.generate(registerDTO.getPassword()));
        if (smsEnable) {
            account.setPassword(PasswordUtil.generate(account.getMobile()));
        }
        account.setDomain(AccountDomain.C_DOMAIN.getDomain());
        account.setStatus(StatusCode.STATUS_OUT.getCode());
        save(account);
        return convertAccountEntityToDTO(account);
    }

    /**
     * entity转为dto
     * @param entity do类型
     * @return
     */
    private AccountDTO convertAccountEntityToDTO(Account entity) {
        if (entity == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

}
