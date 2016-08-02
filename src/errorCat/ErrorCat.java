package errorCat;


public class ErrorCat {
	// Integer Codes
	final public int FLS_SUCCESS = 0;
	final public int FLS_END_OF_DB = 199;
	final public int FLS_SQL_EXCEPTION = 200;
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
	final public int FLS_SQL_EXCEPTION_I = 213;
	final public int FLS_INVALID_USER_I = 214;
	final public int FLS_PROMO_EXPIRED = 215;
	final public int FLS_INVALID_PROMO = 216;
	final public int FLS_ITEM_ON_HOLD = 217;
	final public int FLS_ACCESS_TOKEN_FAILED = 400;
	final public int FLS_NULL_POINT = 218;

	//String success messages
	final public String FLS_ITEMS_ADD = "Item added into  items table";
	final public String FLS_ITEMS_DELETE = "Item entry deleted from items table";
	final public String FLS_ITEMS_EDIT = "Item edited";
	final public String FLS_ITEMS_EDIT_STAT = "Item status updated";
	final public String FLS_ITEMS_DELETE_POSTING = "Posting deleted";
	final public String FLS_ITEMS_DELETE_WISH = "Wish deleted";
	final public String FLS_CATEGORY_ADD = "Category added.";
	final public String FLS_CATEGORY_DELETE = "Category deleted.";
	final public String FLS_SUCCESS_M = "Operation successfull";
	final public String FLS_ADD_FRIEND = "Friend added";
	final public String FLS_DELETE_FRIEND = "Friend deleted";
	final public String FLS_EDIT_FRIEND = "Friend edited";
	final public String FLS_EDIT_POST = "Edit posting successfull";
	final public String FLS_EDIT_WISH = "Edit wishlist successfull";
	final public String FLS_GRANT_LEASE = "Lease granted";
	final public String FLS_POST_ITEM = "Item posted in store";
	final public String FLS_REJECT_REQUEST = "Request rejected";
	final public String FLS_RENEW_LEASE = "Lease renewed successfully";
	final public String FLS_CLOSE_LEASE = "Lease closed successfully";
	final public String FLS_REQUEST_ITEM = "Request added successfully";
	final public String FLS_SIGNUP = "Signup successfull";
	final public String FLS_WISH_ITEM = "Item added to wishlist";
	
	//String error codes
	final public String FLS_SQL_EXCEPTION_M = "Couldn't create statement or couldn't execute query (sql exception)";
	final public String FLS_SQL_EXCEPTION_IMAGE = "Please select image less than 16MB";
	final public String FLS_ENTRY_NOT_FOUND_M = "Entry not found in table";
	final public String FLS_END_OF_DB_M = "End of database.";
	final public String FLS_JSON_EXCEPTION_M = "Json exception encountered.";
	final public String FLS_ITEMS_DP_LEASED_M = "Item is leased, close the lease first.";
	final public String FLS_ITEMS_DP_DEFAULT_M = "Item is niether posted nor leased";
	final public String FLS_INVALID_OPERATION_M = "Invalid operation selected";
	final public String FLS_DUPLICATE_ENTRY_M = "Item already requested by you,please wait.";
	final public String FLS_DUPLICATE_ENTRY_L = "Item already leased to someone,please wait";
	final public String FLS_GRANT_LEASE_N_M = "Lease could not be granted.";
	final public String FLS_LOGIN_USER_F = "Invalid password or username.";
	final public String FLS_POST_ITEM_F_M = "Post item could not be performed";
	final public String FLS_WISH_ITEM_F_M = "Wishitem could not be performed";
	final public String FLS_INVALID_TABLE_NAME_M = "Request contains invalid table name";
	final public String FLS_SEARCH_ITEM_F_M = "No data found.";
	final public String FLS_INVALID_USER_M = "Request Rejected as User ID Proof not submitted";
	final public String FLS_PROMO_EXPIRED_M = "This promo has expired!!";
	final public String FLS_INVALID_PROMO_M = "This is an invalid promo code!!";
	final public String FLS_ITEM_ON_HOLD_M= "This item is not allowed to be requested";
	final public String FLS_ACCESS_TOKEN_FAILED_M = "Session Expired!! Please login again";
	final public String FLS_NULL_POINT_M = "Wrong data!!";
	
	//String query variables.
	final public String FLS_WISHLIST_ADD = "Wished";
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

