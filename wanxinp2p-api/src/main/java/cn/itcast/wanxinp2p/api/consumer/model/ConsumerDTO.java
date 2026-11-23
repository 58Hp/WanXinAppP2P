package cn.itcast.wanxinp2p.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Schema(title = "平台c端用户信息")
public class ConsumerDTO {

	@Schema(title = "用户id")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "真实姓名")
	private String fullname;

	@Schema(title = "身份证号")
	private String idNumber;

	@Schema(title = "用户编码，用户在存管系统标识，不允许重复")
	private String userNo;

	@Schema(title = "手机号")
	private String mobile;

	@Schema(title = "用户类型,个人or企业，预留")
	private String userType;

	@Schema(title = "角色")
	private String role;

	@Schema(title = "存管授权列表")
	private String authList;

	@Schema(title = "是否已绑定银行卡")
	private Integer isBindCard;

	@Schema(title = "启用状态")
	private Integer status;

	@Schema(title = "可贷额度")
	private BigDecimal loanAmount;

}