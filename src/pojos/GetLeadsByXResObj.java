package pojos;

public class GetLeadsByXResObj extends ResObj{
	
	String message;
	
	int leadId;
	String leadLogDate,leadUserId,leadType,leadURL;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
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
		this.leadLogDate = leadLogDate;
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
