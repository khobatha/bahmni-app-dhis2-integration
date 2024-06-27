var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
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

var weeks = [
	{
	  "number": 1,
	  "name": "Week 1"
	},
	{
	  "number": 2,
	  "name": "Week 2"
	},
	{
	  "number": 3,
	  "name": "Week 3"
	},
	{
	  "number": 4,
	  "name": "Week 4"
	},
	{
	  "number": 5,
	  "name": "Week 5"
	},
	{
	  "number": 6,
	  "name": "Week 6"
	},
	{
	  "number": 7,
	  "name": "Week 7"
	},
	{
	  "number": 8,
	  "name": "Week 8"
	},
	{
	  "number": 9,
	  "name": "Week 9"
	},
	{
	  "number": 10,
	  "name": "Week 10"
	},
	{
	  "number": 11,
	  "name": "Week 11"
	},
	{
	  "number": 12,
	  "name": "Week 12"
	},
	{
	  "number": 13,
	  "name": "Week 13"
	},
	{
	  "number": 14,
	  "name": "Week 14"
	},
	{
	  "number": 15,
	  "name": "Week 15"
	},
	{
	  "number": 16,
	  "name": "Week 16"
	},
	{
	  "number": 17,
	  "name": "Week 17"
	},
	{
	  "number": 18,
	  "name": "Week 18"
	},
	{
	  "number": 19,
	  "name": "Week 19"
	},
	{
	  "number": 20,
	  "name": "Week 20"
	},
	{
	  "number": 21,
	  "name": "Week 21"
	},
	{
	  "number": 22,
	  "name": "Week 22"
	},
	{
	  "number": 23,
	  "name": "Week 23"
	},
	{
	  "number": 24,
	  "name": "Week 24"
	},
	{
	  "number": 25,
	  "name": "Week 25"
	},
	{
	  "number": 26,
	  "name": "Week 26"
	},
	{
	  "number": 27,
	  "name": "Week 27"
	},
	{
	  "number": 28,
	  "name": "Week 28"
	},
	{
	  "number": 29,
	  "name": "Week 29"
	},
	{
	  "number": 30,
	  "name": "Week 30"
	},
	{
	  "number": 31,
	  "name": "Week 31"
	},
	{
	  "number": 32,
	  "name": "Week 32"
	},
	{
	  "number": 33,
	  "name": "Week 33"
	},
	{
	  "number": 34,
	  "name": "Week 34"
	},
	{
	  "number": 35,
	  "name": "Week 35"
	},
	{
	  "number": 36,
	  "name": "Week 36"
	},
	{
	  "number": 37,
	  "name": "Week 37"
	},
	{
	  "number": 38,
	  "name": "Week 38"
	},
	{
	  "number": 39,
	  "name": "Week 39"
	},
	{
	  "number": 40,
	  "name": "Week 40"
	},
	{
	  "number": 41,
	  "name": "Week 41"
	},
	{
	  "number": 42,
	  "name": "Week 42"
	},
	{
	  "number": 43,
	  "name": "Week 43"
	},
	{
	  "number": 44,
	  "name": "Week 44"
	},
	{
	  "number": 45,
	  "name": "Week 45"
	},
	{
	  "number": 46,
	  "name": "Week 46"
	},
	{
	  "number": 47,
	  "name": "Week 47"
	},
	{
	  "number": 48,
	  "name": "Week 48"
	},
	{
	  "number": 49,
	  "name": "Week 49"
	},
	{
	  "number": 50,
	  "name": "Week 50"
	},
	{
	  "number": 51,
	  "name": "Week 51"
	},
	{
	  "number": 52,
	  "name": "Week 52"
	}
  ];
  

var months = [ {
	number : 12,
	name : "December"
}, {
	number : 11,
	name : "November"
}, {
	number : 10,
	name : "October"
}, {
	number : 9,
	name : "September"
}, {
	number : 8,
	name : "August"
}, {
	number : 7,
	name : "July"
}, {
	number : 6,
	name : "June"
}, {
	number : 5,
	name : "May"
}, {
	number : 4,
	name : "April"
}, {
	number : 3,
	name : "March"
}, {
	number : 2,
	name : "February"
}, {
	number : 1,
	name : "January"
} ];

var years = range(supportedStartDate, supportedEndDate);
var fiscalYears = fiscalYearRange(supportedStartDate, supportedEndDate);
var hasReportingPrivilege = true;

$(document).ready(
		function() {

			// Activate tooltip
			$('[data-toggle="tooltip"]').tooltip();
			
			// Select/Deselect checkboxes
			var checkbox = $('table tbody input[type="checkbox"]');
			$("#selectAll").click(function(){
				if(this.checked){
					checkbox.each(function(){
						this.checked = true;                        
					});
				} else{
					checkbox.each(function(){
						this.checked = false;                        
					});
				} 
			});
			checkbox.click(function(){
				if(!this.checked){
					$("#selectAll").prop("checked", false);
				}
			});

			isAuthenticated().then(isSubmitAuthorized).then(initTabs).then(renderWeeklyReport).then(
				renderPrograms).then(renderYearlyReport).then(
				selectApproxLatestGregorianYear).then(
				registerOnchangeOnComment).then(getLogStatus);

		});

function isAuthenticated() {
	return $.get("is-logged-in").then(function(response) {
		if (response != 'Logged in') {
			window.location.href = loginRedirectUrl + window.location.href;
		}
	}).fail(function(response) {
		if (response && response.status != 200) {
			window.location.href = loginRedirectUrl;
		}
	});
}

function isSubmitAuthorized() {
	return $.get("hasReportingPrivilege").then(function(response) {
		hasReportingPrivilege = response;
		if (!hasReportingPrivilege) {
			$(".submit").remove();
		}
	});
}

function initTabs() {
	$("#tabs").tabs();
}

function range(start, end) {
	return Array.apply(null, new Array(start - end + 1)).map(
			function(ignore, index) {
				return start - index;
			});
}

function fiscalYearRange(start, end) {
	return Array.apply(null, new Array(start - end + 1)).map(
			function(ignore, index) {
				return (start - index - 1) + '-' + (start - index);
			});
}

function selectApproxLatestGregorianYear() {
	var date = new Date();
	$('[id^="year-"]').val(date.getFullYear());
	$('[id^="month-"]').val(date.getMonth());

	$('[id^="fiscal-year-"]').val( (date.getFullYear()-1) + '-' + date.getFullYear());
}

function selectApproxLatestNepaliYear() {
	var date = new Date();
	var bsDate = calendarFunctions.getBsDateByAdDate(date.getFullYear(), date
			.getMonth() + 1, date.getDate());
	if (bsDate.bsMonth == 1) {
		bsDate.bsYear = bsDate.bsYear - 1;
		bsDate.bsMonth = 12;
	} else {
		bsDate.bsMonth = bsDate.bsMonth - 1;
	}
	$('[id^="year-"]').val(bsDate.bsYear);
	$('[id^="month-"]').val(bsDate.bsMonth);

	$('[id^="fiscal-year-"]').val((bsDate.bsYear - 1) + '-' + bsDate.bsYear);
}

function renderPrograms() {
	return $.get('html/programs.html').then(
			function(template) {
				var isYearlyReport = false;
				var isWeeklyReport = false;
				var canSubmitReport = hasReportingPrivilege;
				return getContent(isWeeklyReport,isYearlyReport, canSubmitReport).then(
						function(content) {
							//console.log(content.programs);
							$("#programs").html(
									Mustache.render(template, content));
						});
			});
}

function listPrograms() {
	getContent(isWeeklyReport,isYearlyReport, canSubmitReport).then(
						function(content) {
							alert(content);
						});
}	


function renderYearlyReport() {
	return $.get('html/programs.html').then(function(template) {
		var isYearlyReport = true;
		var isWeeklyReport = false;
		return getContent(isWeeklyReport,isYearlyReport).then(function(content) {
			$("#programs-yearly").html(Mustache.render(template, content));
		});
	});
}

function renderWeeklyReport() {
	return $.get('html/programs.html').then(function(template) {
		var isWeeklyReport = true;
		var isYearlyReport = false;
		var canSubmitReport = hasReportingPrivilege;
		return getContent(isWeeklyReport,isYearlyReport,canSubmitReport).then(function(content) {
			$("#programs-weekly").html(Mustache.render(template, content));
		});
	});
}


function getContent(isWeeklyReport,isYearlyReport, canSubmitReport) {
	return getDHISPrograms().then(function(programs) {
		if(isWeeklyReport){
			return {
				weeks : weeks,
				years : years,
				programs : programs,
				isWeeklyReport: isWeeklyReport,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		}
		else if (isYearlyReport) {
			return {
				years : fiscalYears,
				programs : programs,
				isWeeklyReport: isWeeklyReport,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		} else {
			return {
				months : months,
				years : years,
				programs : programs,
				isWeeklyReport: isWeeklyReport,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		}
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

function putStatus(data, index) {
	element('comment', index).html(data.comment).html();
	//alert("[putStatus] Welcome to the putStatus function...displaying argument data.stringify()...");
	//alert(JSON.stringify(data));
	//alert("[putStatus] Welcome to the putStatus function...displaying argument data.status...");
	//alert(data.status);
	if (data.status == 'Success' || data.status == 'Complete') {
		//alert("[putStatus] Status is SUCCESS...updating...displaying the data");
		//alert(data.status);
		var template = $('#success-status-template').html();
		Mustache.parse(template);
		element('status', index).html(Mustache.render(template, data));
		return;
	}
	//alert("[putStatus] Status is FAILURE...updating...displaying the data");
	//alert(data.status.status);
	var template = $('#failure-status-template').html();
	Mustache.parse(template);
	data.message = JSON.stringify(data.exception || data.response);
	element('status', index).html(Mustache.render(template, data));
	element('status', index).find('.status-failure').on('click', function() {
		alert(data.message);
		console.log(data.message);
	});
}

function putStatusRefresh(data, index) {
	element('comment', index).html(data.comment).html();
	//alert("[putStatus] Welcome to the putStatus function...displaying argument data.stringify()...");
	//alert(JSON.stringify(data));
	//alert("[putStatus] Welcome to the putStatus function...displaying argument data.status...");
	//alert(data.status);
	var reportStatus = new Object();
	reportStatus.status = 'Failure';
	if (data.status == 'Success' || data.comment == 'Complete') {
		//alert("[putStatus] Status is SUCCESS...updating...displaying the data");
		//alert(data.status);
		reportStatus.status = 'Success';
		var template = $('#success-refresh-status-template').html();
		Mustache.parse(template);
		element('status', index).html(Mustache.render(template, reportStatus));
		return;
	}
	//alert("[putStatus] Status is FAILURE...updating...displaying the data");
	//alert(data.status);
	var template = $('#failure-refresh-status-template').html();
	Mustache.parse(template);
	data.message = JSON.stringify(data.exception || data.response);
	element('status', index).html(Mustache.render(template, reportStatus));

}

function download(index) {
	var year = element('year', index).val();
	var month = element('month', index).val();
	var programName = element('program-name', index).html();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var isFamily = programName.toLowerCase() === FAMILYPLANNING_PROGRAM
			.toLowerCase();
	var url = downloadUrl.replace('NAME', programName).replace('YEAR', year)
			.replace('MONTH', month).replace('IS_IMAM', isImam).replace('IS_FAMILY', isFamily);
	downloadCommon(url);
}

function downloadFiscalYearReport(index) {
	var yearRange = element('fiscal-year', index).val();
	var years = yearRange.split('-');
	var startYear = years[0];
	var startMonth = 4; //Shrawan
	var endYear = years[1];
	var endMonth = 3; //Ashadh
	var programName = element('program-name', index).html();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var url = fiscalYearReportUrl.replace('NAME', programName).replace(
			'START_YEAR', startYear).replace('START_MONTH', startMonth)
			.replace('END_YEAR', endYear).replace('END_MONTH', endMonth)
			.replace('IS_IMAM', isImam);
	downloadCommon(url);
}

function downloadCommon(url) {
	var a = document.createElement('a');
	a.href = url;
	a.target = '_blank';
	a.click();
	return false;
}

function submit(index, attribute) {
	spinner.show();
	var year = element('year', index).val();
	var month = element('month', index).val();
	var programName = element('program-name', index).html();
	var comment = element('comment', index).val();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var isFamily = programName.toLowerCase() === FAMILYPLANNING_PROGRAM.toLowerCase();

	var parameters = {
		year : year,
		month : month,
		name : programName,
		comment : comment,
		isImam : isImam,
		isFamily : isFamily
	};

	disableBtn(element('submit', index));
	var submitTo = submitUrl;
	//alert("[submit] Welcome to the submit function...");
	if (attribute == true) {
		//alert("attribute == true, submitTo = submitUrlAtr");
		submitTo = submitUrlAtr;
	}
	$.get(submitTo, parameters).done(function(data) {
		data = JSON.parse(data)
		//alert("[submit] Submitted...displaying the feedback...data.stringify()");
		//alert(JSON.stringify(data));
		//alert("[submit] Submitted...displaying the feedback...data.status");
		//alert(data.status);
		if (!$.isEmptyObject(data)) {
			
			putStatus(data, index);
		}
		//alert("[submit] Submitted...feedback is empty...");
	}).fail(function(response) {
		//alert("[submit] Failed to submit...");
		if (response.status == 403) {
			//alert("[submit] Forbidden...403...");
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		enableBtn(element('submit', index));
		spinner.hide();
	});
}

function autoSubmit(year,month,programName,comment) {
	//spinner.show();
	//var year = element('year', index).val();
	//var month = element('month', index).val();
	//var programName = element('program-name', index).html();
	//var comment = element('comment', index).val();
	var isImam = false;
	isFamily = false;

	var parameters = {
		year : year,
		month : month,
		name : programName,
		comment : comment,
		isImam : isImam,
		isFamily : isFamily
	};

	//disableBtn(element('submit', index));
	var submitTo = submitUrl;
	//alert("[submit] Welcome to the submit function...");
	if (attribute == true) {
		//alert("attribute == true, submitTo = submitUrlAtr");
		submitTo = submitUrlAtr;
	}
	$.get(submitTo, parameters).done(function(data) {
		data = JSON.parse(data)
		//alert("[submit] Submitted...displaying the feedback...data.stringify()");
		//alert(JSON.stringify(data));
		//alert("[submit] Submitted...displaying the feedback...data.status");
		//alert(data.status);
		if (!$.isEmptyObject(data)) {
			
			putStatus(data, index);
		}
		//alert("[submit] Submitted...feedback is empty...");
	}).fail(function(response) {
		//alert("[submit] Failed to submit...");
		if (response.status == 403) {
			//alert("[submit] Forbidden...403...");
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		enableBtn(element('submit', index));
		spinner.hide();
	});
}

function confirmAndSubmit(index, attribute) {
	if (confirm("This action cannot be reversed. Are you sure, you want to submit?")) {
		submit(index, attribute);
	}
}

function getStatus(index) {
	var programName = element('program-name', index).html();
	var year = element('year', index).val();
	var month = element('month', index).val();

	var parameters = {
		programName : programName,
		month : month,
		year : year
	};
	spinner.show();
	$.get(logUrl, parameters).done(function(data) {
		
		data = JSON.parse(data);
		//alert("[getStatus] Status data retrieved from logUrl...displaying data after json parsing...data.stringify()");
		//alert(JSON.stringify(data));
		//alert("[getStatus] Status data retrieved from logUrl...displaying data after json parsing...data.status");
		//alert(data.status);
		if ($.isEmptyObject(data)) {
			element('comment', index).html('');
			element('status', index).html('');
		} else {
			//alert("[getStatus] Status retrieved from log...updating,,displaying the data after json parsing...");
			//alert(data.status.status);
			putStatusRefresh(data, index);
		}
	}).fail(function(response) {
		console.log("failure response");
		if (response.status == 403) {
			//alert("[getStatus] Status retrieval failed...access forbidden...");
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		//alert("[getStatus] Status retrieval failed...not sure why...");
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		spinner.hide();
	})
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

