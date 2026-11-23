package cn.itcast.wanxinp2p.consumer.service.Impl;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CodePrefixCode;
import cn.itcast.wanxinp2p.common.domain.CommonErrorCode;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.consumer.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.consumer.common.ConsumerErrorCode;
import cn.itcast.wanxinp2p.consumer.converter.ConsumerConverter;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import cn.itcast.wanxinp2p.consumer.mapper.ConsumerMapper;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements ConsumerService {

    @Autowired
    AccountApiAgent accountApiAgent;

    @Override
    public Integer checkMobile (String mobile){
        return getByMobile(mobile, false) != null ? 1 : 0;
    }

    /**
     * 根据手机号获取用户信息
     * @param mobile 手机号
     * @param throwEx 不存在是否抛出异常
     * @return
     */
    private ConsumerDTO getByMobile (String mobile, Boolean throwEx){
        Consumer entity = getOne(new QueryWrapper<Consumer>().lambda().eq(Consumer::getMobile, mobile));
        return ConsumerConverter.INSTANCE.convertConsumerEntityToDTO(entity);
    }

    @Override
    public void register(ConsumerRegisterDTO consumerRegisterDTO) {
        //检测是否已注册
        if (checkMobile(consumerRegisterDTO.getMobile()) == 1) {
            throw new BusinessException(ConsumerErrorCode.E_140107);
        }
        // 创建用户信息
        Consumer consumer = ConsumerConverter.INSTANCE.convertConsumerRegisterDTOToEntity(consumerRegisterDTO);
        consumer.setUsername(CodeNoUtil.getNo(CodePrefixCode.CODE_NO_PREFIX));
        consumer.setUserNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        consumer.setIsBindCard(0);
        save(consumer);

        // 调用accountApi创建账户信息
        AccountRegisterDTO accountRegisterDTO = ConsumerConverter.INSTANCE.convertConsumerRegisterDTO2AccountRegisterDTO(consumerRegisterDTO);
        RestResponse<AccountDTO> restResponse = accountApiAgent.register(accountRegisterDTO);
        if (restResponse.getCode() != CommonErrorCode.SUCCESS.getCode()) {
            throw new BusinessException(ConsumerErrorCode.E_140106);
        }
    }
}
