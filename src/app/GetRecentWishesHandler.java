package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import adminOps.Response;
import connect.Connect;
import pojos.GetRecentWishesReqObj;
import pojos.GetRecentWishesResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRecentWishesHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRecentWishesHandler.class.getName());
	
	private Response res = new Response();
	
	private static GetRecentWishesHandler instance = null;
	
	public static GetRecentWishesHandler getInstance(){
		if(instance == null)
			instance = new GetRecentWishesHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		
		GetRecentWishesReqObj rq = (GetRecentWishesReqObj)req;
		GetRecentWishesResObj rs = new GetRecentWishesResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps = null;
		ResultSet result = null;
		
		LOGGER.info("Inside process method " + rq.getLimit());
		
		try {
			String sql = "SELECT DISTINCT item_name FROM `items` WHERE item_status='Wished' ORDER BY item_id DESC LIMIT ?";
			LOGGER.info("Creating Statement...");
			ps = hcp.prepareStatement(sql);
			ps.setInt(1, rq.getLimit());

			LOGGER.info("statement created...executing select wished titles from items");
			result = ps.executeQuery();

			LOGGER.info(result.toString());

			List<String> wishes = new ArrayList<>();
			
			if (result.next()) {
				while(result.next()){
					String wish = result.getString("item_name");
					wishes.add(wish);
				}
				rs.setWishes(wishes);
				rs.setCode(FLS_SUCCESS);

				LOGGER.info("Printing out ResultSet: " + rs.getWishes());
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			if(result != null)
				result.close();
			if(ps != null)
				ps.close();
			if(hcp != null)
				hcp.close();
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
