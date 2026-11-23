package cn.itcast.wanxinp2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title = "账户信息")
public class AccountDTO {

	@Schema(title = "标识")
	private Long id;

    @Schema(title = "用户名")
	private String username;

	 @Schema(title = "手机号")
	private String mobile;

	 @Schema(title = "账号状态")
	private Integer status;

	 @Schema(title = "域(c：c端用户；b：b端用户)")
	private String domain;
}
