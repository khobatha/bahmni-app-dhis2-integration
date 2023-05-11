package com.possible.dhis2int.db;

import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.audit.Recordlog;
import com.possible.dhis2int.scheduler.PharmacyPeriod;
import com.possible.dhis2int.scheduler.PharmacySchedule;
import com.possible.dhis2int.scheduler.Schedule;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;

@Service
public class DatabaseDriver {

	private final Logger logger = getLogger(DatabaseDriver.class);

	private Properties properties;
	private final static Integer INDENT_FACTOR = 1;

	@Autowired
	public DatabaseDriver(Properties properties) {
		this.properties = properties;
	}

	public Results executeQuery(String formattedSql, String type) throws DHISIntegratorException {
		Connection connection = null;
		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			if ("ElisGeneric".equalsIgnoreCase(type)) {
				connection = DriverManager.getConnection(properties.openelisDBUrl);
			}
			if ("ERPGeneric".equalsIgnoreCase(type)) {
				connection = DriverManager.getConnection(properties.odooDBUrl);
			}
			ResultSet resultSet = connection.createStatement().executeQuery(formattedSql);
			return Results.create(resultSet);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION, formattedSql), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public ResultSet executeQuery(String formattedSql) throws DHISIntegratorException {
		Connection connection = null;
		String type = "MRSGeneric";
		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			if ("ElisGeneric".equalsIgnoreCase(type)) {
				connection = DriverManager.getConnection(properties.openelisDBUrl);
			}
			ResultSet resultSet = connection.createStatement().executeQuery(formattedSql);
			return resultSet;
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION, formattedSql), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeCreateQuery(Schedule record)
			throws DHISIntegratorException {
		logger.debug("Inside executeUpdateQuery method");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO dhis2_schedules (report_name,report_id,frequency,enabled,created_by,created_date,target_time) VALUES (?, ?, ?, ?, ?, ?, ?)");

			ps.setString(1, record.getProgramName());
			ps.setInt(2, record.getReportId());
			ps.setString(3, record.getFrequency());
			ps.setBoolean(4, record.getEnabled());
			ps.setString(5, record.getCreatedBy());
			ps.setString(6, record.getCreatedDate().toString());
			ps.setString(7, record.getTargetDate().toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeCreateQuery(PharmacySchedule record)
			throws DHISIntegratorException {
		logger.debug("Inside executeUpdateQuery method -- create pharmacy schedule");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO dhis2_schedules (report_name,report_id,enabled,created_by,created_date) VALUES (?, ?, ?, ?, ?)",Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, record.getProgramName());
			ps.setInt(2, record.getReportId());
			ps.setBoolean(3, record.getEnabled());
			ps.setString(4, record.getCreatedBy());
			ps.setString(5, record.getCreatedDate().toString());
			//ps.setString(6, record.getTargetDate().toString());
			ps.executeUpdate();

			//Get id of schedule just inserted
            ResultSet generatedKeys = ps.getGeneratedKeys();
			
			if (generatedKeys.next()) {
    			int scheduleId = generatedKeys.getInt(1);
				logger.info("ID of the recent schedule insert is "+scheduleId);

				//Now insert in periods table
				for (PharmacyPeriod period : record.getPeriods()){
					PreparedStatement ps1 = connection.prepareStatement(
						"INSERT INTO dhis2_pharmacy_periods (dhis2_schedule_id, period, enabled,created_by,created_date,start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)");

					ps1.setInt(1, scheduleId);	
					ps1.setInt(2, period.getPeriod());
					ps1.setBoolean(3, period.isEnabled());
					ps1.setString(4, period.getCreatedBy());
					ps1.setString(5, period.getCreatedDate().toString());
					ps1.setString(6, period.getStartTime());
					ps1.setString(7, period.getEndTime());

					ps1.executeUpdate();
				}
			}
			else{
				logger.info("Failed to retrieve ID of recently inserted schedule...");

			}
            
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeUpdateQuery(Integer scheduleId, Boolean enabled)
			throws DHISIntegratorException {
		logger.debug("Inside executeUpdateQuery method ... enable/disable schedule");
		Integer schedule_id = scheduleId;
		Boolean schedule_enabled = enabled;
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"UPDATE dhis2_schedules SET enabled =" + schedule_enabled + " WHERE id=" + schedule_id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeUpdateQuery(Integer scheduleId, LocalDate targetDate, String status)
			throws DHISIntegratorException {
		LocalDateTime dateTimeNow = LocalDateTime.now();
		logger.debug("Inside executeUpdateQuery method ... edit target date and last run");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);

			if (targetDate == null) { // unsuccessful schedule run
				PreparedStatement ps = connection.prepareStatement(
						"UPDATE dhis2_schedules SET status = '" + status + "', last_run = '" + dateTimeNow
								+ "' WHERE id=" + scheduleId);
				ps.executeUpdate();
			} else { // successful schedule run so set new target date as well
				PreparedStatement ps = connection.prepareStatement(
						"UPDATE dhis2_schedules SET target_time ='" + targetDate + "', status = '" + status
								+ "', last_run = '" + dateTimeNow
								+ "' WHERE id=" + scheduleId);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeDeleteQuery(Integer scheduleId)
			throws DHISIntegratorException {
		logger.debug("Inside executeDeleteQuery method");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM dhis2_schedules WHERE id=" + scheduleId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void executeDeletePharmSchedQuery(Integer scheduleId)
			throws DHISIntegratorException {
		logger.debug("Inside executeDeletePharmSchedQuery method");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
            //Execute the two SQLs as a transaction
			//delete from pharmacy periods first, then from schedules
			connection.setAutoCommit(false);
			try (PreparedStatement stmt1 = connection.prepareStatement("DELETE FROM dhis2_pharmacy_periods WHERE dhis2_schedule_id=" + scheduleId)) { // Automatic close.
				
				try (PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM dhis2_schedules WHERE id=" + scheduleId) ){
					connection.commit();
				}
			} catch (SQLException ex) {
				connection.rollback();
				throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), ex);

			}
		}  catch (SQLException ex) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), ex);

		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void recordQueryLog(Recordlog record, Integer month, Integer year) throws DHISIntegratorException {
		logger.debug("Inside recordQueryLog method");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO dhis2_log (report_name, submitted_date, submitted_by, report_log ,status, comment, report_month, report_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			Timestamp time = new Timestamp(record.getTime().getTime());
			ps.setString(1, record.getEvent());
			ps.setTimestamp(2, time);
			ps.setString(3, record.getUserId());
			ps.setString(4, record.getLog());
			ps.setString(5, record.getStatus().toString());
			ps.setString(6, record.getComment());
			ps.setInt(7, month);
			ps.setInt(8, year);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public String getQuerylog(String programName, Integer month, Integer year) throws DHISIntegratorException {
		logger.info("Inside getQueryLog method");
		ResultSet resultSet = null;
		Connection connection = null;
		String log = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(

					"SELECT * FROM  dhis2_log WHERE report_name = ? AND report_month = ? AND report_year = ? ORDER BY submitted_date DESC LIMIT 1");
			ps.setString(1, programName);
			ps.setInt(2, month);
			ps.setInt(3, year);
			resultSet = ps.executeQuery();
			JSONObject jsonObject = new JSONObject();
			while (resultSet.next()) {
				jsonObject.put("status", resultSet.getString(5));
				jsonObject.put("comment", resultSet.getString(6));
				jsonObject.put("response", resultSet.getString(7));
			}
			log = jsonObject.toString(INDENT_FACTOR);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DHISIntegratorException("DB Exception occurred " + e.getMessage(), e);
		} catch (JSONException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}

		return log;
	}

	public void createTempTable(Integer numberOfMaleLessThanSix, Integer numberOfFemalesLessThanSix,
			Integer numberOfMalesMoreThanSix, Integer numberOfFemalesMoreThanSix) throws DHISIntegratorException {
		logger.info("Inside create temp table method.");

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS imam");
			statement.executeUpdate(
					"CREATE TABLE imam(male_less_than_six int, female_less_than_six int, male_more_than_six int, female_more_than_six int)");
			String insertImamData = new StringBuffer(
					"INSERT INTO imam(male_less_than_six , female_less_than_six , male_more_than_six , female_more_than_six) SELECT ")
					.append(numberOfMaleLessThanSix).append(", ").append(numberOfFemalesLessThanSix)
					.append(", ").append(numberOfMalesMoreThanSix).append(", ")
					.append(numberOfFemalesMoreThanSix).toString();
			statement.executeUpdate(insertImamData);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to create table imam"), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void dropImamTable() throws DHISIntegratorException {
		logger.info("Inside dropImamTable method.");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS imam");
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to drop table imam"), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void createTempFamilyTable(Integer numberOfVasectomyUser, Integer numberOfPillsUser,
			Integer numberOfOtherUser, Integer numberOfMinilipUser, Integer numberOfIUCDUser,
			Integer numberOfImplantUser, Integer numberOfDepoUser, Integer numberOfCondomsUser)
			throws DHISIntegratorException {
		logger.info("create family planning temp table method.");

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS familyPlanning");
			statement.executeUpdate(
					"CREATE TABLE familyPlanning(vasectomy_user int, pills_user int, other_user int, minilap_user int,IUCD_user int, implant_user int, depo_user int, condoms_user int)");

			String insertFamilyPlanningData = new StringBuffer(
					"INSERT INTO familyPlanning(vasectomy_user, pills_user, other_user,  minilap_user, IUCD_user, implant_user, depo_user, condoms_user) SELECT ")
					.append(numberOfVasectomyUser).append(", ").append(numberOfPillsUser).append(", ")
					.append(numberOfOtherUser).append(", ").append(numberOfMinilipUser).append(", ")
					.append(numberOfIUCDUser).append(", ").append(numberOfImplantUser).append(", ")
					.append(numberOfDepoUser).append(", ").append(numberOfCondomsUser).toString();
			statement.executeUpdate(insertFamilyPlanningData);

		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to create table familyPlanning"), e);
		} finally {
			if (connection != null) {
				try {

					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

}