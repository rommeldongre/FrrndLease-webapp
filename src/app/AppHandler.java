/**
 * 
 */
package app;

import pojos.ReqObj;
import pojos.ResObj;

/**
 * @author gayathri
 *
 */
public interface AppHandler {
	
	public void init();
	public ResObj process(ReqObj req) throws Exception;
	public void cleanup();
}
