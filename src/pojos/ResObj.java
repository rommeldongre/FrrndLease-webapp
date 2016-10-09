package pojos;

/**
 * @author gayathri
 *
 */

public abstract class ResObj {
	
	// error description in case of error, null if no error
	String error = null;

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}
	
	
}
