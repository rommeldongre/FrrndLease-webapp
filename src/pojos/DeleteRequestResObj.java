package pojos;

public class DeleteRequestResObj extends ResObj {
	
	
	//Return code
		int ReturnCode = 0;
		
	//Error String
	    String ErrorString;
		
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

}
