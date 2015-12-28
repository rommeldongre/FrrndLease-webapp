package pojos;

import javax.validation.constraints.NotNull;

import java.util.Date;
//import org.apache.tomcat.util.buf.StringCache;

public class GetRequestsByUserResObj extends ResObj{
	
	//Return code for GetRequestsByUser
	int ReturnCode = 0;
	
	//Error String
	String ErrorString;
	
	
	String title;
	
	// description of item
	String desc;
	
	// number of items, default is 1
	int quantity = 1;
	
	// ID of request
	// TBD: need to change type
	@NotNull
	int request_id;
	
	// user posting item
	// TBD: change to user id type
	@NotNull
	String owner_Id;
	
	//user requesting item
	String user_Id;
	
	// Cookie
	// TBD: change to user id type
	@NotNull
	int cookie;
			
	//Request Status
	String Request_status;
	
	//Request date
	Date Request_date;

	public int getReturnCode() {
		return ReturnCode;
	}

	public void setReturnCode(int returnCode) {
		ReturnCode = returnCode;
	}

	public String getErrorString() {
		return ErrorString;
	}

	public void setErrorString(String errorString) {
		ErrorString = errorString;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getRequest_id() {
		return request_id;
	}

	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}

	public String getOwner_Id() {
		return owner_Id;
	}

	public void setOwner_Id(String owner_Id) {
		this.owner_Id = owner_Id;
	}

	public String getUser_Id() {
		return user_Id;
	}

	public void setUser_Id(String user_Id) {
		this.user_Id = user_Id;
	}

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public String getRequest_status() {
		return Request_status;
	}

	public void setRequest_status(String request_status) {
		Request_status = request_status;
	}

	public Date getRequest_date() {
		return Request_date;
	}

	public void setRequest_date(Date request_date) {
		Request_date = request_date;
	}

	
	
}