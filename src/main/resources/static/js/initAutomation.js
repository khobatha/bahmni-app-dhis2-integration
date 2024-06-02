var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
var getClinicalSchedulesUrl = '/dhis-integration/get-schedules';
var getPharmSchedulesUrl = '/dhis-integration/get-pharm-schedules';
var getPharmSchedulePeriodsUrl='/dhis-integration/get-pharm-schedule-periods';
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
			renderDHISSchedules(getClinicalSchedulesUrl);
			renderDHISSchedules(getPharmSchedulesUrl);
			initPharmSchedulePeriodDatePickers();
			addPharmScheduleFrequencyEventListener();
			addCustomPeriodCheckboxEventListener();
			
});

function initPharmSchedulePeriodDatePickers(){

	let periodMonths= [8,9,10,11,12,1,2,3,4,5,6,7,8];
	let currentDate = new Date();
	let currentYear=currentDate.getFullYear();
	let endYear=currentYear;
	let startYear=currentYear;
	let startDay=1;
	let setEndYear=false;
	for (let i = 0; i < 12; i++) {
		let startDatetimePicker = document.getElementById(`reporting_period${i+1}-start`);
		let endDatetimePicker = document.getElementById(`reporting_period${i+1}-end`);
		
		let startMonth=periodMonths[i] ;
		let endMonth=periodMonths[i+1];
		if(i ==4){
			endYear=endYear+1;
		}
		else if(i>4 && !setEndYear){
			startYear=startYear+1;
			setEndYear=true;
		}
		let startDate=`${startYear}-${String(startMonth).padStart(2, "0")}-${String(startDay).padStart(2, "0")}`;
		let minStartDate=startDate;
		let endDay=startMonth==2?29:31;
		let maxStartDate=`${startYear}-${String(startMonth).padStart(2, "0")}-${String(endDay).padStart(2, "0")}`;
		let endDate=`${endYear}-${String(endMonth).padStart(2, "0")}-${String(startDay).padStart(2, "0")}`;
		let minEndDate=endDate;
		endDay=endMonth==2?29:31;
		let maxEndDate=`${endYear}-${String(endMonth).padStart(2, "0")}-${String(endDay).padStart(2, "0")}`;
		startDatetimePicker.value= startDate;
		startDatetimePicker.minDate= minStartDate;
		startDatetimePicker.maxDate= maxStartDate;
		endDatetimePicker.value= endDate;
		endDatetimePicker.minDate=minEndDate;
		endDatetimePicker.maxDate=maxEndDate;
		console.log(endDate);
		console.log(startDate);
	}

}

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


function getSchedulePeriods(url) {
	return new Promise((resolve, reject) => {
	  const xhr = new XMLHttpRequest();
	  xhr.open('GET', url);
	  xhr.onload = function() {
		if (xhr.status === 200) {
		  resolve(xhr.responseText);
		} else {
		  reject(xhr.statusText);
		}
	  };
	  xhr.onerror = function() {
		reject('Network error');
	  };
	  xhr.send();
	});
  }
  
  async function addCollapsibleLinkOnMultiperiodSchedule(schedule_id) {
	try {
	  var url=`${getPharmSchedulePeriodsUrl}?pharmschedid=${schedule_id}`;
	  //console.log('The url is:', url);
	  const result = await getSchedulePeriods(url);
	  const periods = JSON.parse(result);
	  console.log('[isMultiPeriodSchedule] Periods received for schedule '+schedule_id+':', periods);
	  console.log('[isMultiPeriodSchedule] Length of received periods for schedule '+schedule_id+':', periods[0].length);
	  if(periods[0].length==2){
		console.log('[isMultiPeriodSchedule] Processing a single-period schedule '+schedule_id);
	  }
	  else{
		//add bootstrap classes to make this row collapsible
		console.log('[isMultiPeriodSchedule] Processing a multi-period schedule '+schedule_id);
		var rowId="schedule-"+schedule_id+"-row";
		var tr = document.getElementById(rowId);
		tr.classList.add("table-row");

		//add the plus icon link for collapsing this multiperiod schedule
		const span = document.createElement("span");
		span.classList.add("expand-icon");
		span.appendChild(document.createTextNode("\u00A0"));
		var cellId="schedule-"+schedule_id;
		const cell = document.getElementById(cellId);
		cell.insertBefore(span,cell.firstChild);
		

		//generate dynamic html to display the periods of this schedule
		generateMultiSchedulePeriodsHtml(schedule_id,periods);

		//add event listener to parent row to collapse when clicked
		tr.addEventListener('click', function() {
			const hiddenRow = tr.nextElementSibling;
			tr.classList.toggle('collapsed');
			hiddenRow.classList.toggle('hidden-row');
		  	console.log('Schedule '+schedule_id+' Row clicked!');
		});
	}
	} catch (error) {
	  console.error('Error:', error);
	}
  }

function periodIdToName(period_id){
	const periodMap = {
		0: "Aug/Sept",
		1: "Sept/Oct",
		2: "Oct/Nov",
		3: "Nov/Dec",
		4: "Dec/Jan",
		5: "Jan/Feb",
		6: "Feb/Mar",
		7: "Mar/Apr",
		8: "Apr/May",
		9: "May/Jun",
		10: "Jun/Jul",
		11: "Jul/Aug"
	  };
	
	  return periodMap[period_id] || "Unknown";
}  

function generateMultiSchedulePeriodsHtml(schedule_id,result){
	
	console.log('[generateMultiSchedulePeriodsHtml] Loaded data is '+result);
	var periods=JSON.parse(result);

	// create a new hidden row to hold periods of this schedule
	var tempHTML='<tr class="hidden-row"><td></td><td colspan="6"><div>';
	var closingHTML='</div></td></tr>';

	// construct html to render periods of this schedule
	periods.forEach(function(object){
		console.log('[generateMultiSchedulePeriodsHtml] Processing period '+object.id);
		var enabled=object.enabled?"checked":"unchecked";
		// checkbox | Name | Frequency | LastRun | Status | NextRun | Enabled
		var rowDiv ='<div class="row">'+ 
					'<div class="col-4">' + periodIdToName(object.period) + '</div>' + 
					'<div class="col-2" align="center">monthly</div>' +
					'<div class="col-1">' + object.lastRun + '</div>' +
					'<div class="col-1" align="center" style="padding-left:40px">' + object.status + '</div>' +
					'<div class="col-2" align="center">-</div>'+
					'<div class="col-2" align="center" style="padding-left:60px">'+
					'<label class="switch"><input type="checkbox" id="period-'+object.id+'" onclick="disenPeriodSchedule('+object.id+')"+ '+enabled+' ><span class="slider round"></span></label>'+
					'</div></div>';
		//add this period row div to the parent div
		tempHTML=tempHTML+rowDiv;
	});
	tempHTML=tempHTML+closingHTML;
	//insert the row of periods immediately after the corresponding parent row
	var rowId="schedule-"+schedule_id+"-row";
	var parentTr = document.getElementById(rowId);
	parentTr.insertAdjacentHTML('afterend', tempHTML);	
}

//populate and render list of schedules from db
async function renderDHISSchedules(url){

	var data = await getDHISSchedules(url);
	console.log('[renderDHISSchedules] Welcome');
	//console.log('[url ]'+url);
	console.log('[renderDHISSchedules] Loaded data is'+data);
	//alert(data);
	var clinicalSchedulesTable = document.getElementById('clinical-program-schedules');
	var pharmacySchedulesTable = document.getElementById('pharmacy-program-schedules');
	var LabSchedulesTable = document.getElementById('lab-program-schedules');

	//create table bodies for all tables
	const clinicalSchedulesTableBody = document.createElement('tbody');
	const pharmacySchedulesTableBody = document.createElement('tbody');
	const LabSchedulesTableBody = document.createElement('tbody');


	var schedules=JSON.parse(data);
	var tempHTML;
	schedules.forEach(function(object) {
		console.log('[renderDHISSchedules] Processing schedule '+object.id);
		var tr = document.createElement('tr');
		var cellId="schedule-"+object.id;
		var rowId="schedule-"+object.id+"-row";
		tr.setAttribute("id", rowId);
		var mainScheduleCheckBoxId="main-"+((object.reportId==1)?"clinical":(object.reportId==2)?"pharmacy":"lab")+"-schedule-checkbox-"+object.id;
		console.log('[renderDHISSchedules] Processing a single-period schedule '+object.id);
		tempHTML ="<td>"+"<span class='custom-checkbox'>"+
					"<input class='selectSchedule' type='checkbox' id='"+mainScheduleCheckBoxId+"' name='options[]' value='"+object.id+"'/>"+
					"<label for='checkbox1'></label>"+"</span></td>" +
					'<td id=' + cellId + ">" + object.programName + '</td>' +
					'<td>' + object.frequency + '</td>' +
					'<td>' + object.lastRun + '</td>' +
					'<td>' + object.status + '</td>' +
					'<td>' + object.targetDate + '</td>';
		if(object.reportId==1){
			tr.innerHTML =tempHTML+
						"<td>"+
						"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
						"</td>";
			clinicalSchedulesTableBody.appendChild(tr);
			clinicalSchedulesTable.appendChild(clinicalSchedulesTableBody);
		}
		else if(object.reportId==2){
			tr.innerHTML =tempHTML+
						"<td>"+
						"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
						"</td>";
			pharmacySchedulesTableBody.appendChild(tr);
			pharmacySchedulesTable.appendChild(pharmacySchedulesTableBody);
		}
		else if(object.reportId==3){
			tr.innerHTML =tempHTML+
						"<td>"+
						"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
						"</td>";
			LabSchedulesTableBody.appendChild(tr);
			LabSchedulesTable.appendChild(LabSchedulesTableBody);
		}
		document.getElementById(object.id).checked= object.enabled;
		addCollapsibleLinkOnMultiperiodSchedule(object.id);
	});

	//});
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
						if(entry.hasOwnProperty("config")){
							if(entry.config.hasOwnProperty("reports")){
								if(entry.config.reports[0].type=="MRSGeneric"){
									clinical_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
								}
								else if(entry.config.reports[0].type=="ERPGeneric"){
									pharmacy_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
								}
								else if(entry.config.reports[0].type=="LISGeneric"){
									lab_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
								}
							}
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
function getDHISSchedules(url) {
	return fetch(url)
		.then(response => response.json())
		.then(jsonArray => {
		return jsonArray;
		})
		.catch(error => {
		console.error('Error:', error);
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

function handleDeleteScheduleEvent(clicked_element_id){

	
	//var clicked_element_id='deletePharmacyScheduleLink';
	//console.log(clicked_element_id);
	if(clicked_element_id!=null){
		var modal;
		var programCategory;
		var msg;
		var paragraph;

		//select the right modal to display and the paragraph element to update
		if(clicked_element_id=='deletePharmacyScheduleLink'){
			modal = document.getElementById('deletePharmacyScheduleModal');
			programCategory="pharmacy";
			paragraph = document.querySelector('#deletePharmacyScheduleModal .delete-schedule-msg');

		}
		else if(clicked_element_id=='deleteClinicalScheduleLink'){
			modal = document.getElementById('deleteClinicalScheduleModal');
			programCategory="clinical";
			paragraph = document.querySelector('#deleteClinicalScheduleModal .delete-schedule-msg');
		}
		else if(clicked_element_id=='deleteLabScheduleLink'){
			modal = document.getElementById('deleteLabScheduleModal');
			programCategory="lab";
			paragraph = document.querySelector('#deleteLabScheduleModal .delete-schedule-msg');
		}

		//select the right message to display
		var flag = isProgramScheduleSelected(programCategory);
		console.log('[handleDeleteScheduleEvent] flag='+flag);
		if(flag){
			console.log('[handleDeleteScheduleEvent] atleast one schedule selected!');
			msg="Are you sure you want to delete these schedules?";
			paragraph.textContent=msg;
		}
		else{
			console.log('[handleDeleteScheduleEvent] No schedule selected!');
			msg="No schedule selected for deletion. Select schedule(s) to delete using the checkboxes on the left.";
			paragraph.textContent=msg;
		}

		/*$("#deletePharmacyScheduleModal").find('.btn-default, .close').off('click').on('click', function() {
			//$("#deletePharmacyScheduleModal").modal('hide');
			modal.classList.add('hide');
			//modal.style.display = 'block';
			console.log('[handleDeleteScheduleEvent] onclick event handlder added for cancel and close buttons!');
		});*/
		//enable cancel button
		//$("#deletePharmacyScheduleModal").find('.btn-default').prop('disabled', false);
		//enable close button
		//$("#deletePharmacyScheduleModal").find('.close').prop('disabled', false);
		//display the right modal with the right message
		//modal.classList.add('show');
		//modal.style.display = 'block';
		//modal.modal('show');
		//var selectModalbyId="#"+modal;
		//$(modal).modal('show');
		// Re-bind event handlers for cancel and close buttons
		
	
	}
}

function isProgramScheduleSelected(program_category){
	var found=false;
	$.each($(".selectSchedule:checked"), function(index,checkbox){   
		console.log('[isProgramScheduleSelected] checkedbox id is '+checkbox.id);   
		console.log('[isProgramScheduleSelected] checkbox id includes '+program_category+'? '+checkbox.id.includes(program_category));       
		if(checkbox.id.includes(program_category)==true)
			found= true;
	});
	return found;

}

//delete schedule from the database
function deleteDHISSchedule(clicked_id){

	var scheduleIds=[];
	$.each($(".selectSchedule:checked"), function(){            
		scheduleIds.push($(this).val());
		//alert('ID of clicked schedule to delete is '+$(this).val());
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
	var modalName;
	var pharmReportingPeriods=[];
	var reportName;
	var reportTypeName;
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
		//alert('[Creating a clinical schedule.]');
		reportName=document.getElementById('clinical-report-name').value;
		scheduleTime=document.getElementById('clinical-time').value;
		scheduleFrequency=document.getElementById('clinical-frequency').value;
		reportTypeName="MRSGeneric";
		modalName ='addClinicalScheduleModal';
	}
	else if(clicked_id == 'addPharmacySchedulebtn'){
		//alert('[Creating a pharmacy schedule.]');
		reportName=document.getElementById('pharmacy-report-name').value;
		scheduleTime=document.getElementById('pharmacy-time').value;
		scheduleFrequency=document.getElementById('pharmacy-frequency').value;
		reportTypeName="ERPGeneric";
		modalName ='addPharmacyScheduleModal';
		if(isCustomReportingPeriods.checked){
		// if this pharmacy report has custom reporting periods, read them
			let currentDate=new Date();
			let currentYear=currentDate.getFullYear();
			let currentMonth=currentDate.getMonth();
			let currentDay=currentDate.getDay();
			let maxDateStr=`${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(currentDay).padStart(2, "0")}`;
			let maxDate=new Date(maxDateStr);
			console.log("Current date is "+maxDateStr);
			let sorted=true;//checks if the pharmacy periods are sorted in ascending order
			for (let i = 1; i <= 12 && sorted; i++) {
				let startDatetimePicker = document.getElementById(`reporting_period${i}-start`);
				let endDatetimePicker = document.getElementById(`reporting_period${i}-end`);
				
				let startDatetime = startDatetimePicker.value;
				const startDate=new Date(startDatetime);
				let endDatetime = endDatetimePicker.value;
				const endDate=new Date(endDatetime);

				if(startDate>=maxDate){
					maxDate=startDate;
					if(maxDate<=endDate){
						maxDate=endDate;
						pharmReportingPeriods.push({start: startDatetime, end: endDatetime});
					}
					else{
						sorted=false;
						showFeedbackMessage('Error - periods are not sorted!', 'failure', modalName);
						return sorted;
					}
				}
				else {
					sorted=false;
					showFeedbackMessage('Error - periods are not sorted!', 'failure', modalName);
					return sorted;
				}
				
				
			}

		}
	}
	else if(clicked_id == 'addLabSchedulebtn'){
		//alert('[Creating a lab schedule.]');
		reportName=document.getElementById('lab-report-name').value;
		scheduleTime=document.getElementById('lab-time').value;
		scheduleFrequency=document.getElementById('lab-frequency').value;
		reportTypeName="ELISGeneric";
		modalName ='addLabScheduleModal';
	}

	//alert('[ReportTypeName is ]'+reportTypeName);
	if(reportTypeName=="MRSGeneric"){
		//alert('[Added to clinical schedule table.]');
		tr.innerHTML =tempHTML+
					  '<td>' + reportName + '</td>' +
					  '<td>' + frequency + '</td>' +
					  '<td>' + '-' + '</td>'+
					  '<td>' + 'Ready' + '</td>'+
					  '<td>' + '-' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		clinicalSchedulesTable.appendChild(tr);
	}
	else if(reportTypeName=="ERPGeneric"){
		//alert('[Added to pharmacy schedule table.]');
		tr.innerHTML =tempHTML+
						'<td>' + reportName + '</td>' +
						'<td>' + frequency + '</td>' +
						'<td>' + '-' + '</td>'+
						'<td>' + 'Ready' + '</td>'+
						'<td>' + '-' + '</td>'+
						"<td>"+
						"<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
						"</td>";
		pharmacySchedulesTable.appendChild(tr);
	}
	else if(reportTypeName=="ELISGeneric"){
		//alert('[Added to lab schedule table.]');
		tr.innerHTML =tempHTML+
						'<td>' + reportName + '</td>' +
						'<td>' + frequency + '</td>' +
						'<td>' + '-' + '</td>'+
						'<td>' + 'Ready' + '</td>'+
						'<td>' + '-' + '</td>'+
						"<td>"+
						"<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
						"</td>";
		LabSchedulesTable.appendChild(tr);
	}

	// Define the request parameters
	const params = new URLSearchParams();
	params.append('reportName',reportName);
	params.append('reportTypeName',reportTypeName);
	params.append('scheduleFrequency',scheduleFrequency);
	params.append('scheduleTime',scheduleTime);

	
	const xhr = new XMLHttpRequest();
	const body = JSON.stringify(pharmReportingPeriods);
	if(reportTypeName=="ERPGeneric" && isCustomReportingPeriods.checked){
		xhr.open('POST', `/dhis-integration/create-pharm-schedule?${params.toString()}`);
	}
	else
		xhr.open('POST', `/dhis-integration/create-schedule?${params.toString()}`);
	
	xhr.setRequestHeader('Content-Type', 'application/json');

	xhr.onload = function() {
	if (xhr.status === 200) {
		console.log(xhr.responseText);
		if(xhr.responseText=="true"){
			console.log('New schedule created successfully!');
			showFeedbackMessage('New schedule created successfully!', 'success', modalName);
		}
		else if(xhr.responseText=="false"){
			console.log('Error creating new schedule!');
			showFeedbackMessage('Error creating new schedule!', 'failure', modalName);
		}
	}
	};

	xhr.onerror = function() {
		console.error('Error:', xhr.statusText);
		showFeedbackMessage('Error creating new schedule!', 'failure', modalName);
	};

	if(reportTypeName=="ERPGeneric" && isCustomReportingPeriods.checked){
		// Send the request
		console.log("[Posting new multi-period pharm schedule. Periods are ]"+ JSON.stringify(pharmReportingPeriods));
		
		xhr.send(body);
	}
	else	
		xhr.send();
}

function showFeedbackMessage(message, type, modalID) {
	var modal = document.getElementById(modalID);
	// Create a new feedback message element
	const feedbackElement = document.createElement('div');
	feedbackElement.className = `feedback-${type}`;
	feedbackElement.textContent = message;
	
	// Get a reference to the form element within the modal
	const form = modal.querySelector('form');

	// Insert the feedback message into the modal form
	form.appendChild(feedbackElement);
  
	// Close the modal after a certain duration (e.g., 3 seconds)
	setTimeout(function() {
	  closeModal(modalID);
	  feedbackElement.remove();
	}, 3000);
  }

function closeModal(modalID){
	var modal = document.getElementById(modalID);
	modal.style.display = 'none';
	location.reload();
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