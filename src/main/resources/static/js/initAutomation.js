var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
var getSchedulesUrl = '/dhis-integration/get-schedules';
var createScheduleUrl = '/dhis-integration/create-schedule';
var createPharmScheduleUrl = '/dhis-integration/create-pharm-schedule';
var deleteScheduleUrl='/dhis-integration/delete-schedule';
var disenScheduleUrl='/dhis-integration/disable-enable-schedule';
var submitUrlAtr = '/dhis-integration/submit-to-dhis-atr';
var loginRedirectUrl = '/bahmni/home/index.html#/login?showLoginMessage&from=';
var NUTRITION_PROGRAM = '03-2 Nutrition Acute Malnutrition';
var FAMILYPLANNING_PROGRAM = '07 Family Planning Program';
var logUrl = '/dhis-integration/log';
var fiscalYearReportUrl = '/dhis-integration/download/fiscal-year-report?name=NAME&startYear=START_YEAR&startMonth=START_MONTH&endYear=END_YEAR&endMonth=END_MONTH&isImam=IS_IMAM';
var supportedStartDate = 2033;
var supportedEndDate = 2008;
var approximateNepaliYear = (new Date()).getFullYear() + 56;
var spinner = spinner || {};

var hasReportingPrivilege = true;

$(document).ready(
		function() {

			initTabs();
			initDHISProgramNameDropdowns();
			renderDHISSchedules();
			addPharmScheduleFrequencyEventListener();
			addCustomPeriodCheckboxEventListener();
			
});

function addPharmScheduleFrequencyEventListener(){
// if monthly frequency selected for a pharmacy report, display checkbox for determining if report has
// custom monthly periods or not
	var select = document.getElementById("pharmacy-frequency");
	var checkbox = document.getElementById("is-custom-monthly-reporting-periods");

	select.addEventListener("change", function() {
	if (select.value === "monthly") {
		checkbox.style.display = "block";
	} else {
		checkbox.style.display = "none";
	}
	});
}

function addCustomPeriodCheckboxEventListener(){
// if the custom monthly reporting periods checkbox is selected, display the custom monthly reporting periods
// entry table
		var checkbox = document.getElementById("is-custom-monthly-reporting-periods-checkbox");
		var div = document.getElementById("custom-monthly-reporting-periods-entry-table");
	
		checkbox.addEventListener("change", function() {
		if (checkbox.checked) {
			div.style.display = "block";
		} else {
			div.style.display = "none";
		}
		});
	}

//populate and render list of schedules from db
function renderDHISSchedules(){
	getDHISSchedules().then(function(data){
		console.log('[render hmis program schedules]');
		console.log(data);
		//alert(data);
		var clinicalSchedulesTable = document.getElementById('clinical-program-schedules');
		var pharmacySchedulesTable = document.getElementById('pharmacy-program-schedules');
		var LabSchedulesTable = document.getElementById('lab-program-schedules');
		var schedules=JSON.parse(data);
		schedules.forEach(function(object) {
			console.log(object);
			var tr = document.createElement('tr');
			var tempHTML ="<td>"+"<span class='custom-checkbox'>"+
							"<input class='selectSchedule' type='checkbox' id='checkbox1' name='options[]' value='"+object.id+"'/>"+
							"<label for='checkbox1'></label>"+"</span></td>" +
							'<td>' + object.programName + '</td>' +
							'<td>' + object.frequency + '</td>' +
							'<td>' + object.lastRun + '</td>' +
							'<td>' + object.status + '</td>' +
							'<td>' + object.targetDate + '</td>';
			if(object.reportId==1){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				clinicalSchedulesTable.appendChild(tr);
			}
			else if(object.reportId==2){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				pharmacySchedulesTable.appendChild(tr);
			}
			else if(object.reportId==3){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				LabSchedulesTable.appendChild(tr);
			}
			document.getElementById(object.id).checked= object.enabled;
		});
	});
}

//use checkboxes to disable/enable scheduled reports
function disenSchedule(toggled_id){
	var scheduleId=toggled_id;
	var enabled = document.getElementById(toggled_id).checked ? 'true' : 'false';
	console.log('Clicked toggle switch element is '+toggled_id);
	console.log('Clicked toggle switch element value is '+document.getElementById(toggled_id).value);
	console.log('Clicked toggle switch element value is '+enabled);

	console.log('Clicked schedule to enable/disable is '+toggled_id);

	var parameters = {
		scheduleId : scheduleId,
		enabled:enabled
	};
	
	var submitTo = disenScheduleUrl;
	return $.get(submitTo,parameters).done(function(data) {
		console.log('[Server result for disenSchedule()]');
		console.log(data);

		
	}).fail(function(response) {
		console.log('[Operation disenSchedule() failed]');
	});

}

//populate list of DHIS-enabled hmis programs into select element
function initDHISProgramNameDropdowns(){
	var isYearlyReport = false;
	var canSubmitReport = hasReportingPrivilege;
	getContent(isYearlyReport, canSubmitReport).then(
				function(content) {
					console.log('[populate option element]');
					console.log(content.programs);
					let clinical_dropdown = $('#clinical-report-name');
					let pharmacy_dropdown = $('#pharmacy-report-name');
					let lab_dropdown = $('#lab-report-name');
					clinical_dropdown.empty();
					pharmacy_dropdown.empty();
					lab_dropdown.empty();
					clinical_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					pharmacy_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					lab_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					clinical_dropdown.prop('selectedIndex', 0);
					pharmacy_dropdown.prop('selectedIndex', 0);
					lab_dropdown.prop('selectedIndex', 0);
					$.each(content.programs, function (key, entry) {
						//console.log('Report type is '+ entry.config.reports[0].type);
						if(entry.config.reports[0].type=="MRSGeneric"){
							clinical_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
						}
						else if(entry.config.reports[0].type=="ERPGeneric"){
							pharmacy_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
						}
						else if(entry.config.reports[0].type=="LISGeneric"){
							lab_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
						}
					});
				});
}

function initTabs() {
	$("#tabs").tabs();
}

//read a list of DHIS enabled programs 
function getDHISPrograms() {
	return $.getJSON(reportConfigUrl).then(function(reportConfigs) {
		var DHISPrograms = [];
		Object.keys(reportConfigs).forEach(function(reportKey) {
			if (reportConfigs[reportKey].DHISProgram) {
				reportConfigs[reportKey].index = DHISPrograms.length;
				DHISPrograms.push(reportConfigs[reportKey]);
			}
		});
		return DHISPrograms;
	});
}

//read list of existing DHIS scheduless
function getDHISSchedules() {
	return $.get(getSchedulesUrl).done(function(data) {
		console.log('[Get DHIS schedules]');
		//console.log(data);
		
	}).fail(function(response) {
		
	});
}

//delete checked schedules from the list
function removeAllRowsContainingCheckedCheckbox(table) {
    for (var rowi= table.rows.length; rowi-->0;) {
        var row= table.rows[rowi];
        var inputs= row.getElementsByTagName('input');
        for (var inputi= inputs.length; inputi-->0;) {
            var input= inputs[inputi];

            if (input.type==='checkbox' && input.checked && input.className =='selectSchedule') {
                row.parentNode.removeChild(row);
                break;
            }
        }
    }
}

//delete schedule from the database
function deleteDHISSchedule(clicked_id){

	var scheduleIds=[];
	$.each($(".selectSchedule:checked"), function(){            
		scheduleIds.push($(this).val());
		console.log('ID of clicked schedule to delete is '+$(this).val());
		var checkbox=this;
		var row_index=checkbox.parentElement.parentElement.rowIndex;
		console.log('Row index of schedule to delete is '+ row_index);
		if(clicked_id == 'deleteWeeklySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("weekly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("weekly-program-schedules"));
		}
		else if(clicked_id == 'deleteMonthlySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("monthly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("monthly-program-schedules"));
		}
		else if(clicked_id == 'deleteQuarterlySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("quarterly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("quarterly-program-schedules"));
		}

	});

	console.log('Clicked schedule to delete is '+scheduleIds);

	var parameters = {
		scheduleIds : scheduleIds
	};
	
	var submitTo = deleteScheduleUrl;
	return $.get(submitTo,parameters).done(function(data) {
		//data = JSON.stringify(data);
		console.log('[Server result for deleteDHISSchedule()]');
		console.log(data);

		window.location.reload();

		
	}).fail(function(response) {
		console.log('[Operation deletDHISSchedule() failed]');
	});

}

//create a new schedule and add it to the db
function createDHISSchedule(clicked_id, frequency){
	console.log('Creating new schedule, clicked_id='+clicked_id+' frequency='+frequency);
	var parameters=[];
	var submitTo;
	var pharmReportingPeriods=[];
	var reportName;
	var scheduleFrequency;//=frequency;
	var scheduleTime;
	var clinicalSchedulesTable = document.getElementById('clinical-program-schedules');
	var pharmacySchedulesTable = document.getElementById('pharmacy-program-schedules');
	var isCustomReportingPeriods=document.getElementById('is-custom-monthly-reporting-periods-checkbox');
	var LabSchedulesTable = document.getElementById('lab-program-schedules');
	var tr = document.createElement('tr');
	var tempHTML ="<td>"+"<span class='custom-checkbox'>"+
				  "<input type='checkbox' class='selectSchedule' id='checkbox1' name='options[]' value='1'/>"+
				  "<label for='checkbox1'></label>"+"</span></td>";

	if(clicked_id == 'addClinicalSchedulebtn'){
		alert('[Creating a clinical schedule.]');
		reportName=document.getElementById('clinical-report-name').value;
		scheduleTime=document.getElementById('clinical-time').value;
		scheduleFrequency=document.getElementById('clinical-frequency').value;
		reportTypeName="MRSGeneric";
	}
	else if(clicked_id == 'addPharmacySchedulebtn'){
		alert('[Creating a pharmacy schedule.]');
		reportName=document.getElementById('pharmacy-report-name').value;
		scheduleTime=document.getElementById('pharmacy-time').value;
		scheduleFrequency=document.getElementById('pharmacy-frequency').value;
		reportTypeName="ERPGeneric";
		if(isCustomReportingPeriods.checked){
		// if this pharmacy report has custom reporting periods, read them
			for (let i = 1; i <= 12; i++) {
				let startDatetimePicker = document.getElementById(`reporting_period${i}-start`);
				let endDatetimePicker = document.getElementById(`reporting_period${i}-end`);
				
				let startDatetime = startDatetimePicker.value;
				let endDatetime = endDatetimePicker.value;
				
				pharmReportingPeriods.push({start: startDatetime, end: endDatetime});
				
			}

		}
	}
	else if(clicked_id == 'addLabSchedulebtn'){
		alert('[Creating a lab schedule.]');
		reportName=document.getElementById('lab-report-name').value;
		scheduleTime=document.getElementById('lab-time').value;
		scheduleFrequency=document.getElementById('lab-frequency').value;
		reportTypeName="ELISGeneric";
	}

	if(reportTypeName="MRSGeneric"){
		alert('[Added to clinical schedule table.]');
		tr.innerHTML =tempHTML+
					  '<td>' + reportName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  '<td>' + '-' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		clinicalSchedulesTable.appendChild(tr);
	}
	else if(reportTypeName="ERPGeneric"){
		alert('[Added to pharmacy schedule table.]');
		tr.innerHTML =tempHTML+
					  '<td>' + reportName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  '<td>' + '-' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		pharmacySchedulesTable.appendChild(tr);
	}
	else if(reportTypeName="ELISGeneric"){
		alert('[Added to lab schedule table.]');
		tr.innerHTML =tempHTML+
					  '<td>' + reportName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  '<td>' + '-' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		LabSchedulesTable.appendChild(tr);
	}

	if(isCustomReportingPeriods.checked){
		submitTo=createPharmScheduleUrl;
		parameters = {
			reportName : reportName,
			reportTypeName: reportTypeName,
			scheduleFrequency : scheduleFrequency,
			scheduleTime : scheduleTime,
			PharmacyPeriodListRequest:pharmReportingPeriods
		};
	}else{
		submitTo=createScheduleUrl;
		parameters = {
			reportName : reportName,
			reportTypeName: reportTypeName,
			scheduleFrequency : scheduleFrequency,
			scheduleTime : scheduleTime
		};
	}
	return $.get(submitTo,parameters).done(function(data) {
		//data = JSON.stringify(data);
		console.log('[Server result for submitNewSchedule()]');
		console.log("URL:"+submitTo);
		console.log(data);
		if(data==true){

		}
		else{
			
		}
		window.location.reload();
		
	}).fail(function(response) {
		console.log('[Operation submitNewSchedule() failed]');
	});

}


function element(name, index) {
	var id = name + '-' + index;
	return $('[id="' + id + '"]');
}

function enableBtn(btn) {
	return btn.attr('disabled', false).removeClass('btn-disabled');
}

function disableBtn(btn) {
	return btn.attr('disabled', true).addClass('btn-disabled');
}

function disableAllSubmitBtns() {
	disableBtn($("[id*='submit-']"));
}

function registerOnchangeOnComment() {
	disableAllSubmitBtns();
	$("[id*='comment-']").on('change keyup paste', function(event) {
		var index = $(event.target).attr('index');
		if ($(event.target).val().trim() != "") {
			enableBtn(element('submit', index));
		} else {
			disableBtn(element('submit', index));
		}
	});
}

function getLogStatus() {
	$('#programs .month-selector').each(function(index) {
		getStatus(index);
	});
}

function getDHISPrograms() {
	return $.getJSON(reportConfigUrl).then(function(reportConfigs) {
		var DHISPrograms = [];
		Object.keys(reportConfigs).forEach(function(reportKey) {
			if (reportConfigs[reportKey].DHISProgram) {
				reportConfigs[reportKey].index = DHISPrograms.length;
				DHISPrograms.push(reportConfigs[reportKey]);
			}
		});
		return DHISPrograms;
	});
}

function getContent(isYearlyReport, canSubmitReport) {
	return getDHISPrograms().then(function(programs) {
		if (isYearlyReport) {
			return {
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		} else {
			return {
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		}
	});
}