package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import connect.Connect;
import pojos.GetTicketDetailsResObj;
import pojos.GetTicketTypesResObj;
import pojos.GetTicketsByXListResObj;
import pojos.GetTicketsByXResObj;
import pojos.TicketNote;

public class FlsTicket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsTicket.class.getName());

	public static enum Ticket_Status {
		OPEN, CLOSED
	}

	public static enum Filter_Status {
		DONE, DUE, PENDING
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

			String sqlGetDueDate = "SELECT * FROM `ticket_types` WHERE ticket_type=? LIMIT 1";

			ps1 = hcp.prepareStatement(sqlGetDueDate);
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

			if (result == 1) {
				LOGGER.info("Note Added for ticket id - " + ticketId);
				updateTicketLastModified(ticketId);
			} else
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

	private void updateTicketLastModified(int ticketId) {

		LOGGER.info("Inside updateTicketLastModified Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlTicketModifiedDate = "UPDATE tickets SET ticket_lastmodified=now() WHERE ticket_id=?";

			ps1 = hcp.prepareStatement(sqlTicketModifiedDate);
			ps1.setInt(1, ticketId);
			result = ps1.executeUpdate();

			if (result == 1) {
				LOGGER.info("Last modified date updated for ticket id - " + ticketId);
			} else
				LOGGER.info("Not able to update last modified date for ticket id - " + ticketId);

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
	}

	public int updateDueDate(int ticketId, String dueDate) {

		LOGGER.info("Inside updateDueDate Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int result = 0;

		try {

			String sqlUpdateDueDate = "UPDATE tickets SET due_date=? WHERE ticket_id=?";

			ps1 = hcp.prepareStatement(sqlUpdateDueDate);
			ps1.setString(1, dueDate);
			ps1.setInt(2, ticketId);
			result = ps1.executeUpdate();

			if (result == 1) {
				LOGGER.info("Due date updated for ticket id - " + ticketId);
			} else
				LOGGER.info("Not able to update due date for ticket id - " + ticketId);

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

			String sqlGetTicket = "SELECT tb1.*, tb2.user_full_name, tb2.user_sublocality, tb2.user_locality, tb2.user_profile_picture, tb3.script FROM tickets tb1 INNER JOIN users tb2 ON tb1.ticket_user_id=tb2.user_id INNER JOIN ticket_types tb3 ON tb1.ticket_type=tb3.ticket_type WHERE ticket_id=?";

			ps1 = hcp.prepareStatement(sqlGetTicket);
			ps1.setInt(1, ticketId);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				rs.setCreationDate(dateToLong(rs1.getString("ticket_date")));
				rs.setLastModifiedDate(dateToLong(rs1.getString("ticket_lastmodified")));
				rs.setUserId(rs1.getString("ticket_user_id"));
				rs.setDueDate(dateToString(rs1.getDate("due_date")));
				rs.setType(rs1.getString("ticket_type"));
				rs.setScript(rs1.getString("script"));
				rs.setStatus(rs1.getString("ticket_status"));
				rs.setUserName(rs1.getString("user_full_name"));
				rs.setProfilePic(rs1.getString("user_profile_picture"));
				rs.setSublocality(rs1.getString("user_sublocality"));
				rs.setLocality(rs1.getString("user_locality"));

				String sqlGetNotes = "SELECT * FROM `ticket_notes` WHERE ticket_id=? ORDER BY note_date DESC";

				ps2 = hcp.prepareStatement(sqlGetNotes);
				ps2.setInt(1, ticketId);
				rs2 = ps2.executeQuery();

				while (rs2.next()) {
					TicketNote note = new TicketNote();
					note.setId(rs2.getInt("note_id"));
					note.setDate(dateToLong(rs2.getString("note_date")));
					note.setNote(rs2.getString("note"));

					rs.addNote(note);
				}

				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);

				LOGGER.info("Got the ticket details for ticket id - " + ticketId);

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

	public GetTicketsByXListResObj getTicketsByX(Filter_Status filterStatus, String ticketUserId, int cookie,
			int limit) {

		LOGGER.info("Inside getTicketsByX method");

		GetTicketsByXListResObj rs = new GetTicketsByXListResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			// Prepare SQL
			String sql = "SELECT tb1.*, tb2.user_profile_picture FROM tickets tb1 INNER JOIN users tb2 ON tb1.ticket_user_id=tb2.user_id WHERE";

			if (filterStatus.equals(Filter_Status.DONE)) {
				sql = sql + " tb1.ticket_status='CLOSED'";
			} else if (filterStatus.equals(Filter_Status.DUE)) {
				sql = sql + " tb1.ticket_status='OPEN' AND tb1.due_date <= CURRENT_TIMESTAMP";
			} else if (filterStatus.equals(Filter_Status.PENDING)) {
				sql = sql + " tb1.ticket_status='OPEN' AND tb1.due_date > CURRENT_TIMESTAMP";
			}

			if (ticketUserId != null)
				sql = sql + " AND tb1.ticket_user_id='" + ticketUserId + "'";

			sql = sql + " ORDER BY tb1.due_date DESC LIMIT " + cookie + ", " + limit;

			ps1 = hcp.prepareStatement(sql);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				GetTicketsByXResObj ticket = new GetTicketsByXResObj();
				ticket.setTicketId(rs1.getInt("ticket_id"));
				ticket.setTicketDate(dateToString(rs1.getDate("ticket_date")));
				ticket.setTicketUserId(rs1.getString("ticket_user_id"));
				ticket.setDueDate(dateToString(rs1.getDate("due_date")));
				ticket.setTicketType(rs1.getString("ticket_type"));
				ticket.setProfilePic(rs1.getString("user_profile_picture"));

				rs.addTickets(ticket);
				cookie = cookie + 1;
			}

			rs.setOffset(cookie);
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
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

		return rs;
	}

	private String dateToString(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String d = df.format(date);
		return d;
	}

	private long dateToLong(String d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public GetTicketTypesResObj getTicketTypes() {
		LOGGER.info("Inside getTicketTypes Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		GetTicketTypesResObj rs = new GetTicketTypesResObj();

		try {

			String sqlGetTicketType = "SELECT * FROM `ticket_types`";

			ps1 = hcp.prepareStatement(sqlGetTicketType);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				rs.addType(rs1.getString("ticket_type"));
				rs.addScript(rs1.getString("script"));
				rs.addDue(rs1.getInt("due_date"));
			}

			if (!rs.getTypes().isEmpty()) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				rs.setCode(FLS_TICKET_TYPES_LIST_FAIL);
				rs.setMessage(FLS_TICKET_TYPES_LIST_FAIL_M);
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

		return rs;
	}

}
