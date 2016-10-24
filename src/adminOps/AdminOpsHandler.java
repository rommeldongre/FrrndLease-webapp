package adminOps;

import errorCat.ErrorCat;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.*;

import tableOps.*;
import util.FlsLogger;

public class AdminOpsHandler extends ErrorCat {

	private FlsLogger LOGGER = new FlsLogger(AdminOpsHandler.class.getName());

	private String table, operation;
	private JSONObject jsonobj1, jsonobj2;

	private Response res = new Response();

	public Response getInfo(String Table, JSONObject obj) {
		table = Table;

		try {
			jsonobj2 = obj;
			jsonobj1 = obj.getJSONObject("row");
			operation = obj.getString("operation");
			LOGGER.info(operation);

			selectTable();

		} catch (JSONException e) {
			LOGGER.warning("Couldnt parse operation from json");
			e.printStackTrace();
		}
		return res;
	}

	private void selectTable() {
		table = table.toLowerCase();
		LOGGER.info("Inside switch case.");
		switch (table) {

		case "items":
			Items items = new Items();
			ItemsModel im = new ItemsModel();

			LOGGER.info("Items table is selected..");
			im.getData(jsonobj1);
			res = items.selectOp(operation, im, jsonobj2);
			break;

		case "category":
			Category category = new Category();
			CategoryModel cm = new CategoryModel();

			LOGGER.info("Category table is selected..");
			cm.getData(jsonobj1);
			res = category.selectOp(operation, cm, jsonobj2);
			break;

		case "friends":
			Friends friend = new Friends();
			FriendsModel fm = new FriendsModel();

			LOGGER.info("Friends table is selected...");
			fm.getData(jsonobj1);
			res = friend.selectOp(operation, fm, jsonobj2);
			break;

		case "leases":
			Leases lease = new Leases();
			LeasesModel lm = new LeasesModel();

			LOGGER.info("Leases table is selected....");
			lm.getData(jsonobj1);
			res = lease.selectOp(operation, lm, jsonobj2);
			break;

		case "leaseterms":
			LeaseTerms leaseTerm = new LeaseTerms();
			LeaseTermsModel ltm = new LeaseTermsModel();

			LOGGER.info("LeaseTerms table is selected..");
			ltm.getData(jsonobj1);
			res = leaseTerm.selectOp(operation, ltm, jsonobj2);
			break;

		case "requests":
			Requests request = new Requests();
			RequestsModel rm = new RequestsModel();

			LOGGER.info("Requests table is selected..");
			rm.getData(jsonobj1);
			res = request.selectOp(operation, rm, jsonobj2);
			break;

		case "users":
			Users user = new Users();
			UsersModel um = new UsersModel();
			
			LOGGER.info("Users table is selected..");
			um.getData(jsonobj1);
			res = user.selectOp(operation, um, jsonobj2);
			break;
			
		case "promocode":
			PromoCode promoCode = new PromoCode();
			PromoCodeModel pcm = new PromoCodeModel();
			
			LOGGER.info("Promo Code table is selected");
			pcm.getData(jsonobj1);
			res = promoCode.selectOp(operation, pcm, jsonobj2);
			break;

		default:
			LOGGER.info("Table not present..");
			res.setData(FLS_INVALID_TABLE_NAME, "0", FLS_INVALID_TABLE_NAME_M);
			break;
		}
	}

}
