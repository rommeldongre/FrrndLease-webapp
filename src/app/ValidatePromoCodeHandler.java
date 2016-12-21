package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import adminOps.Response;
import connect.Connect;
import pojos.ValidatePromoCodeReqObj;
import pojos.ValidatePromoCodeResObj;
import pojos.PromoCodeModel.Code_Type;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsCredit;
import util.FlsLogger;
import util.OAuth;
import util.FlsCredit.Credit;

public class ValidatePromoCodeHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ValidatePromoCodeHandler.class.getName());

	private Response res = new Response();

	private static ValidatePromoCodeHandler instance = null;

	public static ValidatePromoCodeHandler getInstance() {
		if (instance == null)
			instance = new ValidatePromoCodeHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of ValidatePromoCodeHandler");

		ValidatePromoCodeReqObj rq = (ValidatePromoCodeReqObj) req;
		ValidatePromoCodeResObj rs = new ValidatePromoCodeResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;

		try {
			
			String promoCode = rq.getPromoCode();
			String userId = rq.getUserId();
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(promoCode == null || promoCode.equals("")){
				rs.setCode(FLS_INVALID_PROMO);
				rs.setMessage(FLS_INVALID_PROMO_M);
				return rs;
			}
			rs.setPromoCode(promoCode);

			// Getting all the data for the promo code
			String sqlGetPromoData = "SELECT * FROM promo_credits WHERE code=?";
			ps1 = hcp.prepareStatement(sqlGetPromoData);
			ps1.setString(1, promoCode);
			
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				
				FlsCredit credits = new FlsCredit();
				
				int credit = rs1.getInt("credit");
				LOGGER.info("Credit for that promo code are - " + credit);
				
				Date expiry = rs1.getDate("expiry");
				LOGGER.info("Expiry of the promo code is - " + expiry);
				if(expiry != null){
					if (expired(expiry)) {
						rs.setCode(FLS_PROMO_EXPIRED);
						rs.setMessage(FLS_PROMO_EXPIRED_M);
						return rs;
					}
				}
				
				int count = rs1.getInt("count");
				LOGGER.info("Count of the promo code is - " + count);
				if(count != -1){
					if(count == 0){
						rs.setCode(FLS_PROMO_EXPIRED);
						rs.setMessage(FLS_PROMO_EXPIRED_M);
						return rs;
					}
				}
				
				String sqlGetFromOrders = "SELECT * FROM orders WHERE order_user_id=? AND promo_code=?";
				ps2 = hcp.prepareStatement(sqlGetFromOrders);
				ps2.setString(1, userId);
				ps2.setString(2, promoCode);
				
				rs2 = ps2.executeQuery();
				
				int tot = 0;
				
				while(rs2.next()){
					tot = tot + 1;
				}
				
				int perPersonCount = rs1.getInt("per_person_count");
				LOGGER.info("Per Person Count of the promo code is - " + perPersonCount);
				if(perPersonCount != -1){
					if(tot >= perPersonCount){
						rs.setCode(FLS_INVALID_PROMO);
						rs.setMessage("You cannot use this promo code more than " + perPersonCount + " time");
						return rs;
					}
				}

				String codeType = rs1.getString("code_type");
				switch(Code_Type.valueOf(codeType)){
					case FLS_INTERNAL:
						credits.logCredit(userId, credit, "Applied Promo Code", promoCode, Credit.ADD);
						int creditLogId = credits.getCreditLogId(userId, promoCode);
						int amount = credits.getCreditValue() * credit;
						credits.addOrder(userId, amount, promoCode, -1, creditLogId, Code_Type.FLS_INTERNAL);
						break;
					case FLS_EXTERNAL:
						break;
				}
				
				rs.setNewCreditBalance(credits.getCurrentCredits(userId));

			} else {
				rs.setCode(FLS_INVALID_PROMO);
				rs.setMessage(FLS_INVALID_PROMO_M);
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			if (rs2 != null) rs2.close();
			if (rs1 != null) rs1.close();
			if (ps2 != null) ps2.close();
			if (ps1 != null) ps1.close();
			if (hcp != null) hcp.close();
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

	private boolean expired(Date expiry) {

		int result = 0;

		try {
			Date current = new Date();
			current.setTime(0);
			result = current.compareTo(expiry);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}

		if (result <= 0)
			return false;
		else
			return true;
	}

}
