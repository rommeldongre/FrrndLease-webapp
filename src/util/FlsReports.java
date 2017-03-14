package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import connect.Connect;
import pojos.GetReportReqObj;
import pojos.GetReportResObj;

public class FlsReports extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsReports.class.getName());

	public enum Report {
		USER_TRACTION
	}

	public enum Freq {
		WEEKLY, MONTHLY
	}

	public GetReportResObj generateReport(GetReportReqObj rq) {

		LOGGER.info("Inside generateReport method");

		GetReportResObj rs = new GetReportResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			String sql = createQuery(rq.getReport(), rq.getFreq(), rq.getFrom(), rq.getTo());
			
			ps1 = hcp.prepareStatement(sql);
			rs1 = ps1.executeQuery();
			
			ResultSetMetaData metaData = rs1.getMetaData();
			int count = metaData.getColumnCount();
			
			System.out.println(count);
			
			int[] signupData = new int[count];
			String[] labels = new String[count];
			
			int i = 0;
			
			if(rs1.next()){
				while(i < count){
					labels[i] = metaData.getColumnLabel(i+1);
					signupData[i] = rs1.getInt(labels[i]);
					System.out.println(labels[i]);
					System.out.println(signupData[i]);
					i++;
				}
			}
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			rs.setLabels(labels);
			rs.setSeries(new String[]{"Sign Up"});
			rs.addData(signupData);

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return rs;

	}

	private String createQuery(Report report, Freq freq, String from, String to) {

		String sql = "SELECT";

		switch (freq) {
			case WEEKLY:
				try {
					Date fromDate = stringToDate(from), toDate = stringToDate(to);
	
					while (fromDate.compareTo(toDate) < 0) {
						Date ad = addDays(fromDate, 7);
						sql = sql + " SUM(CASE WHEN user_signup_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
						fromDate = ad;
					}
					sql = sql.substring(0, sql.length()-1);
					sql = sql + " from users";
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MONTHLY:
				break;
		}

		return sql;

	}

	private Date addDays(Date date, int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	private Date stringToDate(String date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		try {
			d = df.parse(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}
	
	private String dateToString(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String d = df.format(date);
		return d;
	}

}
