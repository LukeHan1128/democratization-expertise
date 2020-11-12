package com.de.login.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.de.login.vo.LoginHistoryVO;
import com.de.login.vo.LoginVO;


@Mapper
public interface LoginMapper {

	public LoginVO getUser(String username) throws Exception;

	public LoginVO getUserInfo(String username) throws Exception;

	public LoginVO getUserUUIDInfo(LoginVO vo) throws Exception;

	public LoginVO getUserChk(LoginVO vo) throws Exception;

	public int updateUserUUID(LoginVO vo) throws Exception;

	public LoginVO getSocialUserInfo(String username) throws Exception;

	public int saveLoginHistory(LoginHistoryVO vo) throws Exception;
}
