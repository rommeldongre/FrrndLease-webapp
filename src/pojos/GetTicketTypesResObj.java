package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetTicketTypesResObj extends ResObj {

	int code;
	String message;

	List<String> types = new ArrayList<>();
	List<String> scripts = new ArrayList<>();
	List<Integer> dues = new ArrayList<>();

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

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public void addType(String type) {
		this.types.add(type);
	}

	public List<String> getScripts() {
		return scripts;
	}

	public void setScripts(List<String> scripts) {
		this.scripts = scripts;
	}

	public void addScript(String script) {
		this.scripts.add(script);
	}

	public List<Integer> getDues() {
		return dues;
	}

	public void setDues(List<Integer> dues) {
		this.dues = dues;
	}

	public void addDue(int due) {
		this.dues.add(due);
	}

}
