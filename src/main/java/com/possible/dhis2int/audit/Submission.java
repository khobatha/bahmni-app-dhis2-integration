package com.possible.dhis2int.audit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.ResponseEntity;

import static org.apache.log4j.Logger.getLogger;
import org.apache.log4j.Logger;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;

public class Submission {

	private final Logger logger = getLogger(DHISIntegrator.class);

	public static final String FILE_NAME = "'data_submitted_on_'yyyyMMddHHmmss'.json'";

	private final String fileName;

	private JSONObject postedData;

	private ResponseEntity<String> response;

	private Exception exception;

	private Status status;

	private final static Integer INDENT_FACTOR = 1;

	public Submission() {
		this.fileName = DateTimeFormat.forPattern(FILE_NAME).print(new DateTime());
	}

	public String toStrings() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("response", response == null ? null : response.getBody());
		jsonObject.put("exception", exception);
		jsonObject.put("request", postedData);
		return jsonObject.toString(INDENT_FACTOR);
	}

	public ResponseEntity<String> getResponseEntity() {
		return response;
	}

	public String getFileName() {
		return fileName;
	}

	public JSONObject getPostedData() {
		return postedData;
	}

	public void setPostedData(JSONObject postedData) {
		this.postedData = postedData;
	}

	public void setResponse(ResponseEntity<String> response) {
		this.response = response;
	}

	public void setStatus(Status stat) {
		this.status = stat;
	}

	public Status retrieveStatus() {
		return status;
	}

	public Status getStatus() throws JSONException {
		logger.info("[DEBUG] Response string is "+response.getBody());
		logger.info("[DEBUG] Response string value code is "+response.getStatusCodeValue());
		if (response == null || response.getStatusCodeValue() != 200) {
			return Status.Failure;
		}
		JSONObject responseBody;
		try {
			responseBody = new JSONObject(new JSONTokener(response.getBody()));
		} catch (JSONException e) {
			logger.info("[DEBUG] Exception trying to retrieve responseBody!");
			return Status.Failure;
		}
		if (isServerError(responseBody) || isIgnored(responseBody) || hasConflicts(responseBody)) {
			logger.info("[DEBUG] Either server error, ignored and/or has conflicts!");
			return Status.Failure;
		}
		return Status.Success;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getInfo() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", getStatus());
		jsonObject.put("exception", exception);
		jsonObject.put("response", response == null ? null : response.getBody());
		return jsonObject.toString(INDENT_FACTOR);
	}

	private boolean hasConflicts(JSONObject responseBody) {
		try {
			// Navigate to the nested "conflicts" array within "response"
			if (responseBody.has("response")) {
				JSONObject response = responseBody.getJSONObject("response");
				if (response.has("conflicts")) {
					return response.getJSONArray("conflicts").length() > 0;
				}
			}
		} catch (JSONException e) {
			logger.error("Error checking conflicts", e);
		}
		return false;
	}
	

	private boolean isIgnored(JSONObject responseBody) throws JSONException {
		JSONObject importCount = responseBody.getJSONObject("response").getJSONObject("importCount");
		return importCount.getInt("ignored") > 0;
	}
	
	private boolean isServerError(JSONObject responseBody) {
		try {
			return "ERROR".equals(responseBody.getString("status"));
		} catch (JSONException e) {
			logger.error("Error checking server error status", e);
			return false;
		}
	}
	

	public enum Status {
		Success,
		Failure,
		Complete,
		Incomplete
	}
}
