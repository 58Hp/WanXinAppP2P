package cn.itcast.wanxinp2p.consumer.converter;

import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConsumerConverter {
    ConsumerConverter INSTANCE = Mappers.getMapper(ConsumerConverter.class);

    ConsumerDTO convertConsumerEntityToDTO (Consumer entity);

    Consumer convertConsumerRegisterDTOToEntity (ConsumerRegisterDTO consumerRegisterDTO);

    AccountRegisterDTO convertConsumerRegisterDTO2AccountRegisterDTO (ConsumerRegisterDTO consumerRegisterDTO);
}
