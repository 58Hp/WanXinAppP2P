package cn.itcast.wanxinp2p.account.converter;

import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountConverter {
    AccountConverter INSTANCE = Mappers.getMapper(AccountConverter.class);

    /**
     * entity转为dto
     * @param entity do类型
     * @return
     */
    AccountDTO convertAccountEntityToDTO(Account entity);
}
