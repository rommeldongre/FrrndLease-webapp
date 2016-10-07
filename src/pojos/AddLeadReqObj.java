package pojos;

public class AddLeadReqObj extends ReqObj{
	
	String lead_email, lead_type;

	public String getLead_email() {
		return lead_email;
	}

	public void setLead_email(String lead_email) {
		this.lead_email = lead_email;
	}

	public String getLead_type() {
		return lead_type;
	}

	public void setLead_type(String lead_type) {
		this.lead_type = lead_type;
	}
}
