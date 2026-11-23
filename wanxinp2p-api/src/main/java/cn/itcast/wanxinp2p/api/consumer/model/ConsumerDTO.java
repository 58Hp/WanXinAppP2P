package cn.itcast.wanxinp2p.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Schema(name = "ConsumerDTO", description = "平台c端用户信息")
public class ConsumerDTO {

	@Schema(name = "id", description = "用户id")
	private Long id;

	@Schema(name = "username", description = "用户名")
	private String username;

	@Schema(name = "fullname", description = "真实姓名")
	private String fullname;

	@Schema(name = "idNumber", description = "身份证号")
	private String idNumber;

	@Schema(name = "userNo", description = "用户编码，用户在存管系统标识，不允许重复")
	private String userNo;

	@Schema(name = "mobile", description = "手机号")
	private String mobile;

	@Schema(name = "userType", description = "用户类型,个人or企业，预留")
	private String userType;

	@Schema(name = "role", description = "角色")
	private String role;

	@Schema(name = "authList", description = "存管授权列表")
	private String authList;

	@Schema(name = "isBindCard", description = "是否已绑定银行卡")
	private Integer isBindCard;

	@Schema(name = "status", description = "启用状态")
	private Integer status;

	@Schema(name = "loanAmount", description = "可贷额度")
	private BigDecimal loanAmount;

}