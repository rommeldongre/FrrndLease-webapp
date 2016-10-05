package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import app.AppHandler;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.AddLeadReqObj;
import pojos.AddLeadResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.OAuth;

public class AddLeadHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(AddLeadHandler.class.getName());
	
	private static AddLeadHandler instance = null;

	public static AddLeadHandler getInstance() {
		if (instance == null)
			instance = new AddLeadHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Add Lead Handler");
		
		AddLeadReqObj rq = (AddLeadReqObj) req;
		AddLeadResObj rs = new AddLeadResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null;
		int rs2=0;
		try {
			
				LOGGER.info("Select statement for checking if email exists or not .....");
				String sqlLeadEmail = "SELECT * FROM `leads` WHERE lead_email =?";
				ps1 = hcp.prepareStatement(sqlLeadEmail);
				ps1.setString(1, rq.getLead_email());
				rs1 = ps1.executeQuery();
				
				
				if(!rs1.next()){
					LOGGER.info("Insert statement for adding email to leads table .....");
					String sqlAddLeadEmail = "insert into leads (lead_email, lead_type) values (?,?)";
					ps2 = hcp.prepareStatement(sqlAddLeadEmail);
					ps2.setString(1, rq.getLead_email());
					ps2.setString(2, rq.getLead_type());
					rs2 = ps2.executeUpdate();
					
					if(rs2!=1){
						rs.setCode(FLS_SQL_EXCEPTION);
						rs.setMessage(FLS_SQL_EXCEPTION_M);
						return rs;
					}
					
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SUCCESS_M);
				}else{
					rs.setCode(FLS_DUPLICATE_ENTRY);
					rs.setMessage(FLS_DUPLICATE_ENTRY_LEAD);
				}
		} catch (NullPointerException e) {
			LOGGER.warning("Null Pointer Exception in Send Message App Handler");
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} catch(Exception e){
			LOGGER.warning("not able to get scheduler inside Send Message App Handler");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally {
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(ps2 != null)ps2.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method of Send Message handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}