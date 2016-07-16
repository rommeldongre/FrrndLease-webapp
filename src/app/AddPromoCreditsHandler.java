package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import adminOps.Response;
import connect.Connect;
import pojos.AddPromoCreditsReqObj;
import pojos.AddPromoCreditsResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.LogCredit;

public class AddPromoCreditsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(AddPromoCreditsHandler.class.getName());

	private Response res = new Response();

	private static AddPromoCreditsHandler instance = null;

	public static AddPromoCreditsHandler getInstance() {
		if (instance == null)
			instance = new AddPromoCreditsHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		AddPromoCreditsReqObj rq = (AddPromoCreditsReqObj) req;
		AddPromoCreditsResObj rs = new AddPromoCreditsResObj();
		Connection hcp = getConnectionFromPool();

		PreparedStatement ps1 = null;
		ResultSet result1 = null;
		
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		ResultSet result3 = null;

		LOGGER.info("Inside Process Method " + rq.getUserId());

		try {

			String promoCode = rq.getPromoCode();
			String userId = rq.getUserId();

			String sql1 = "SELECT credit, expiry FROM promo_credits WHERE code=?";
			ps1 = hcp.prepareStatement(sql1);
			ps1.setString(1, promoCode);

			LOGGER.info("statement created...executing select from users query");
			result1 = ps1.executeQuery();

			LOGGER.info(result1.toString());

			if (result1.next()) {

				String expiry = result1.getString("expiry");
				int credit = result1.getInt("credit");
				if (expired(expiry)) {
					rs.setCode(FLS_PROMO_EXPIRED);
					rs.setMessage(FLS_PROMO_EXPIRED_M);
				} else {
					
					LOGGER.info(credit+userId+expiry);
					String sql2 = "UPDATE users SET user_credit=user_credit+? WHERE user_id=?";
					ps2 = hcp.prepareStatement(sql2);
					ps2.setInt(1, credit);
					ps2.setString(2, userId);
					ps2.executeUpdate();
					
					String sql3 = "SELECT user_credit FROM users WHERE user_id=?";
					ps3 = hcp.prepareStatement(sql3);
					ps3.setString(1, userId);
					result3 = ps3.executeQuery();
					
					if(result3.next()){
						rs.setNewCreditBalance(result3.getInt("user_credit"));
					}
					
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SUCCESS_M);

					LogCredit lc = new LogCredit();
					lc.addLogCredit(userId, result1.getInt("credit"), "Promo Code", "");
				}

			} else {
				rs.setCode(FLS_INVALID_PROMO);
				rs.setMessage(FLS_INVALID_PROMO_M);
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			if(ps3 != null) ps3.close();
			if(result3 != null) result3.close();
			if(ps2 != null) ps2.close();
			if (result1 != null) result1.close();
			if (ps1 != null) ps1.close();
			if (hcp != null) hcp.close();
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	private boolean expired(String expiry) {

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		int result = 0;
		
		try{
			Date expiryDate = (Date)formatter.parse(expiry);
			
			Date currentDate = new Date();
			currentDate.setTime(0);
			result = currentDate.compareTo(expiryDate);
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}

		if(result <= 0)
			return false;
		else
			return true;
	}

}
