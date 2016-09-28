package pojos;

public class GetItemRatingResObj extends ResObj {

	int code, totalRating, totalRaters;
	String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getTotalRating() {
		return totalRating;
	}

	public void setTotalRating(int totalRating) {
		this.totalRating = totalRating;
	}

	public int getTotalRaters() {
		return totalRaters;
	}

	public void setTotalRaters(int totalRaters) {
		this.totalRaters = totalRaters;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
