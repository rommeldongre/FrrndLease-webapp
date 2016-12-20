package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.PromoCodeModel;
import pojos.PromoCodeModel.Code_Type;
import util.FlsLogger;
import util.OAuth;

public class PromoCode extends Connect {

	private FlsLogger LOGGER = new FlsLogger(PromoCode.class.getName());

	private String operation, promoCode, expiry, userId, accessToken;
	private int credit, count, perPersonCount;
	
	private Code_Type codeType;

	private PromoCodeModel pcm;
	private Response res = new Response();

	public Response selectOp(String Operation, PromoCodeModel pc, JSONObject obj) {
		operation = Operation.toLowerCase();
		pcm = pc;

		switch (operation) {

		case "addpromocode":
			LOGGER.info("addPromoCode op is selected..");
			addPromoCode();
			break;
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}
	
	private void addPromoCode(){
		
		LOGGER.info("Inside addPromoCode Method");
		
		userId = pcm.getUserId();
		accessToken = pcm.getAccessToken();
		
		OAuth oauth = new OAuth();
		String oauthcheck = oauth.CheckOAuth(accessToken);
		if(!oauthcheck.equals(userId)){
			res.setData(FLS_ACCESS_TOKEN_FAILED, "0", FLS_ACCESS_TOKEN_FAILED_M);
			return;
		}
		
		promoCode = pcm.getPromoCode();
		expiry = pcm.getExpiry();
		codeType = pcm.getCodeType();
		credit = pcm.getCredit();
		count = pcm.getCount();
		perPersonCount = pcm.getPerPersonCount();
		
		Connection hcp = getConnectionFromPool();
		
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlCheckPromoCode = "SELECT * FROM promo_credits WHERE code=?";
			ps1 = hcp.prepareStatement(sqlCheckPromoCode);
			ps1.setString(1, promoCode);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				res.setData(FLS_DUPLICATE_ENTRY, "0", FLS_DUPLICATE_ENTRY_M);
			}else{
				String sqlAddPromoCode = "INSERT INTO `promo_credits` (`code`, `credit`, `expiry`, `count`, `per_person_count`, `code_type`) VALUES (?, ?, ?, ?, ?, ?)";
				ps2 = hcp.prepareStatement(sqlAddPromoCode);
				ps2.setString(1, promoCode);
				ps2.setInt(2, credit);
				ps2.setString(3, expiry);
				if(count == -1)
					ps2.setString(4, null);
				else
					ps2.setInt(4, count);
				if(perPersonCount == -1)
					ps2.setString(5, null);
				else
					ps2.setInt(5, perPersonCount);
				ps2.setString(6, codeType.name());
				
				ps2.executeUpdate();
				
				res.setData(FLS_SUCCESS, "0", FLS_ADD_PROMO_CODE);
			}
			
		}catch(SQLException e){
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(ps2 != null)ps2.close();
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
