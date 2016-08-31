package util;

import java.security.SecureRandom;
import java.util.Random;

public class ReferralCode {
	
	public String createRandomCode(int codeLength){  
		String id = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	     char[] chars = id.toCharArray();
	        StringBuilder sb = new StringBuilder();
	        Random random = new SecureRandom();
	        for (int i = 0; i < codeLength; i++) {
	            char c = chars[random.nextInt(chars.length)];
	            sb.append(c);
	        }
	        String output = sb.toString();
	        System.out.println(output);
	        return output ;
	    }

}
