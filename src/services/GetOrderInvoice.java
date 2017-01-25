package services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.FlsCredit;

@WebServlet(description = "Get Order Invoice", urlPatterns = { "/GetOrderInvoice" })
public class GetOrderInvoice extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
		resp.setContentType("application/pdf");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		FlsCredit order = new FlsCredit();
		
		try{

			int orderId = Integer.parseInt(request.getParameter("orderId"));
			
			ByteArrayOutputStream output = order.getOrderInvoice(orderId);

			if(output == null)
				System.out.println("OutPut Stream NULL");
			
			response.setHeader("Expires", "0");
			response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");
			
			//set the response content type to PDF
			response.setContentType("application/pdf");
			
			response.setContentLength(output.size());
			
			//get the output stream for writing binary data in the response.
			ServletOutputStream os = response.getOutputStream();
			output.writeTo(os);
			os.flush();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Inside Exception of Get Order Invoice");
		}
	}

}
