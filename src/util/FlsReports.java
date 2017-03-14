package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			rs.setLabels(new String[] { "January", "February", "March", "April", "May", "June", "July", "August",
					"September", "October", "December" });
			rs.setSeries(new String[] { "Sign Up", "Requests", "Leases" });
			int[] data1 = new int[] { 65, 59, 80, 81, 56, 55, 40, 81, 56, 55, 40 };
			int[] data2 = new int[] { 28, 48, 40, 19, 86, 27, 90, 81, 56, 55, 40 };
			int[] data3 = new int[] { 78, 78, 90, 89, 36, 77, 50, 31, 36, 75, 20 };
			rs.addData(data1);
			rs.addData(data2);
			rs.addData(data3);

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

		String sql = "SELECT SUM(CASE WHEN user_signup_date < '2016-12-25' THEN 1 END) as `2016-12-25`, SUM(CASE WHEN user_signup_date > '2016-12-25' THEN 1 END) as `2016-12-26` from users";

		switch (freq) {
		case WEEKLY:
			try {
				Date fromDate = stringToDate(from), toDate = stringToDate(to);

				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addDays(fromDate, 7);
					System.out.println(fromDate + "----" + ad);
					fromDate = ad;
				}
				
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
