package adminOps;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.*;

import tableOps.*;

public class AdminOpsHandler {
	
	private String table, operation;
	private JSONObject jsonobj1,jsonobj2;

	
	
	
	
	
	private Response res = new Response();
	
	public Response getInfo(String Table, JSONObject obj){
		table = Table;
		
		try {
			jsonobj2 = obj;
			jsonobj1 = obj.getJSONObject("row");
			operation = obj.getString("operation");
			System.out.println(operation);
			
			selectTable();
			
		} catch (JSONException e) {
			System.out.println("Couldnt parse operation from json");
			e.printStackTrace();
		}
		return res;
	}
	
	private void selectTable() {
		table = table.toLowerCase();
		System.out.println("Inside switch case.");
		switch(table){
		
		case "items" :
			Items items = new Items();
			ItemsModel im = new ItemsModel();
			
			System.out.println("Items table is selected..");
			im.getData(jsonobj1);
			res = items.selectOp(operation, im, jsonobj2);
			break;
			
		case "category" :
			Category category = new Category();
			CategoryModel cm = new CategoryModel();
			
			System.out.println("Category table is selected..");
			cm.getData(jsonobj1);
			res = category.selectOp(operation, cm, jsonobj2);
			break;
			
		case "friends" :
			Friends friend = new Friends();
			FriendsModel fm = new FriendsModel();
			
			System.out.println("Friends table is selected...");
			fm.getData(jsonobj1);
			res = friend.selectOp(operation, fm, jsonobj2);
			break;
			
		case "leases" :
			Leases lease = new Leases();
			LeasesModel lm = new LeasesModel();
			System.out.println("Leases table is selected....");
			lm.getData(jsonobj1);
			res = lease.selectOp(operation, lm, jsonobj2);
			break;
			
		default:
			System.out.println("Table not present..");
			res.setData(203, "0", "Table not found!!!");
			break;
		}
	}

}
