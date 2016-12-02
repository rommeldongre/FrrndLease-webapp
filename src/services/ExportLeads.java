package services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.FlsExportLeads;
import util.FlsLogger;
import util.FlsPlan;

@WebServlet(description = "Export Leads as CSV", urlPatterns = { "/ExportLeads" })
public class ExportLeads extends HttpServlet {
	
	private FlsLogger LOGGER = new FlsLogger(ExportLeads.class.getName());
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
		resp.setContentType("text/csv");
		LOGGER.info("Inside GET Method");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		LOGGER.info("Inside POST Method");
		
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"Leads.csv\"");
		
		FlsExportLeads exportLeads = new FlsExportLeads();
		String leadType = request.getParameter("type");
		String fromDate = request.getParameter("fromDate");
		String toDate = request.getParameter("toDate");
		
        try {
        	
            OutputStream o = response.getOutputStream();
            String header = "lead Id,Lead Date,Lead Email,Lead Type,Lead Url\n";
            o.write(header.getBytes());

            StringBuffer line = new StringBuffer();
            line = exportLeads.getLeads(leadType,fromDate,toDate);
            if(line!=null){
            	 o.write(line.toString().getBytes());
            }
           
            o.flush();
            o.close();
            LOGGER.info("Finished Generating CSV file");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
