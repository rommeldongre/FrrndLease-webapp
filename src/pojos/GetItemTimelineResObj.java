package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetItemTimelineResObj{

	int itemId;
	
	String itemLogDate;
	
	String itemLogType;
	
	String itemLogDesc;
	
	String itemLogImageLink;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemLogDate() {
		return itemLogDate;
	}

	public void setItemLogDate(String itemLogDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(itemLogDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.itemLogDate = Long.toString(date.getTime());
		
	}

	public String getItemLogType() {
		return itemLogType;
	}

	public void setItemLogType(String itemLogType) {
		this.itemLogType = itemLogType;
	}

	public String getItemLogDesc() {
		return itemLogDesc;
	}

	public void setItemLogDesc(String itemLogDesc) {
		this.itemLogDesc = itemLogDesc;
	}

	public String getItemLogImageLink() {
		return itemLogImageLink;
	}

	public void setItemLogImageLink(String itemLogImageLink) {
		this.itemLogImageLink = itemLogImageLink;
	}
}
