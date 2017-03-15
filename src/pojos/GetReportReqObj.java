package pojos;

import util.FlsReports.Freq;
import util.FlsReports.Report;

public class GetReportReqObj extends ReqObj {

	Report report;
	Freq freq;
	String from, to;

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public Freq getFreq() {
		return freq;
	}

	public void setFreq(Freq freq) {
		this.freq = freq;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

}
