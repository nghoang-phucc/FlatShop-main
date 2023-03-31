package com.coosi29.flatshop.controller.authen;

import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coosi29.flatshop.entity.Role;
import com.coosi29.flatshop.model.RoleDTO;
import com.coosi29.flatshop.model.UserDTO;
import com.coosi29.flatshop.service.UserService;

@Controller
public class RegisterController {

	@Autowired
	private UserService userService;

	@Autowired
	private MailSender mailSender;

	@GetMapping(value = "/register")
	public String register() {
		return "authen/register";
	}

	@PostMapping(value = "/registers")
	public String register(HttpServletRequest request, @RequestParam(name = "email") String email,
			@RequestParam(name = "password") String password, @RequestParam(name = "repassword") String repassword) {
		UserDTO userDTO = userService.findByEmail(email);
		//System.out.println(userService.findByEmail(email).getEmail());
			if (userDTO != null) {
				request.setAttribute("error", "The email address is already exist!");
				return "authen/register";
			} else {
				String code = randomString(8);
				UserDTO user = new UserDTO();
				user.setEmail(email);
				user.setPassword(new BCryptPasswordEncoder().encode(password));
				user.setAvatar("1608484153089.png");
				RoleDTO roleDTO = new RoleDTO();
				roleDTO.setRoleId(3);
				roleDTO.setRoleName("ROLE_USER");
				user.setRoleDTO(roleDTO);
				user.setVerify(true);
				userService.insert(user);
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				return "redirect:/client/home";
			}
	}
	
	@GetMapping(value = "/resend-code")
	public String resendCode(HttpSession session, HttpServletRequest request) {
		String code = randomString(8);
		String email = (String) session.getAttribute("emailRegister");
		sendEmail("coosi29@gmail.com", email, "Welcome to FlatShop!",
				"Hello, " + email.split("@")[0] + "! Please confirm that you can login in FlatShop!" + " Your confirmation code is: " + code);
		request.setAttribute("resend", "resend");
		session.setAttribute("codeVerify", code);
		return "authen/verify";
	}

	@PostMapping(value = "/verify")
	public String verify(HttpServletRequest request, HttpSession session) {
		String code = request.getParameter("code");
		String codeVerify = (String) session.getAttribute("codeVerify");
		if (!code.equals(codeVerify)) {
			request.setAttribute("verifyFail", "Invalid code, please try again!");
		} else {
			String email = (String) session.getAttribute("emailRegister");
			UserDTO userDTO = userService.findByEmail(email);
			userDTO.setVerify(true);
			request.setAttribute("verifySuccess", "Verification successfull!");
			request.setAttribute("active", "active");
			userService.update(userDTO);
		}
		return "authen/verify";
	} 
	
	@PostMapping(value = "get-news")
	public String getNews(@RequestParam(name = "email") String email) {
		sendEmail("coosi29@gmail.com", email, "Welcome to FlatShop!",
				"Thank you for your interest, we will send you the latest notice if any. Please pay attention to your mail.");
		return "client/get_news";
	}
	
	public void sendEmail(String from, String to, String subject, String content) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(from);
		mailMessage.setTo(to);
		mailMessage.setSubject(subject);
		mailMessage.setText(content);

		mailSender.send(mailMessage);
	}
	
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();

	String randomString(int len){
	   StringBuilder sb = new StringBuilder(len);
	   for(int i = 0; i < len; i++)
	      sb.append(AB.charAt(rnd.nextInt(AB.length())));
	   return sb.toString();
	}
}