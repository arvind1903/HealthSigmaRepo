package com.pos.services.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SecurityUtils {
	public static String sha1(String input) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		BigInteger bi = new BigInteger(1,md.digest(input.getBytes()));
		String hashedPassword = bi.toString(16); 
		if(hashedPassword.length()<40) {
			StringBuffer buffer = new StringBuffer();
			int len = 40-hashedPassword.length();
			for(int x=0; x<len; x++) {
				buffer.append("0");
			}
			hashedPassword=buffer.toString()+hashedPassword;
		}
		return hashedPassword;
	}
	
}
