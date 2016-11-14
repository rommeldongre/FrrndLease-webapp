package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestObj {

	int requestId;
	String requestDate, requestorId, requestorName, requestorProfilePic, requestorLocality, requestorSublocality;

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(String requestDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(requestDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.requestDate = Long.toString(date.getTime());
	}

	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}

	public String getRequestorName() {
		return requestorName;
	}

	public void setRequestorName(String requestorName) {
		this.requestorName = requestorName;
	}

	public String getRequestorProfilePic() {
		return requestorProfilePic;
	}

	public void setRequestorProfilePic(String requestorProfilePic) {
		this.requestorProfilePic = requestorProfilePic;
	}

	public String getRequestorLocality() {
		return requestorLocality;
	}

	public void setRequestorLocality(String requestorLocality) {
		this.requestorLocality = requestorLocality;
	}

	public String getRequestorSublocality() {
		return requestorSublocality;
	}

	public void setRequestorSublocality(String requestorSublocality) {
		this.requestorSublocality = requestorSublocality;
	}
	
}
