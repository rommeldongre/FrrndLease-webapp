package pojos;

public class AddFbIdResObj extends ResObj{

	//Return code for PostItem
	int code = 0;
			
	//Error String
	String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
