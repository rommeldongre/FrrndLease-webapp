package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import connect.Connect;
import pojos.GetEventsByXResObj;
import pojos.GetRazorPayDetailsByXListResObj;
import pojos.GetRazorPayDetailsByXReqObj;
import pojos.GetRazorPayDetailsByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRazorPayDetailsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRazorPayDetailsByXHandler.class.getName());

	private static GetRazorPayDetailsByXHandler instance = null;

	public static GetRazorPayDetailsByXHandler getInstance() {
		if (instance == null)
			instance = new GetRazorPayDetailsByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetRazorPayDetailsByXReqObj rq = (GetRazorPayDetailsByXReqObj) req;

		GetRazorPayDetailsByXListResObj rs = new GetRazorPayDetailsByXListResObj();
		RazorpayClient razorpay = new RazorpayClient("rzp_test_GwL1Gj4oI20Jeq", "Ec7yx9ffCkafqcsUNDFcNVpj");

		LOGGER.info("Inside process method "+ rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetRazorPayDetailsByX method");

		try {
			
					// storing the front end data in appropriate variables
					int limit = rq.getLimit();
					int final_amount =0;
					int offset = rq.getCookie();
					String userId = rq.getUserId();
					String razorPayId = rq.getRazorPayId();
					Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String paymentDate="";
					Date createdAt;
			
			
					//already getting all data from Razor Pay
					Payment payment = razorpay.Payments.fetch(razorPayId);
			
			 
					GetRazorPayDetailsByXResObj orders_rs1 = new GetRazorPayDetailsByXResObj();
					
					orders_rs1.setPaymentId(payment.get("id"));
					
					createdAt = payment.get("created_at");
					paymentDate = formatter.format(createdAt);
					orders_rs1.setPaymentDate(paymentDate);
					
					final_amount = payment.get("amount");
					final_amount = final_amount/100;
					orders_rs1.setAmount(final_amount);
					
					orders_rs1.setPaymentUserEmail(payment.get("email"));
					
					orders_rs1.setPaymentUserPhoneNumber(payment.get("contact"));
					
					rs.addResList(orders_rs1);
					offset = offset + 1;
					rs.setLastOrderId(offset);
			
					LOGGER.info("Orders successfully added in response object");
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SUCCESS_M);
					rs.setLastOrderId(offset);
		} catch (RazorpayException  e) {
			LOGGER.warning("Error Check Stacktrace");
			rs.setCode(FLS_RAZOR_EXCEPTION);
			rs.setMessage(FLS_RAZOR_EXCEPTION_M);
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
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
