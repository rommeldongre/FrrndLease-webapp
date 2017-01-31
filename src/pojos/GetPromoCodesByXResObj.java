package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetPromoCodesByXResObj extends ResObj{
	
	int promoCodeId,credits,count,personCount;
	String expiryDate,promoCode,codeType;
	
	public int getPromoCodeId() {
		return promoCodeId;
	}
	public void setPromoCodeId(int promoCodeId) {
		this.promoCodeId = promoCodeId;
	}
	public int getCredits() {
		return credits;
	}
	public void setCredits(int credits) {
		this.credits = credits;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getPersonCount() {
		return personCount;
	}
	public void setPersonCount(int personCount) {
		this.personCount = personCount;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		if(expiryDate!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			try {
				date = sdf.parse(expiryDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			this.expiryDate = Long.toString(date.getTime());
		}else{
			this.expiryDate = expiryDate;
		}
		
	}
	public String getPromoCode() {
		return promoCode;
	}
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	public String getCodeType() {
		return codeType;
	}
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
}
