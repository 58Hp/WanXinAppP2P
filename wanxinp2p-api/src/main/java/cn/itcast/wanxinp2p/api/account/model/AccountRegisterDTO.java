package cn.itcast.wanxinp2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title = "账户注册信息")
public class AccountRegisterDTO {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "手机号")
	private String mobile;

	@Schema(title = "密码")
	private String password;

}
