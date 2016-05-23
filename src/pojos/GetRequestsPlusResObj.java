package pojos;

public class GetRequestsPlusResObj extends ResObj{
	
	int Code, RequestItemId,requestId;
	String Id,Message,RequestorId,RequestUserName,RequestUserId,RequestDate, Title;
	public int getCode() {
		return Code;
	}
	public void setCode(int code) {
		Code = code;
	}
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
	public int getRequestItemId() {
		return RequestItemId;
	}
	public void setRequestItemId(int requestItemId) {
		RequestItemId = requestItemId;
	}
	public String getRequestorId() {
		return RequestorId;
	}
	public void setRequestorId(String requestorId) {
		RequestorId = requestorId;
	}
	public String getRequestUserName() {
		return RequestUserName;
	}
	public void setRequestUserName(String requestUserName) {
		RequestUserName = requestUserName;
	}
	public String getRequestUserId() {
		return RequestUserId;
	}
	public void setRequestUserId(String requestUserId) {
		RequestUserId = requestUserId;
	}
	public String getRequestDate() {
		return RequestDate;
	}
	public void setRequestDate(String requestDate) {
		RequestDate = requestDate;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
}
