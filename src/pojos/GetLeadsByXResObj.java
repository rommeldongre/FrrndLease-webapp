package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetLeadsByXResObj extends ResObj{
	
	int leadId;
	String leadLogDate,leadUserId,leadType,leadURL;
	
	public int getLeadId() {
		return leadId;
	}
	public void setLeadId(int leadId) {
		this.leadId = leadId;
	}
	public String getLeadLogDate() {
		return leadLogDate;
	}
	public void setLeadLogDate(String leadLogDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(leadLogDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.leadLogDate = Long.toString(date.getTime());
	}
	public String getLeadUserId() {
		return leadUserId;
	}
	public void setLeadUserId(String leadUserId) {
		this.leadUserId = leadUserId;
	}
	public String getLeadType() {
		return leadType;
	}
	public void setLeadType(String leadType) {
		this.leadType = leadType;
	}
	public String getLeadURL() {
		return leadURL;
	}
	public void setLeadURL(String leadURL) {
		this.leadURL = leadURL;
	}
}
