package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;

import connect.Connect;

public class FlsTicket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsTicket.class.getName());

	public int addTicketType(String ticketType, String script, int dueDate) {

		LOGGER.info("Inside addTicketType Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlAddTicketType = "INSERT INTO `ticket_types` (`ticket_type`, `script`, `due_date`) VALUES (?,?,?)";

			ps1 = hcp.prepareStatement(sqlAddTicketType);
			ps1.setString(1, ticketType);
			ps1.setString(2, script);
			ps1.setInt(3, dueDate);
			result = ps1.executeUpdate();

			if (result == 1)
				LOGGER.info("Ticket Type Added");
			else
				LOGGER.info("Not able to add Ticket Type");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public int addTicket(String userId, String dueDate, String ticketType) {

		LOGGER.info("Inside addTicket Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		int result = 0;

		try {

			int dd = getDueDate(ticketType);
			if (dd == -1) {
				return dd;
			}

			String sqlAddTicket = "INSERT INTO `tickets` (`ticket_user_id`, `due_date`, `ticket_type`) VALUES (?,?,?)";

			ps1 = hcp.prepareStatement(sqlAddTicket, Statement.RETURN_GENERATED_KEYS);
			ps1.setString(1, userId);
			if (dueDate == null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.DATE, dd);
				ps1.setDate(2, new java.sql.Date(cal.getTimeInMillis()));
			} else {
				ps1.setString(2, dueDate);
			}
			ps1.setString(3, ticketType);
			result = ps1.executeUpdate();

			if (result == 1) {
				LOGGER.info("Ticket Added");
				rs1 = ps1.getGeneratedKeys();
				rs1.next();
				result = rs1.getInt(1);
			} else {
				LOGGER.info("Not able to add Ticket");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private int getDueDate(String ticketType) {

		LOGGER.info("Inside getDueDate Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		int dueDate = -1;

		try {

			String sqlAddNote = "SELECT * FROM `ticket_types` WHERE ticket_type=? LIMIT 1";

			ps1 = hcp.prepareStatement(sqlAddNote);
			ps1.setString(1, ticketType);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				dueDate = rs1.getInt("due_date");
				LOGGER.info("Found the ticket type - " + ticketType);
			} else {
				LOGGER.info("Not able to find the ticket type - " + ticketType);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dueDate;
	}

	public int addNote(String note, int ticketId) {

		LOGGER.info("Inside addNote Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			int idCheck = checkTicketId(ticketId);
			if (idCheck == 0) {
				return -1;
			}
			
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
		} finally {
			try {
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private int checkTicketId(int ticketId) {
		LOGGER.info("Inside checkTicketId Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		int idCheck = 0;

		try {

			String sqlAddNote = "SELECT * FROM `tickets` WHERE ticket_id=? LIMIT 1";

			ps1 = hcp.prepareStatement(sqlAddNote);
			ps1.setInt(1, ticketId);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				idCheck = 1;
				LOGGER.info("Found the ticket for ticket id - " + ticketId);
			} else {
				LOGGER.info("Not able to find the ticket for ticket id - " + ticketId);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (ps1 != null)
					ps1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return idCheck;
	}

}
