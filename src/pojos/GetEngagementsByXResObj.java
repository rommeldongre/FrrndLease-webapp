package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetEngagementsByXResObj extends ResObj{
	
	int totalCredits;
	String startDate,endDate;
	public int getTotalCredits() {
		return totalCredits;
	}
	public void setTotalCredits(int totalCredits) {
		this.totalCredits = totalCredits;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.startDate = Long.toString(date.getTime());
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.endDate = Long.toString(date.getTime());
	}
}
