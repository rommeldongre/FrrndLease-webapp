package adminOps;

public class Response {
	private int Code;
	private String Id,Message;
	
	public void setData(int code, String id, String message ) {
		this.Code = code;
		this.Id = id;
		this.Message = message;
	}
	
	public String getCode() {
		return String.valueOf(this.Code);
	}
	
	public String getId() {
		return Id;
	}
	
	public String getMessage() {
		return this.Message;
	}
	
	public int getIntCode() {
		return this.Code;
	}
}
