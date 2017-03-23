package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;

public class FlsTicket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsTicket.class.getName());

	public int addTicketType(String ticketType, String script) {

		LOGGER.info("Inside addTicketType Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlAddTicketType = "INSERT INTO `ticket_types` (`ticket_type`, `script`) VALUES (?,?)";

			ps1 = hcp.prepareStatement(sqlAddTicketType);
			ps1.setString(1, ticketType);
			ps1.setString(2, script);
			result = ps1.executeUpdate();

			if (result == 1)
				LOGGER.info("Ticket Type Added");
			else
				LOGGER.info("Not able to add Ticket Type");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int addTicket(String userId, String dueDate, String ticketType) {

		LOGGER.info("Inside addTicket Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlAddTicket = "INSERT INTO `tickets` (`ticket_user_id`, `due_date`, `ticket_type`) VALUES (?,?,?)";

			ps1 = hcp.prepareStatement(sqlAddTicket);
			ps1.setString(1, userId);
			ps1.setString(2, dueDate);
			ps1.setString(3, ticketType);
			result = ps1.executeUpdate();

			if (result == 1)
				LOGGER.info("Ticket Added");
			else
				LOGGER.info("Not able to add Ticket");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int addNote(String note, int ticketId) {

		LOGGER.info("Inside addNote Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlAddNote = "INSERT INTO `ticket_notes` (`note`, `ticket_id`) VALUES (?,?)";

			ps1 = hcp.prepareStatement(sqlAddNote);
			ps1.setString(1, note);
			ps1.setInt(2, ticketId);
			result = ps1.executeUpdate();

			if (result == 1)
				LOGGER.info("Note Added for ticket id - " + ticketId);
			else
				LOGGER.info("Not able to add a note for ticket id - " + ticketId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
