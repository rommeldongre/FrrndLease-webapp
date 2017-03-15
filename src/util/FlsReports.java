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
	
	public enum Traction {
		SIGN_UP,
		REQUESTS,
		LEASES,
		ITEMS,
		WISHES
	}

	public GetReportResObj generateReport(GetReportReqObj rq) {

		LOGGER.info("Inside generateReport method");

		GetReportResObj rs = new GetReportResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			Traction[] tractions = Traction.values();
			
			int sCount = tractions.length;
			String[] series = new String[sCount];
			
			int j = 0;
			
			for(Traction traction: tractions){
				String sql = createQuery(rq.getReport(), traction, rq.getFreq(), rq.getFrom(), rq.getTo());
				
				ps1 = hcp.prepareStatement(sql);
				rs1 = ps1.executeQuery();
				
				ResultSetMetaData metaData = rs1.getMetaData();
				int count = metaData.getColumnCount();
				
				String[] labels = new String[count];

				int[] data = new int[count];
				
				int i = 0;
				
				if(rs1.next()){
					while(i < count){
						labels[i] = metaData.getColumnLabel(i+1);
						data[i] = rs1.getInt(labels[i]);
						i++;
					}
				}
				
				rs.setLabels(labels);
				rs.addData(data);
				
				if(traction.equals(Traction.SIGN_UP))
					series[j] = "Sign Up";
				else if(traction.equals(Traction.ITEMS))
					series[j] = "Items";
				else if(traction.equals(Traction.LEASES))
					series[j] = "Leases";
				else if(traction.equals(Traction.REQUESTS))
					series[j] = "Requests";
				else if(traction.equals(Traction.WISHES))
					series[j] = "Wishes";
				
				j++;
			}
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			rs.setSeries(series);

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

	private String createQuery(Report report, Traction traction, Freq freq, String from, String to) {

		String sql = "SELECT";

		Date fromDate = stringToDate(from), toDate = stringToDate(to);
		
		switch(traction){
			case SIGN_UP:
				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addWeekDays(fromDate);
					if(freq.equals(Freq.WEEKLY)){
						ad = addWeekDays(fromDate);
					} else if (freq.equals(Freq.MONTHLY)) {
						ad = addMonthDays(fromDate);
					}
					sql = sql + " SUM(CASE WHEN user_signup_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
					fromDate = ad;
				}
				
				sql = sql.substring(0, sql.length()-1);
				sql = sql + " from users";
				break;
			case REQUESTS:
				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addWeekDays(fromDate);
					if(freq.equals(Freq.WEEKLY)){
						ad = addWeekDays(fromDate);
					} else if (freq.equals(Freq.MONTHLY)) {
						ad = addMonthDays(fromDate);
					}
					sql = sql + " SUM(CASE WHEN request_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
					fromDate = ad;
				}
				
				sql = sql.substring(0, sql.length()-1);
				sql = sql + " from requests";
				break;
			case LEASES:
				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addWeekDays(fromDate);
					if(freq.equals(Freq.WEEKLY)){
						ad = addWeekDays(fromDate);
					} else if (freq.equals(Freq.MONTHLY)) {
						ad = addMonthDays(fromDate);
					}
					sql = sql + " SUM(CASE WHEN lease_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
					fromDate = ad;
				}
				
				sql = sql.substring(0, sql.length()-1);
				sql = sql + " from leases";
				break;
			case ITEMS:
				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addWeekDays(fromDate);
					if(freq.equals(Freq.WEEKLY)){
						ad = addWeekDays(fromDate);
					} else if (freq.equals(Freq.MONTHLY)) {
						ad = addMonthDays(fromDate);
					}
					sql = sql + " SUM(CASE WHEN item_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
					fromDate = ad;
				}
				
				sql = sql.substring(0, sql.length()-1);
				sql = sql + " from items WHERE item_status NOT IN ('Wished', 'Archived')";
				break;
			case WISHES:
				while (fromDate.compareTo(toDate) < 0) {
					Date ad = addWeekDays(fromDate);
					if(freq.equals(Freq.WEEKLY)){
						ad = addWeekDays(fromDate);
					} else if (freq.equals(Freq.MONTHLY)) {
						ad = addMonthDays(fromDate);
					}
					sql = sql + " SUM(CASE WHEN item_date BETWEEN '" + dateToString(fromDate) + "' AND '" + dateToString(ad) + "' THEN 1 END) as '" + dateToString(ad) + "',";
					fromDate = ad;
				}
				
				sql = sql.substring(0, sql.length()-1);
				sql = sql + " from items WHERE item_status='Wished'";
				break;
		}

		

		return sql;

	}

	private Date addWeekDays(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, 7);
		return cal.getTime();
	}

	private Date addMonthDays(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, 7);
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
