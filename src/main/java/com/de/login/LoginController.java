package com.de.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.de.signup.SignupRepository;
import com.de.signup.SignupService;
import com.de.user.Users;
import com.de.user.UsersDetail;
import com.github.scribejava.core.model.OAuth2AccessToken;

@Controller
@RequestMapping(value = "/login")
public class LoginController {
    @Autowired
    SignupService service;
    
    SignupRepository sr;
    
	@RequestMapping(value = "/login")
	public String loginView(Model model, @PageableDefault Pageable pageable) {
		System.out.println("----------log In----------");
		return "/login/signin";
	}

	@RequestMapping(value = "/signin")
	public String loginProc(Model model, @PageableDefault Pageable pageable) {
		System.out.println("----------log In 완료----------");
		return "/sample/list";
	}

	@RequestMapping(value = "/signup")
	public String signUpView(Model model, @PageableDefault Pageable pageable) {
		System.out.println("----------sign Up----------");
		return "/login/signup";
	}
	
	
//	// 로그인 첫 화면 요청 메소드
//	@RequestMapping(value = "/naver", method = { RequestMethod.GET, RequestMethod.POST })
//	public void naverLogin(Model model, Users vo ,HttpSession session) {
//		System.out.println("====> naver  controller<=====");
//		System.out.println("user id--> "+vo.getUserId());
//		System.out.println("user email--> "+vo.getUserEmail());
//		System.out.println("user name--> "+vo.getUserName());			
//	//	return "/login/naver";
//	}
//
//	// 네이버 로그인 성공시 callback호출 메소드
////	@RequestMapping(value = "/naverCallback", method = { RequestMethod.GET, RequestMethod.POST })
//////		@RequestMapping(value = "/naverCallback", method = { RequestMethod.GET, RequestMethod.POST })
////	public String callback(Model model, @RequestParam String code, @RequestParam String state, HttpSession session)
////			throws IOException {
////		System.out.println("여기는 callback");
////		OAuth2AccessToken oauthToken;
////		oauthToken = naverLoginBO.getAccessToken(session, code, state);
////		// 로그인 사용자 정보를 읽어온다.
////		apiResult = naverLoginBO.getUserProfile(oauthToken);
////		model.addAttribute("result", apiResult);
////
////		/* 네이버 로그인 성공 페이지 View 호출 */
////		return "/login/naverSuccess";
////	}
////
////	
//	@RequestMapping(value = "/naverCallback", method = { RequestMethod.GET, RequestMethod.POST })
//	public void callback(Model model, HttpSession session)throws IOException {
//		System.out.println("----naver  버튼 클릭---");
//
//		 String token = "PX4mWV3F_bqII3KVAVsl";// 네아로 접근 토큰 값";
//		 String header = "Bearer " + token; // Bearer 다음에 공백 추가
//
//		 	String apiURL = "https://openapi.naver.com/v1/nid/me";
//	       
//		 	Map<String, String> requestHeaders = new HashMap<>();
//	       requestHeaders.put("Authorization", header);
//	       String responseBody = get(apiURL,requestHeaders);
//
//	       System.out.println(responseBody);
//			System.out.println("====> naver login success controller");
//
//	}
//	


	// naver 소셜 가입
	@RequestMapping(value = "/naverCallback", method = { RequestMethod.GET, RequestMethod.POST })
	public String callback(Model model, HttpSession session, Users vo)throws IOException {
		System.out.println("=====================================");
		System.out.println("====> naver callback controller<=====");
		
		System.out.println("userEmail-->" + vo.getUserEmail());
		String email = vo.getUserEmail();
		System.out.println("아이디 존재 여부----> "+service.isExist(email));
		
	if(service.isExist(email)==true) {//기존에 등록된 계정
		model.addAttribute("retVal", "E");
		return "/sample/list";
	}else {// 새롭게 가입하는 sns 연동계정
			return "/login/naverCallback";

	}
		
	//	return "/login/naverCallback";

	}
	
	@RequestMapping(value = "/personalInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public void personalInfo( RedirectAttributes redirect ,Model model, HttpSession session, Users vo, UsersDetail uvo, @RequestParam String access_token)throws IOException {
		System.out.println("====> naver personal info controller<=====");
		System.out.println("user id--> " + vo.getUserId());
		System.out.println("user email--> " + vo.getUserEmail());
		System.out.println("user name--> " + vo.getUserName());
		System.out.println("access_token ---->"+ access_token);
		
		String userPassword = "@fromnaver";
		//ModelAndView view = new ModelAndView();
		//DB에 유저정보 저장 
		//소설가입 = 2
		vo.setUserStatus(2);
		vo.setUserPassword(userPassword);
		System.out.println("vo-->" + vo.toString());
		
		//최초 로그인 여부
		System.out.println("userEmail-->" + vo.getUserEmail());
		String email = vo.getUserEmail();
		System.out.println("아이디 존재 여부----> "+service.isExist(email));
		
		
		if(service.isExist(email) != true ) { //해당 이메일을 사용하는 계정이 존재하지 않는 경우만 저장			
			try {
				 service.save(vo);
				 if(vo.getUserId() != null) {
					 System.out.println("=====user info save====");
					 uvo.setUserNo(vo.getUserNo());
					 service.save(uvo);
					 System.out.println("=====user details info save====");
					redirect.addFlashAttribute("procVal", 'Y');
	
				 }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("저장 실패!");
				e.printStackTrace();
				redirect.addFlashAttribute("procVal",'F');
				
			}
		}else { // 가입된 계정인 경우

			System.out.println("해당 이메일로된 계정이 이미 존재함");
//			model.addAttribute("retVal","해당 이메일로된 계정이 이미 존재함" );	
//			System.out.println("해당 이메일로된 계정이 이미 존재함1"+model);
//			redirect.addFlashAttribute("procVal", "E");
		//	model.addAttribute("retVal", "E");
		}		
		
		//return "redirect:naverCallback";
		//return "redirect:login/login";
		//return "/login/naverCallback";

	}
	
}
