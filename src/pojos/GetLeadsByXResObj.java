package pojos;

public class GetLeadsByXResObj extends ResObj{
	
	int quantity = 1;
	String message;
	
	int leadId;
	String leadLogDate,leadUserId,leadType;
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
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
}
