package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetRequestsPlusResObj extends ResObj {

	int Code, RequestItemId, requestId;
	String Id, Message, RequestorId, RequestUserName, RequestUserId, RequestDate, Title, Description;

	// more items details
	String category, leaseValue, leaseTerm, uid, primaryImageLink;

	// more items owners details
	String requestor_mobile, requestor_address, requestor_locality, requestor_sublocality;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLeaseValue() {
		return leaseValue;
	}

	public void setLeaseValue(String leaseValue) {
		this.leaseValue = leaseValue;
	}

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	public String getPrimaryImageLink() {
		return primaryImageLink;
	}

	public void setPrimaryImageLink(String primaryImageLink) {
		this.primaryImageLink = primaryImageLink;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getRequestor_mobile() {
		return requestor_mobile;
	}

	public void setRequestor_mobile(String requestor_mobile) {
		this.requestor_mobile = requestor_mobile;
	}

	public String getRequestor_address() {
		return requestor_address;
	}

	public void setRequestor_address(String requestor_address) {
		this.requestor_address = requestor_address;
	}

	public String getRequestor_locality() {
		return requestor_locality;
	}

	public void setRequestor_locality(String requestor_locality) {
		this.requestor_locality = requestor_locality;
	}

	public String getRequestor_sublocality() {
		return requestor_sublocality;
	}

	public void setRequestor_sublocality(String requestor_sublocality) {
		this.requestor_sublocality = requestor_sublocality;
	}

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(requestDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		RequestDate = Long.toString(date.getTime());
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}
}
