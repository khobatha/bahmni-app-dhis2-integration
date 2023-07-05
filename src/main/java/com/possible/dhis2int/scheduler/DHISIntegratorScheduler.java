package com.possible.dhis2int.scheduler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.possible.dhis2int.Properties;
import com.possible.dhis2int.db.DatabaseDriver;
import com.possible.dhis2int.db.Results;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;
import com.possible.dhis2int.scheduler.Schedule;
import com.possible.dhis2int.scheduler.PharmacyPeriodReq;

import org.apache.log4j.Logger;
import org.apache.tomcat.jni.Local;
import org.joda.time.Months;

import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
@RestController
public class DHISIntegratorScheduler {

	private final DatabaseDriver databaseDriver;
	private final Logger logger = getLogger(DHISIntegratorScheduler.class);
	private final Properties properties;
	private final String SUBMISSION_ENDPOINT = "/dhis-integration/submit-to-dhis";
	private final String OPENMRS_LOGIN_ENDPOINT = "/session";

	@Autowired
	public DHISIntegratorScheduler(DatabaseDriver databaseDriver, Properties properties) {
		this.databaseDriver = databaseDriver;
		this.properties = properties;
	}

	@RequestMapping(path = "/get-schedules")
	public JSONArray getIntegrationSchedules(HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException, DHISIntegratorException, Exception {

		String sql0 = "SELECT id, name from dhis2_report_type WHERE name = 'MRSGeneric'";
		String type = "MRSGeneric";
		Results results0 = new Results();
		String reportNameId = "";
		try {
			results0 = databaseDriver.executeQuery(sql0, type);

			for (List<String> row : results0.getRows()) {
				reportNameId = row.get(0);
			}
		} catch (DHISIntegratorException e) {
		// logger.info("Inside loadIntegrationSchedules...");
		logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}
				
		String sql = "SELECT id, report_name, report_id, frequency, enabled, last_run, target_time, status FROM dhis2_schedules WHERE report_id ='"+reportNameId+"';";
		JSONArray jsonArray = new JSONArray();
		ArrayList<Schedule> list = new ArrayList<Schedule>();
		Results results = new Results();
		//String type = "MRSGeneric";
		Schedule schedule;
		ObjectMapper mapper;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				
				logger.info("Showing getIntegrationSChedules results...");
				logger.info(row);
				schedule = new Schedule();

				schedule.setId(Integer.parseInt(row.get(0)));
				schedule.setProgName(row.get(1));
				schedule.setReportId(Integer.parseInt(row.get(2)));
				schedule.setFrequency(row.get(3));
				schedule.setEnabled(Integer.parseInt(row.get(4)) == 1 ? true : false);
				schedule.setLastRun(row.get(5));
				schedule.setTargetDate(row.get(6));
				logger.info("Target date is "+LocalDate.parse(row.get(6),formatter));
				schedule.setStatus(row.get(7));
				list.add(schedule);
			}
		
			mapper = new ObjectMapper();
			String jsonstring = mapper.writeValueAsString(list);
			jsonArray.put(jsonstring);
			logger.info("Task loadIntegrationSchedules ran successfully...");
		} catch (DHISIntegratorException | JSONException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return jsonArray;
	}

	@RequestMapping(path = "/get-pharm-schedules")
	public JSONArray getIntegrationPharmSchedules(HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException, DHISIntegratorException, Exception {
		
		String sql0 = "SELECT id, name from dhis2_report_type WHERE name = 'ERPGeneric'";
		String type = "MRSGeneric";
		Results results0 = new Results();
		String reportNameId = "";
		try {
			results0 = databaseDriver.executeQuery(sql0, type);

			for (List<String> row : results0.getRows()) {
				reportNameId = row.get(0);
			}
		} catch (DHISIntegratorException e) {
		// logger.info("Inside loadIntegrationSchedules...");
		logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}
		
		String sql = "SELECT id, report_name, report_id, frequency, enabled, last_run, target_time, status FROM dhis2_schedules WHERE report_id ='"+reportNameId+"';";
		JSONArray jsonArray = new JSONArray();
		ArrayList<PharmacySchedule> list = new ArrayList<PharmacySchedule>();
		ArrayList<PharmacyPeriod> pharmacyPeriods = new ArrayList<PharmacyPeriod>();
		Results results = new Results();
		Results pharmacyPeriodResults = new Results();
		//String type = "MRSGeneric";
		//Schedule schedule;
		PharmacySchedule schedule;
		ObjectMapper mapper;

		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				
				logger.info("Showing getIntegrationSChedules results...");
				logger.info(row);
				schedule = new PharmacySchedule();

				schedule.setId(Integer.parseInt(row.get(0)));
				schedule.setProgName(row.get(1));
				schedule.setReportId(Integer.parseInt(row.get(2)));
				schedule.setFrequency(row.get(3));
				schedule.setEnabled(Integer.parseInt(row.get(4)) == 1 ? true : false);
				schedule.setLastRun(row.get(5));
				schedule.setTargetDate(row.get(6));
				schedule.setStatus(row.get(7));
								
				list.add(schedule);
			}
			
			mapper = new ObjectMapper();
			String jsonstring = mapper.writeValueAsString(list);
			jsonArray.put(jsonstring);
			logger.info("Task loadIntegrationSchedules ran successfully...");
		} catch (DHISIntegratorException | JSONException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return jsonArray;
	}

	@RequestMapping(path = "/get-pharm-schedule-periods")
	public JSONArray getIntegrationPharmSchedulePeriods(HttpServletRequest clientReq, 
					HttpServletResponse clientRes, 
					@RequestParam("pharmschedid") String pharmSchedId)
			throws IOException, JSONException, DHISIntegratorException, Exception {
		
		String periodsSql = "SELECT id, dhis2_schedule_id, period, created_by, created_date, start_time, end_time, last_run, status, enabled FROM openmrs.dhis2_pharmacy_periods WHERE dhis2_schedule_id="+pharmSchedId+";";		
		String type = "MRSGeneric";
		JSONArray jsonArray = new JSONArray();
		ArrayList<PharmacyPeriod> pharmacySchedPeriods = new ArrayList<PharmacyPeriod>();
		Results pharmacyPeriodResults = new Results();
		ObjectMapper mapper;
		
		try{
			//Get Periods for this schedule
			pharmacyPeriodResults = databaseDriver.executeQuery(periodsSql, type);
			PharmacyPeriod pharmacyPeriod = new PharmacyPeriod();
				
			for(List<String> rowList : pharmacyPeriodResults.getRows()){
				pharmacyPeriod.setId(Integer.parseInt(rowList.get(0)));
				pharmacyPeriod.setDhis2ScheduleId(Integer.parseInt(rowList.get(1)));
				pharmacyPeriod.setPeriod(Integer.parseInt(rowList.get(2)));
				pharmacyPeriod.setCreatedBy(rowList.get(3));
				// LocalDate dateNow = LocalDate.of(1995, 1, 20);
				// pharmacyPeriod.setCreatedDate(dateNow);
				// pharmacyPeriod.setCreatedDate(rowList.get(4));
				pharmacyPeriod.setStartTime(rowList.get(5));
				pharmacyPeriod.setEndTime(rowList.get(6));
				pharmacyPeriod.setLastRun(rowList.get(7));
				pharmacyPeriod.setStatus(rowList.get(8));
				pharmacyPeriod.setEnabled(Integer.parseInt(rowList.get(9)) == 1 ? true : false);
					
				pharmacySchedPeriods.add(pharmacyPeriod);
			}
			
			mapper = new ObjectMapper();
			String jsonstring = mapper.writeValueAsString(pharmacySchedPeriods);
			jsonArray.put(jsonstring);
			logger.info("Task loadPharmSchedulePeriods ran successfully...");
		} catch (DHISIntegratorException | JSONException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return jsonArray;
	}


	@RequestMapping(path = "/create-schedule")
	public Boolean createIntegrationSchedule(@RequestParam("reportName") String reportName,
			@RequestParam("reportTypeName") String reportTypeName,
			@RequestParam("scheduleFrequency") String schedFrequency,
			@RequestParam("scheduleTime") String schedTime, HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Boolean created = true;
		Schedule newschedule = new Schedule();
		newschedule.setProgName(reportName);
		newschedule.setFrequency(schedFrequency);
		newschedule.setCreatedBy("Test");
		newschedule.setEnabled(true);
		newschedule.setStatus("Ready");

		LocalDate created_date = LocalDate.now();
		String target_date = getMonthlyTargetDate(created_date).toString();
		newschedule.setCreatedDate(created_date);
		newschedule.setTargetDate(target_date);

		String sql = "SELECT id, name from dhis2_report_type WHERE name = '"+reportTypeName+"';";
		String type = "MRSGeneric";
		Results results = new Results();
		logger.info("Inside create clinical schedules...");
		try {
			results = databaseDriver.executeQuery(sql, type);
			logger.info("Trying to find the report type ID...");
			for (List<String> row : results.getRows()) {
				newschedule.setReportId(Integer.parseInt(row.get(0)));
				logger.info("Parameter report type name as "+reportTypeName);
				logger.info("Parsed report type ID as "+Integer.parseInt(row.get(0)));
				logger.info("Set report type ID as "+newschedule.getReportId() );
			}
		} catch (DHISIntegratorException e) {
			logger.info("Failed to find the report type ID...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.info("Failed to find the report type ID...");
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		logger.info("Inside saveIntegrationSchedules...");
		try {
			databaseDriver.executeCreateQuery(newschedule);
			logger.info("Executed insert query successfully...");

		} catch (DHISIntegratorException | JSONException e) {
			created = false;
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			created = false;
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return created;
	}

	@RequestMapping(path = "/create-pharm-schedule")
	public Boolean createIntegrationPharmSchedule(@RequestParam("reportName") String reportName,
	        @RequestParam("reportTypeName") String reportTypeName,
			@RequestParam("scheduleFrequency") String schedFrequency,
			@RequestParam("scheduleTime") String schedTime, 
			@RequestBody String pharmacyPeriodListRequest,
			HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Boolean created = false;
		logger.info("[Creating new pharmacy schedule ...]");
		ObjectMapper mapper=new ObjectMapper();
		List<PharmacyPeriodReq> periods=mapper.readValue(pharmacyPeriodListRequest, new TypeReference<List<PharmacyPeriodReq>>() {});
		
		PharmacySchedule newPharmacySchedule = new PharmacySchedule();
		newPharmacySchedule.setProgName(reportName);
		newPharmacySchedule.setFrequency(schedFrequency);
		newPharmacySchedule.setCreatedBy("Test");
		newPharmacySchedule.setEnabled(true);
		newPharmacySchedule.setStatus("Ready");
		
		LocalDate created_date = LocalDate.now();
		newPharmacySchedule.setCreatedDate(created_date);
		logger.info("[Extracting periods from the request body ...]");
		logger.info("[Request body argument - periods string is ...]"+pharmacyPeriodListRequest);
		logger.info("[Start date of first deserialised period object is ...]"+periods.get(0).getStart());
		
		// convert from type PharmacyPeriodReq to PharmacyPeriod
		List <PharmacyPeriod> pharmacyPeriods=new ArrayList <>();
		int count=0;
		for (PharmacyPeriodReq pharmacyPeriod : periods) {
			if(!pharmacyPeriod.getStart().isEmpty()){
				PharmacyPeriod temp=new PharmacyPeriod();
				temp.setStartTime(pharmacyPeriod.getStart());
				logger.info("[New schedule start date value is ...]"+temp.getStartTime());
				temp.setEndTime(pharmacyPeriod.getEnd());
				temp.setCreatedBy(newPharmacySchedule.getCreatedBy());
				logger.info("[New schedule created by value is ...]"+temp.getCreatedBy());
				temp.setCreatedDate(newPharmacySchedule.getCreatedDate());
				logger.info("[New schedule created date value is ...]"+temp.getCreatedDate());
				temp.setEnabled(true);
				logger.info("[New schedule enabled value is ...]"+temp.getEnabled());
				temp.setPeriod(count);
				temp.setStatus("Ready");
				count++;
				pharmacyPeriods.add(temp);
			}
			
		}

		newPharmacySchedule.setPeriods(pharmacyPeriods);
		logger.info("[Start date of first period of new schedule is...]"+newPharmacySchedule.getPeriods().get(0).getStartTime());
		logger.info("[Number of periods for the new schedule is...]"+newPharmacySchedule.getPeriods().size());
		newPharmacySchedule.setTargetDateInit();
		logger.info("[Target date of new schedule set...as...]"+newPharmacySchedule.getTargetDate());

		String sql = "SELECT id, name from dhis2_report_type WHERE name = '"+reportTypeName+"';";
		String type = "MRSGeneric";
		Results results = new Results();
		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				newPharmacySchedule.setReportId(Integer.parseInt(row.get(0)));
				logger.info("Parsed report type ID as "+Integer.parseInt(row.get(0)));
				logger.info("Set report type ID as "+newPharmacySchedule.getReportId() );
			}
		} catch (DHISIntegratorException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		logger.info("Inside saveIntegrationSchedules...");
		try {
			databaseDriver.executeCreateQuery(newPharmacySchedule);
			logger.info("Executed insert query successfully...");
			created = true;

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return created;
	}

	@RequestMapping(path = "/disable-enable-schedule")
	public Results disenIntegrationSchedule(@RequestParam("scheduleId") String scheduleId,
			@RequestParam("enabled") Boolean enabled,
			HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Integer schedule_id = Integer.parseInt(scheduleId);
		Boolean schedule_enabled = enabled;

		Results results = new Results();
		logger.info("Inside disenIntegrationSchedule...");
		try {
			databaseDriver.executeUpdateQuery(schedule_id, schedule_enabled);
			logger.info("Executed disable/enable schedule query successfully...");

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return results;
	}

	private LocalDate getMonthlyTargetDate(LocalDate aDate) {
		// Returns a copy of this LocalDate with the day-of-month altered to the last
		// day of the month
		return aDate.withDayOfMonth(aDate.getMonth().length(aDate.isLeapYear()));
	}

	@RequestMapping(path = "/delete-schedule")
	public Results deleteIntegrationSchedule(@RequestParam(value = "scheduleIds[]") String scheduleIds[],
			HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Results results = new Results();
		logger.info("Inside deleteIntegrationSchedules..., schedule_id_0=" + scheduleIds[0]);
		try {
			for (String schedule_id : scheduleIds) {
				databaseDriver.executeDeleteQuery(Integer.parseInt(schedule_id));
				logger.info("Executed delete query successfully...");
			}

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return results;
	}

	@RequestMapping(path = "/delete-pharm-schedule")
	public Results deleteIntegrationPharmSchedule(@RequestParam(value = "scheduleIds[]") String scheduleIds[],
			HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Results results = new Results();
		logger.info("Inside deleteIntegrationPharmSchedules..., schedule_id_0=" + scheduleIds[0]);
		try {
			for (String schedule_id : scheduleIds) {
				databaseDriver.executeDeletePharmSchedQuery(Integer.parseInt(schedule_id));
				logger.info("Executed delete pharmacy schedule query successfully...");
			}

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return results;
	}


	private String getAuthToken(String username, String password) {
		Charset UTF_8 = Charset.forName("UTF-8");
		String authToken = Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
		return authToken;
	}

	/*
	 * This method builds the DHISIntegrator url (rest api endpoint and parameters)
	 */
	private String buildDHISIntegratorUrl(String reportName, Integer month, Integer year, String comment) {
		StringBuilder DHISIntegratorRequestUrl = new StringBuilder(
				properties.dhisIntegratorRootUrl + SUBMISSION_ENDPOINT);
		DHISIntegratorRequestUrl.append("?name=").append(reportName).append("&month=").append(month).append("&year=")
				.append(year)
				.append("&isImam=false&isFamily=false").append("&comment=").append(comment);
		return DHISIntegratorRequestUrl.toString();
	}

	/*
	 * Method to authenticate/login daemon user against OpenMRS
	 * Return an empty Authentication response (sessionid and session user) if auth
	 * fails
	 * Else it returns a valid sessionid and session username
	 */
	private AuthResponse authenticate(String openmrsUrl) {
		String authToken = getAuthToken(properties.dhisIntegratorSchedulerUser,
				properties.dhisIntegratorSchedulerPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.GET, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		// logger.info("Openmrs get session response: " + responseEntity.toString());
		// logger.info("Response headers: " + responseEntity.getHeaders().toString());
		Boolean authenticated = new JSONObject(new JSONTokener(responseEntity.getBody()))
				.getBoolean("authenticated");
		AuthResponse authResponse = new AuthResponse("", "");
		if (authenticated) {
			authResponse.setSessionId(new JSONObject(new JSONTokener(responseEntity.getBody())).getString("sessionId"));
			authResponse.setSessionUser(new JSONObject(new JSONTokener(responseEntity.getBody())).getJSONObject("user")
					.getString("username"));
			logger.info("Daemon user " + properties.dhisIntegratorSchedulerUser + " autheticated successfully");
		} else {
			logger.warn("Daemon user " + properties.dhisIntegratorSchedulerUser
					+ " NOT autheticated. Correct the credentials on the properties file (application.yml)");
		}
		return authResponse;
	}

	/*
	 * Log out deamon user from OpenMRS
	 */
	private void logout(String openmrsUrl) {
		String authToken = getAuthToken(properties.dhisIntegratorSchedulerUser,
				properties.dhisIntegratorSchedulerPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.DELETE, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		logger.info("Openmrs delete session response: " + responseEntity.toString());
	}

	/*
	 * This method makes a rest call to DHISIntegrator which in turn submits to
	 * DHIS2 by making
	 * a rest call to the DHIS2 rest api
	 */
	private ResponseEntity<String> submitToDHISIntegrator(String DHISIntegratorUrl, AuthResponse authResponse) {
		HttpHeaders DHISIntegratorAuthHeaders = new HttpHeaders();
		DHISIntegratorAuthHeaders.add("Cookie", "JSESSIONID=" + authResponse.getSessionId());
		DHISIntegratorAuthHeaders.add("Cookie", "reporting_session=" + authResponse.getSessionId());
		DHISIntegratorAuthHeaders.add("Cookie", "bahmni.user=" + authResponse.getSessionUser());

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(DHISIntegratorUrl, HttpMethod.GET,
				new HttpEntity<String>(DHISIntegratorAuthHeaders),
				String.class);

		logger.info("Status code: " + responseEntity.getStatusCode() + "when firing query - GET: " + DHISIntegratorUrl);
		return responseEntity;
	}

	private Boolean isDue(Schedule schedule) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime scheduleTargetDate = LocalDateTime.parse(schedule.getTargetDate(), formatter);
		return scheduleTargetDate.toLocalDate().isBefore(LocalDate.now());
	}

	private ArrayList<Schedule> getIntegrationSchedules()
			throws IOException, DHISIntegratorException, Exception {
		String sql = "SELECT id, report_name, frequency, last_run, status, target_time FROM dhis2_schedules WHERE enabled;";
		ArrayList<Schedule> list = new ArrayList<Schedule>();
		Results results = new Results();
		String type = "MRSGeneric";
		Schedule schedule;

		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				logger.info(row);
				schedule = new Schedule();

				schedule.setId(Integer.parseInt(row.get(0)));
				schedule.setProgName(row.get(1));
				schedule.setFrequency(row.get(2));
				schedule.setLastRun(row.get(3));
				schedule.setStatus(row.get(4));
				schedule.setTargetDate(LocalDate.parse(row.get(5).substring(0, 10)).toString());
				list.add(schedule);

			}
			logger.info("Inside loadIntegrationSchedules...");
		} catch (DHISIntegratorException | JSONException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}
		return list;
	}

	private void updateSchedule(Integer scheduleId, LocalDate targetDate, String status) {
		/*
		 * This method updates the target date and last run on successful submission to
		 * DHIS2 ONLY if it's the 8th day of the month ... since the 7th day is the last day for submission.
		 */
		logger.info("Inside updateIntegrationSchedule...");
		
		if (LocalDate.now().getDayOfMonth() > 7){ //update if it's the 8th and beyond
			try {
				databaseDriver.executeUpdateQuery(scheduleId, targetDate, status);
				logger.info("Executed edit schedule query successfully...");
	
			} catch (DHISIntegratorException | JSONException e) {
				logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
			} catch (Exception e) {
				logger.error(Messages.INTERNAL_SERVER_ERROR, e);
			}
		}
	}

	private ArrayList<MonthlyPeriod> getDuePeriods(LocalDate targetDate, LocalDate today) {
		ArrayList<MonthlyPeriod> duePeriods = new ArrayList<MonthlyPeriod>();
		LocalDate startDate = targetDate;
		while (startDate.isBefore(today)) {
			duePeriods.add(new MonthlyPeriod(startDate.getMonthValue(), startDate.getYear()));
			startDate = startDate.plusMonths(1);
		}
		return duePeriods;
	}

	private Boolean isSubmissionSuccessful(ResponseEntity<String> response) {
		JSONObject responseBody = new JSONObject(new JSONTokener(response.getBody()));
		if (response == null || response.getStatusCodeValue() != 200
				|| !(responseBody.getString("status").equals("Success"))) {
			return false;
		}
		return true;
	}
    
	// schedule submission at 9am from the 1st to 8th (assuming up to date data will have been entered on the 7th - last day) of every month
	@Scheduled(cron = "0 0 7 1-8 * *")
	public void processSchedules() {
		// get schedules
		ArrayList<Schedule> schedules = new ArrayList<Schedule>();
		try {
			schedules = getIntegrationSchedules();
			// for each item, is this report due
			// if due => call submitToDHIS(x,y,z)
			for (int i = 0; i < schedules.size(); i++) {
				logger.info("Current item " + schedules.get(i).getProgramName());
				Schedule currSchedule = schedules.get(i);
				switch (currSchedule.getFrequency()) {
					case "daily":
						// daily logic
						logger.info("Processing a daily schedule at " + LocalDate.now() + "for report "
								+ currSchedule.getProgramName());
						break;
					case "weekly":
						// weekly logic
						logger.info("Processing a weekly schedule at " + LocalDate.now() + "for report "
								+ currSchedule.getProgramName());
						break;
					case "monthly":
						// monthly logic
						logger.info("Processing a montly schedule at " + LocalDate.now() + "for report "
								+ currSchedule.getProgramName());
						if (isDue(currSchedule)) {
							// ArrayList<MonthlyPeriod> currSchedDuePeriods =
							// getDuePeriods(currSchedule.getTargetDate(), LocalDate.now());
							// loop over due periods
							// send report
							logger.info("The following report is due " + currSchedule.getProgramName());
							// extract period
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        					LocalDateTime dateTime = LocalDateTime.parse(currSchedule.getTargetDate(), formatter);
							Integer year = dateTime.getYear();
							Integer month = dateTime.getMonthValue();
							String comment = "DHISIntegratorScheduler submitted " + currSchedule.getProgramName()
									+ " on " + LocalDate.now();

							if(currSchedule.getProgramName().equals("PHARM-001 Pharmacy ARV Regimen") || currSchedule.getProgramName().equals("PHARM-003 Dispensing Summary Report")){
								if(month == 12){
									month = 1;
								}else{
									month = month + 1;
								}
							}			
							String DHISIntegratorUrl = buildDHISIntegratorUrl(currSchedule.getProgramName(), month,
									year, comment);
							AuthResponse authResponse = authenticate(
									properties.openmrsRootUrl + OPENMRS_LOGIN_ENDPOINT);
							ResponseEntity<String> responseEntity = null;
							if (authResponse.getSessionId() != "") {
								responseEntity = submitToDHISIntegrator(DHISIntegratorUrl, authResponse);
							}
							logout(properties.openmrsRootUrl + OPENMRS_LOGIN_ENDPOINT);

							// if submitted successfully, set new target, else leave it to be retried.
							if (isSubmissionSuccessful(responseEntity)) {
								// determine & set new target date
								LocalDate newTatgetDate = getMonthlyTargetDate(LocalDate.now());
								String status = "Success";
								updateSchedule(currSchedule.getId(), newTatgetDate, status);
								logger.info("Submission went through ... :-)");
								logger.info("Response body: " + responseEntity.getBody());

							} else {
								LocalDate newTatgetDate = null;
								String status = "Failure";
								updateSchedule(currSchedule.getId(), newTatgetDate, status);
								logger.info("Submission did not go through ... :-(");
								logger.info("Response body: " + responseEntity.getBody());
							}
						} else {
							logger.info("The following report is NOT due " + currSchedule.getProgramName());
						}
						break;
					case "quarterly":
						// quarterly logic
						logger.info("Processing a quarterly schedule at " + LocalDate.now() + "for report "
								+ currSchedule.getProgramName());
						break;
					case "yearly":
						// yearly logic
						logger.info("Processing a yearly schedule at " + LocalDate.now() + "for report "
								+ currSchedule.getProgramName());
						break;
					default:
						// Ache banna, how did we get here?? .. do nothing
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * logger.info("Executing schedule at :" + new Date().toString());
		 * 
		 * // get from DB
		 * 
		 * Integer month = 6;
		 * Integer year = 2020;
		 * String reportName = "TESTS-01 DHIS Integration App Sync Test";
		 * String comment = "Submitted by daemon";
		 * 
		 * String DHISIntegratorUrl = buildDHISIntegratorUrl(reportName, month, year,
		 * comment);
		 * String openmrsUrl = properties.openmrsRootUrl + OPENMRS_LOGIN_ENDPOINT;
		 * System.out.println("DHISIntegrator url: " + DHISIntegratorUrl);
		 * 
		 * AuthResponse authResponse = authenticate(openmrsUrl);
		 * 
		 * //
		 * if (authResponse.getSessionId() != "") {
		 * submitToDHISIntegrator(DHISIntegratorUrl, authResponse);
		 * }
		 * logout(openmrsUrl);
		 * // logout when done
		 * getSchedules();
		 */
	}

	@Scheduled(cron = "0 0/5 * * * *")
	public void runDailyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the daily task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 * * 0")
	public void runWeeklyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the weekly task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 28-31 * *")
	public void runMonthlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the monthly task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 28-31 3,6,9,12 *")
	public void runQuarterlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the quarterly task. Now is" + new Date());

	}

	/*
	 * public ArrayList<Schedules> getDueReports(String period){
	 * ArrayList<Schedules> list=new ArrayList<Schedules>();
	 * String
	 * sql="SELECT id, report_name FROM dhis2_schedules where frequency='"+period+
	 * "';";
	 * String type="MRSGeneric";
	 * Schedules schedule;
	 * Results results=new Results();
	 * 
	 * try{
	 * results = databaseDriver.executeQuery(sql,type);
	 * 
	 * for (List<String> row : results.getRows()) {
	 * logger.info(row);
	 * schedule=new Schedules();
	 * 
	 * schedule.setId(Integer.parseInt(row.get(0)));
	 * schedule.setProgName(row.get(1));
	 * list.add(schedule);
	 * 
	 * }
	 * }
	 * catch(DHISIntegratorException | JSONException e){
	 * logger.error(Messages.SQL_EXECUTION_EXCEPTION,e);
	 * }
	 * catch(Exception e){
	 * logger.error(Messages.INTERNAL_SERVER_ERROR,e);
	 * }
	 * 
	 * 
	 * return list;
	 * 
	 * }
	 */

}