package services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import util.FlsPlan;

@WebServlet(description = "Get Lease Agreement", urlPatterns = { "/GetLeaseAgreement" })
public class GetLeaseAgreement extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//get the output stream for writing binary data in the response.
		ServletOutputStream os = response.getOutputStream();
		//set the response content type to PDF
		response.setContentType("application/pdf");
		
		int leaseId = 0;
		
		FlsPlan plan = new FlsPlan();
		
		try{
			Document doc = new Document();
			
			PdfWriter.getInstance(doc, os);
			
			doc = plan.getLeaseAgreement(doc, leaseId);
			
			doc.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
