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
import util.Event;
import util.FlsLogger;
import util.OAuth;
import util.Event.Event_Type;
import util.Event.Notification_Type;

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
				ps1.setString(1, rq.getLeadEmail());
				rs1 = ps1.executeQuery();
				
				
				if(!rs1.next()){
					LOGGER.info("Insert statement for adding email to leads table .....");
					String sqlAddLeadEmail = "insert into leads (lead_email, lead_type, lead_url) values (?,?,?)";
					ps2 = hcp.prepareStatement(sqlAddLeadEmail);
					ps2.setString(1, rq.getLeadEmail());
					ps2.setString(2, rq.getLeadType());
					ps2.setString(3, rq.getLeadUrl());
					rs2 = ps2.executeUpdate();
					
					if(rs2!=1){
						rs.setCode(FLS_SQL_EXCEPTION);
						rs.setMessage(FLS_SQL_EXCEPTION_M);
						return rs;
					}
					
					Event event = new Event();
					event.createEvent(rq.getLeadEmail(), "admin@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_ADD_LEAD, 0, "A new Lead <b>" + rq.getLeadEmail() + "</b> of type <b><i>"+rq.getLeadType()+"</i></b> has been added.");
					event.createEvent("admin@frrndlease.com", rq.getLeadEmail(), Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_NOMAIL_ADD_LEAD, 0, "Thank You for subscribing to FrrndLease. You will recieve periodic updates about our exciting offers");
					
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_ADD_LEAD);
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