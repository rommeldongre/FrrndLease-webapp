package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetReportResObj extends ResObj {

	int code;
	String message;
	String[] labels, series;
	List<int[]> data = new ArrayList<>();

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

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public String[] getSeries() {
		return series;
	}

	public void setSeries(String[] series) {
		this.series = series;
	}

	public List<int[]> getData() {
		return data;
	}

	public void setData(List<int[]> data) {
		this.data = data;
	}

	public void addData(int[] arr) {
		this.data.add(arr);
	}

}
