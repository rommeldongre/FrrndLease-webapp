package pojos;

public class AddLeadReqObj extends ReqObj{
	
	String leadEmail,leadType,leadUrl;

	public String getLeadEmail() {
		return leadEmail;
	}

	public void setLeadEmail(String leadEmail) {
		this.leadEmail = leadEmail;
	}

	public String getLeadType() {
		return leadType;
	}

	public void setLeadType(String leadType) {
		this.leadType = leadType;
	}

	public String getLeadUrl() {
		return leadUrl;
	}

	public void setLeadUrl(String leadUrl) {
		this.leadUrl = leadUrl;
	}
}
