package errorCat;


public class ErrorCat {
	// Integer Codes
	final public int FLS_SUCCESS = 0;
	final public int FLS_END_OF_DB = 199;
	final public int FLS_SQL_EXCEPTION = 200;
	final public int FLS_SQL_EXCEPTION_I = 1406;
	final public int FLS_ENTRY_NOT_FOUND = 201;
	final public int FLS_JSON_EXCEPTION = 202;
	final public int FLS_INVALID_OPERATION = 203;
	final public int FLS_INVALID_TABLE_NAME = 204;
	final public int FLS_ITEMS_DP_LEASED = 215;
	final public int FLS_ITEMS_DP_DEFAULT = 216;
	final public int FLS_DUPLICATE_ENTRY = 225;
	final public int FLS_POST_ITEM_F = 210;
	final public int FLS_GRANT_LEASE_N = 220;
	final public int FLS_WISH_ITEM_F = 211;
	final public int FLS_SEARCH_ITEM_F = 212;

	//String success messages
	final public String FLS_ITEMS_ADD = "ITEM ADDED INTO  ITEMS TABLE";
	final public String FLS_ITEMS_DELETE = "ITEM ENTRY DELETED FROM ITEMS TABLE";
	final public String FLS_ITEMS_EDIT = "ITEM EDITED";
	final public String FLS_ITEMS_EDIT_STAT = "ITEM STATUS UPDATED";
	final public String FLS_ITEMS_DELETE_POSTING = "POSTING DELETED";
	final public String FLS_ITEMS_DELETE_WISH = "WISH DELETED";
	final public String FLS_CATEGORY_ADD = "CATEGORY ADDED.";
	final public String FLS_CATEGORY_DELETE = "CATEGORY DELETED.";
	final public String FLS_SUCCESS_M = "OPERATION SUCCESSFULL";
	final public String FLS_ADD_FRIEND = "FRIEND ADDED";
	final public String FLS_DELETE_FRIEND = "FRIEND DELETED";
	final public String FLS_EDIT_FRIEND = "FRIEND EDITED";
	final public String FLS_EDIT_POST = "EDIT POSTING SUCCESSFULL";
	final public String FLS_EDIT_WISH = "EDIT WISHLIST SUCCESSFULL";
	final public String FLS_GRANT_LEASE = "LEASE GRANTED";
	final public String FLS_POST_ITEM = "ITEM POSTED IN STORE";
	final public String FLS_REJECT_REQUEST = "REQUEST REJECTED";
	final public String FLS_RENEW_LEASE = "LEASE RENEWED SUCCESSFULLY";
	final public String FLS_CLOSE_LEASE = "LEASE CLOSED SUCCESSFULLY";
	final public String FLS_REQUEST_ITEM = "REQUEST ADDED SUCCESSFULLY";
	final public String FLS_SIGNUP = "SIGNUP SUCCESSFULL";
	final public String FLS_WISH_ITEM = "ITEM ADDED TO WISHLIST";
	
	//String error codes
	final public String FLS_SQL_EXCEPTION_M = "COULDN'T CREATE STATEMENT OR COULDN'T EXECUTE QUERY (SQL EXCEPTION)";
	final public String FLS_SQL_EXCEPTION_IMAGE = "PLEASE SELECT IMAGE LESS THAN 16MB";
	final public String FLS_ENTRY_NOT_FOUND_M = "ENTRY NOT FOUND IN TABLE";
	final public String FLS_END_OF_DB_M = "END OF DATABASE.";
	final public String FLS_JSON_EXCEPTION_M = "JSON EXCEPTION ENCOUNTERED.";
	final public String FLS_ITEMS_DP_LEASED_M = "ITEM IS LEASED, CLOSE THE LEASE FIRST.";
	final public String FLS_ITEMS_DP_DEFAULT_M = "ITEM IS NIETHER POSTED NOR LEASED";
	final public String FLS_INVALID_OPERATION_M = "INVALID OPERATION SELECTED";
	final public String FLS_DUPLICATE_ENTRY_M = "DUPLICATE ENTRY, TRY AGAIN.";
	final public String FLS_GRANT_LEASE_N_M = "LEASE COULD NOT BE GRANTED.";
	final public String FLS_LOGIN_USER_F = "INVALID PASSWORD OR USERAME.";
	final public String FLS_POST_ITEM_F_M = "POST ITEM COULD NOT BE PERFORMED";
	final public String FLS_WISH_ITEM_F_M = "WISHITEM COULD NOT BE PERFORMED";
	final public String FLS_INVALID_TABLE_NAME_M = "REQUEST CONTAINS INVALID TABLE NAME";
	final public String FLS_SEARCH_ITEM_F_M = "NO DATA FOUND.";
}

//JSON EXCEPTION AT SWITCH-CASE: res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
//INVALID OPERATION : res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
//SQL EXCEPTION : res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
//JSON EXCEPTION REST : res.setData(FLS_JSON_EXCEPTION,"0",FLS_JSON_EXCEPTION_M);
//ENTRY NOT FOUND : res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
/* GET NEXT AND GET PREVIOUS : 
 *         if(check != null ) {
				Code = FLS_SUCCESS;
				Id = check;
			}
			
			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION,"0",FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
 * */
/* import errorCat.ErrorCat;
 * 
 * private ErrorCat e = new ErrorCat();
 * */

