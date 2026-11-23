package cn.itcast.wanxinp2p.account.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountDomain {
    C_DOMAIN("c", "C端用户"),
    B_DOMAIN("b", "B端用户");

    final String domain;
    final String desc;
}
