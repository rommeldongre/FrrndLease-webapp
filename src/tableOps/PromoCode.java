package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.PromoCodeModel;
import util.FlsLogger;

public class PromoCode extends Connect {

	private FlsLogger LOGGER = new FlsLogger(PromoCode.class.getName());

	private String operation, promoCode, expiry, userId;
	private int credit, token;

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
		promoCode = pcm.getPromoCode();
		credit = pcm.getCredit();
		expiry = pcm.getExpiry();
		
		LOGGER.info("Inside addPromoCode Method");
		
		Connection hcp = getConnectionFromPool();
		
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		PreparedStatement ps2 = null;
		
		try{
			
			String sqlCheckPromoCode = "SELECT * FROM promo_credits WHERE code=?";
			ps1 = hcp.prepareStatement(sqlCheckPromoCode);
			ps1.setString(1, promoCode);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				res.setData(FLS_DUPLICATE_ENTRY, "0", FLS_DUPLICATE_ENTRY_M);
			}else{
				String sqlAddPromoCode = "INSERT INTO `promo_credits`(`code`, `credit`, `expiry`) VALUES (?,?,?)";
				ps2 = hcp.prepareStatement(sqlAddPromoCode);
				ps2.setString(1, promoCode);
				ps2.setInt(2, credit);
				ps2.setString(3, expiry);
				
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
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
