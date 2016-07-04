package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetRecentWishesResObj extends ResObj{

	String message;
	int code;
	List<String> wishes = new ArrayList<>();
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public List<String> getWishes() {
		return wishes;
	}
	public void setWishes(List<String> wishes) {
		this.wishes = wishes;
	}
	
	
}
