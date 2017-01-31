package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import pojos.GetEventsByXResObj;
import pojos.GetPromoCodesByXListResObj;
import pojos.GetPromoCodesByXReqObj;
import pojos.GetPromoCodesByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetPromoCodesByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetPromoCodesByXHandler.class.getName());

	private static GetPromoCodesByXHandler instance = null;

	public static GetPromoCodesByXHandler getInstance() {
		if (instance == null)
			instance = new GetPromoCodesByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetPromoCodesByXReqObj rq = (GetPromoCodesByXReqObj) req;

		GetPromoCodesByXListResObj rs = new GetPromoCodesByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null,rs2=null;

		LOGGER.info("Inside process method "+ rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetPromoCodesByX method");

		try {

			// Prepare SQL
			String sql = null;
			
			/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();*/
			
			// storing the front end data in appropriate variables
			int limit = rq.getLimit();
			int offset = rq.getCookie();
			String time =" 00:00:00";
			//String userId = rq.getUserId();
			String fromDate = rq.getFromDate();
			String toDate = rq.getToDate();
			String search_string = rq.getSearchString();
			String type = rq.getType();
			
			
			//already getting all data from orders table
			sql = "SELECT tb_promo.id , tb_promo.expiry, tb_promo.credit, tb_promo.code, tb_promo.count, tb_promo.per_person_count, tb_promo.code_type FROM `promo_credits` tb_promo WHERE ";
			
			if(!fromDate.equals(time)){
				if(fromDate.equals(toDate)){
					sql = sql + "tb_promo.expiry LIKE '"+fromDate+"%' AND ";
				}else{
					sql = sql + "tb_promo.expiry BETWEEN '"+fromDate+"' AND '"+toDate+"' AND ";
				}
			}
			
			if(search_string != "")
				sql = sql + "(tb_promo.code LIKE '%"+search_string+"%') AND ";
			
			if(type.equals("FLS_ALL")){
				sql = sql + "tb_promo.code_type LIKE '%' ORDER BY tb_promo.id  DESC LIMIT " + offset + ","+limit;
			}else{
				sql = sql + "tb_promo.code_type='" + type + "' ORDER BY tb_promo.id DESC LIMIT " + offset + ","+limit;
			}
			
			LOGGER.info(sql);
			
			ps1 = hcp.prepareStatement(sql);

			rs1 = ps1.executeQuery();
			
			if (rs1.isBeforeFirst()) {
				while (rs1.next()) {
					GetPromoCodesByXResObj orders_rs1 = new GetPromoCodesByXResObj();
					
					orders_rs1.setPromoCodeId(rs1.getInt("id"));
					orders_rs1.setExpiryDate(rs1.getString("expiry"));
					orders_rs1.setCredits(rs1.getInt("credit"));
					orders_rs1.setPromoCode(rs1.getString("code"));
					orders_rs1.setCount(rs1.getInt("count"));
					orders_rs1.setPersonCount(rs1.getInt("per_person_count"));
					orders_rs1.setCodeType(rs1.getString("code_type"));
					
					rs.addResList(orders_rs1);
					offset = offset + 1;
				}
				LOGGER.info("Orders successfully added in response object");
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				LOGGER.warning("Entry not found in table");
			}
			rs.setLastPromoCodeId(offset);
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try {
				if(rs1!=null) rs1.close();
				if(ps1!=null) ps1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		LOGGER.info("Finished process method ");
		// return the response

		return rs;

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
