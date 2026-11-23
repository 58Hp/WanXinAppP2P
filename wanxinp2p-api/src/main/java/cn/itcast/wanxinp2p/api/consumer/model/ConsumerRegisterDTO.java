package cn.itcast.wanxinp2p.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title = "用户注册信息")
public class ConsumerRegisterDTO {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "手机号")
	private String mobile;

	@Schema(title = "密码")
	private String password;

	@Schema(title = "用户角色.B借款人or I投资人")
	private String role;

	@Schema(title = "验证码key")
	private String key;

}
