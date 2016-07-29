package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import connect.Connect;

public class OAuth extends Connect{
	
	FlsLogger LOGGER = new FlsLogger(OAuth.class.getName());
	
	String API_KEY = null;
	
	public OAuth(){
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String sqlSelectApiKey = "SELECT value FROM config WHERE option=?";
			ps = hcp.prepareStatement(sqlSelectApiKey);
			ps.setString(1, "api_key");
			rs = ps.executeQuery();
			
			if(rs.next()){
				API_KEY = rs.getString("value");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			try {
				if(rs != null)rs.close();
				if(ps != null)ps.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public String generateOAuth(String userId){
		
		String jwt = null;
		
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.MONTH, 1);
		
		Date expiry = cal.getTime();
		
		try{
			jwt = Jwts.builder().setIssuer(userId)
					.setExpiration(expiry)
					.signWith(SignatureAlgorithm.HS512, API_KEY)
					.compact();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return jwt;
	}
	
	public String CheckOAuth(String oauth){
		
		try{
			Claims claims = Jwts.parser().setSigningKey(API_KEY).parseClaimsJws(oauth).getBody();
			
			Date expiry = claims.getExpiration();
			
			if(expiry.compareTo(new Date()) < 1){
				return null;
			}
			LOGGER.info(claims.getIssuer());
			return claims.getIssuer();
			
		}catch(SignatureException e){
			System.out.println(e.getMessage());
		}catch(NullPointerException e){
			System.out.println(e.getMessage());
		}
		
		return null;
	}
	
}
