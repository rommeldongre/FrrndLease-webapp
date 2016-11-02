package services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.FlsPlan;

@WebServlet(description = "Get Lease Agreement", urlPatterns = { "/GetLeaseAgreement" })
public class GetLeaseAgreement extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
		resp.setContentType("application/pdf");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		FlsPlan plan = new FlsPlan();
		
		try{

			int leaseId = Integer.parseInt(request.getParameter("leaseId"));
			
			ByteArrayOutputStream output = plan.getLeaseAgreement(leaseId);

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
			response.sendRedirect("index.html");
		}
	}

}
