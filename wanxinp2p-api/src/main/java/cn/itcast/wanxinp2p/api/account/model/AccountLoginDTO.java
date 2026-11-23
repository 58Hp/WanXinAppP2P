package cn.itcast.wanxinp2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 封装账户登录信息
 * </p>
 */
@Data
@Schema(title = "账户登录信息")
public class AccountLoginDTO {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "手机号")
	private String mobile;

	@Schema(title = "密码")
	private String password;

	@Schema(title = "域(c：c端用户；b：b端用户)")
	private String domain;

}
