package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.common.AccountDomain;
import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.account.converter.AccountConverter;
import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        return AccountConverter.INSTANCE.convertAccountEntityToDTO(account);
    }

    @Override
    public AccountDTO login(AccountLoginDTO accountLoginDTO) {
        // 获取账户
        Account account = null;
        if (accountLoginDTO.getDomain().equalsIgnoreCase(AccountDomain.C_DOMAIN.getDomain())) {
            account = findAccountByMobile(accountLoginDTO.getMobile());//获取c端用户
        } else {
            account = getAccountByUsername(accountLoginDTO.getUsername());//获取b端用户
        }
        if (account == null) {
            throw new BusinessException(AccountErrorCode.E_130104); // 用户不存在
        }
        AccountDTO accountDTO = AccountConverter.INSTANCE.convertAccountEntityToDTO(account);
        //验证密码
        if (smsEnable) {// 如果smsEnable=true，说明是短信验证码登录，不做密码校验
            return accountDTO;
        }
        if (PasswordUtil.verify(accountLoginDTO.getPassword(), account.getPassword())) {
            return accountDTO;
        }
        throw new BusinessException(AccountErrorCode.E_130105);
    }

    /**
     * 根据手机号查询账户信息（不存在返回 null）
     */
    @Override
    public AccountDTO getAccountByMobile(String mobile) {
        Account account = findAccountByMobile(mobile);
        return account == null ? null : AccountConverter.INSTANCE.convertAccountEntityToDTO(account);
    }

    /**
     * 根据手机获取账户实体
     */
    private Account findAccountByMobile(String mobile) {
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getMobile, mobile));
    }

    /**
     * 根据用户名获取账户信息
     *
     * @param username 用户名
     * @return 账户实体
     */
    private Account getAccountByUsername(String username) {
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getUsername, username));
    }

}
