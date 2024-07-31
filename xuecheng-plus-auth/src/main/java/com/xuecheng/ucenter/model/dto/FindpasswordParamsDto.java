package com.xuecheng.ucenter.model.dto;


import lombok.Data;

@Data
public class FindpasswordParamsDto {

	private String cellphone;
	private String email;
	private String checkcodekey;
	private String checkcode;
	private String confirmpwd;
	private String password;

}
