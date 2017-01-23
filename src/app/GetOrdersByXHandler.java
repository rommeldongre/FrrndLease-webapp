package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import pojos.GetEventsByXResObj;
import pojos.GetOrdersByXListResObj;
import pojos.GetOrdersByXReqObj;
import pojos.GetOrdersByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetOrdersByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetOrdersByXHandler.class.getName());

	private static GetOrdersByXHandler instance = null;

	public static GetOrdersByXHandler getInstance() {
		if (instance == null)
			instance = new GetOrdersByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetOrdersByXReqObj rq = (GetOrdersByXReqObj) req;

		GetOrdersByXListResObj rs = new GetOrdersByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null,rs2=null;

		LOGGER.info("Inside process method "+ rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetOrdersByX method");

		try {

			// Prepare SQL
			String sql = null;
			
			/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();*/
			
			// storing the front end data in appropriate variables
			int limit = rq.getLimit();
			int offset = rq.getCookie();
			String time =" 00:00:00";
			String userId = rq.getUserId();
			String fromDate = rq.getFromDate();
			String toDate = rq.getToDate();
			String search_string = rq.getSearchString();
			String type = rq.getType();
			
			
			//already getting all data from orders table
			sql = "SELECT tb_orders.order_id , tb_orders.order_date, tb_orders.order_user_id, tb_users.user_full_name, tb_orders.amount, tb_orders.promo_code, tb_orders.razor_pay_id, tb_orders.credit_log_id, tb_orders.order_type, tb_credits.credit_type FROM `orders` tb_orders INNER JOIN users tb_users ON tb_orders.order_user_id = tb_users.user_id INNER JOIN credit_log tb_credits ON tb_orders.credit_log_id = tb_credits.credit_log_id WHERE ";
			
			if(!fromDate.equals(time)){
				if(fromDate.equals(toDate)){
					sql = sql + "tb_orders.order_date LIKE '"+fromDate+"%' AND ";
				}else{
					sql = sql + "tb_orders.order_date BETWEEN '"+fromDate+"' AND '"+toDate+"' AND ";
				}
			}
			
			if(search_string != "")
				sql = sql + "(tb_orders.order_user_id LIKE '%"+search_string+"%' OR tb_users.user_full_name LIKE '%"+search_string+"%') AND ";
			
			if(userId != ""){
				sql = sql + "tb_orders.order_user_id='"+userId+"' AND ";
			}
				
			
			if(type.equals("FLS_ALL")){
				sql = sql + "tb_orders.order_type LIKE '%' ORDER BY tb_orders.order_id  DESC LIMIT " + offset + ","+limit;
			}else{
				sql = sql + "tb_orders.order_type='" + type + "' ORDER BY tb_orders.order_id DESC LIMIT " + offset + ","+limit;
			}
			
			LOGGER.info(sql);
			
			ps1 = hcp.prepareStatement(sql);

			rs1 = ps1.executeQuery();
			
			if (rs1.isBeforeFirst()) {
				while (rs1.next()) {
					GetOrdersByXResObj orders_rs1 = new GetOrdersByXResObj();
					
					orders_rs1.setOrderId(rs1.getInt("order_id"));
					orders_rs1.setOrderDate(rs1.getString("order_date"));
					orders_rs1.setOrderUserId(rs1.getString("order_user_id"));
					orders_rs1.setOrderUserName(rs1.getString("user_full_name"));
					orders_rs1.setAmount(rs1.getInt("amount"));
					orders_rs1.setPromoCode(rs1.getString("promo_code"));
					orders_rs1.setRazorPayId(rs1.getString("razor_pay_id"));
					orders_rs1.setCreditLogId(rs1.getInt("credit_log_id"));
					orders_rs1.setOrderType(rs1.getString("order_type"));
					orders_rs1.setCreditType(rs1.getString("credit_type"));
					
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
			rs.setLastOrderId(offset);
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
