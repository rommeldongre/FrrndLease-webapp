package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import connect.Connect;
import pojos.GetTicketDetailsResObj;

public class FlsTicket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsTicket.class.getName());

	public static enum Ticket_Status {
		OPEN, CLOSED
	}

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

	public int toggleStatus(int ticketId, Ticket_Status status) {

		LOGGER.info("Inside toggleStatus Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			int idCheck = checkTicketId(ticketId);
			if (idCheck == 0) {
				return -1;
			}

			String sqlToggleStatus = "UPDATE `tickets` SET ticket_status=? WHERE ticket_id=?";

			ps1 = hcp.prepareStatement(sqlToggleStatus);
			ps1.setString(1, status.name());
			ps1.setInt(2, ticketId);
			result = ps1.executeUpdate();

			if (result == 1)
				LOGGER.info("Updated status to - " + status.name() + " for ticket id - " + ticketId);
			else
				LOGGER.info("Not able to update status for ticket id - " + ticketId);

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

	public GetTicketDetailsResObj getTicketDetails(int ticketId) {

		LOGGER.info("Inside getTicketDetails Method");

		GetTicketDetailsResObj rs = new GetTicketDetailsResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;

		try {

			int idCheck = checkTicketId(ticketId);
			if (idCheck == 0) {
				LOGGER.info("Not a valid ticket!!");
			}

			String sqlGetTicket = "SELECT * FROM `tickets` WHERE ticket_id=?";

			ps1 = hcp.prepareStatement(sqlGetTicket);
			ps1.setInt(1, ticketId);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				rs.setTicketDate(dateToString(rs1.getDate("ticket_date")));
				rs.setTicketUserId(rs1.getString("ticket_user_id"));
				rs.setDueDate(dateToString(rs1.getDate("due_date")));
				rs.setTicketType(rs1.getString("ticket_type"));
				rs.setTicketStatus(rs1.getString("ticket_status"));

				String sqlGetNotes = "SELECT * FROM `ticket_notes` WHERE ticket_id=?";

				ps2 = hcp.prepareStatement(sqlGetNotes);
				ps2.setInt(1, ticketId);
				rs2 = ps2.executeQuery();

				JSONArray notes = new JSONArray();

				while (rs2.next()) {
					JSONObject note = new JSONObject();
					note.put("noteId", rs2.getInt("note_id"));
					note.put("noteDate", dateToString(rs2.getDate("note_date")));
					note.put("note", rs2.getString("note"));

					notes.put(note);
				}

				rs.setNotes(notes);
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);

				LOGGER.info("Got the ticket details fro ticket id - " + ticketId);

			} else {
				LOGGER.info("Not able to get ticket details for ticket id - " + ticketId);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		} finally {
			try {
				if (rs2 != null)
					rs2.close();
				if (ps2 != null)
					ps2.close();
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

		return rs;

	}

	private String dateToString(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String d = df.format(date);
		return d;
	}

}
