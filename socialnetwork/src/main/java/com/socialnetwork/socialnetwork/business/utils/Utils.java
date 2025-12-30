package com.socialnetwork.socialnetwork.business.utils;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class Utils {
	private static final SecureRandom random = new SecureRandom();
	private static final String ALPHANUMERIC_CHARACTERS = 
		      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	public static boolean VerifyPassword(String password) {
		password = password.trim();

		boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
		boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
		
		if(password.length() < 8 || !hasUppercase || !hasLowercase || !checkStringContainsNumber(password) || !checkStringContainsSpecialCharacter(password)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean checkStringContainsNumber(String input) {
	    return input.matches(".*\\d.*");
	}
	
	public static boolean checkStringContainsSpecialCharacter(String input) {
		 return input.matches(".*[^a-zA-Z0-9].*");
	}
	
	public static Object validPage(HttpServletRequest request, boolean userObjectShouldBeDifferentNull) {
		HttpSession session = request.getSession(true);
		Object userObject =   session.getAttribute("userId");
		
		return userObject;
	}
	
	public static String generateRandomString(int length) {
	    return random.ints(length, 0, ALPHANUMERIC_CHARACTERS.length())
	      .mapToObj(ALPHANUMERIC_CHARACTERS::charAt)
	      .map(Object::toString)
	      .collect(Collectors.joining());
	}
}
