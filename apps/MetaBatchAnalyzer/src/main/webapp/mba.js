
function notUN(theValue)
{
	return ((undefined !== theValue) && (null !== theValue));
}

// gets the parameters from the URL given a parameter name
// using just JavaScript
function getParameterByName(theName)
{
	var url = window.location.href;
	theName = theName.replace(/[\[\]]/g, "\\$&");
	var regex = new RegExp("[?&]" + theName + "(=([^&#]*)|&|#|$)");
	var results = regex.exec(url);
	if (!results)
	{
		return null;
	}
	else if (!results[2])
	{
		return '';
	}
	return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getMBAPropertiesCommon(theSelf)
{
	// Do an async Ajax call to MBA properties to get defaults.
	// The inline conditionals below check if theJson.theProperty got back a non-null value.
	// If so, than the property observables are updated accordingly.
	$.ajax(
	{ 
		type: "GET",
		dataType:'json',
		async:false,
		url: "MBAproperties",
		cache: false,
		success: function(theJson)
		{
			//console.log("MBAproperties: success");
			theSelf.serverTitle((!notUN(theJson.serverTitle)) ? theSelf.serverTitle() : theJson.serverTitle);
			theSelf.propAllowLogin(
				(!notUN(theJson.allowLogin)) ? theSelf.propAllowLogin() : theJson.allowLogin
			);
		},
		error: function(jqXHR, textStatus, errorThrown)
		{
			console.log("MBAproperties: " + textStatus + " and " + errorThrown);
			alert("MBAproperties: " + textStatus + " and " + errorThrown);
		}
	});
}

function setupKnockoutValidation()
{
	// Activate knockout-validation
	// parseInputAttributes talls KnockoutJS to use HTML5 validation attributes within the HTML
	// other attributes give calsses for details and tell validator to markup bad entries
	// 
	// I changed that so that the ‘mbaerror’ class and error message would  still be applied 
	// even if the initial values for an RBN/EBN input (or any input for that matter) are invalid. 
	// Realistically, the default values will be setup so that the pre-loaded configuration is not 
	// in an error state; however, if it is and messagesOnModified is set to true, than the
	// ‘mbarror’ class isn’t applied. It’s even more confusing at the GUI level because
	// appview.isValid() will still register as false, so the ‘Mbatch Run’ button will be hidden, 
	// but there is no notification of what the problem is. 
	//
	// My understanding of the messagesOnModified parameter is that by setting it to false you are 
	// telling knockout to validate at all times rather than only on a modification. Explained a bit more in these:
	//
	// https://stackoverflow.com/questions/15547734/showing-validation-messages-just-after-binding-with-knockout-validation
	//
	// https://github.com/ericmbarnard/Knockout-Validation/wiki/Configuration
	return(
	{
		insertMessages: true,
		decorateElement: true,
		errorMessageClass: 'mbaerror',
		errorElementClass: 'mbaerror',
		errorClass: 'mbaerror',
		errorsAsTitle: true,
		parseInputAttributes: true,
		writeInputAttributes: true,
		messagesOnModified: false,
		decorateElementOnModified: true,
		decorateInputElement: true
	});
}


function getLoggedInUser(theSelf)
{
	$.ajax(
	{ 
		type: "GET",
		dataType:'json',
		async:false,
		url: "AuthUser",
		cache: false,
		success: function(theJson)
		{
			theSelf.currentUserName(!notUN(theJson.userName) ? "" : theJson.userName);
			theSelf.availableUsers(!notUN(theJson.availableUsers) ? theSelf.availableUsers() : theJson.availableUsers);
			theSelf.availableRoles(!notUN(theJson.availableRoles) ? theSelf.availableRoles() : theJson.availableRoles);
		},
		error: function(jqXHR, textStatus, errorThrown)
		{
			console.log("AuthUser " + textStatus + " and " + errorThrown);
			alert("AuthUser " + textStatus + " and " + errorThrown);
		}
	});
}

////////////////////////////////////////////////////////////////
//// GUI utility function to disable input during long running processes
////////////////////////////////////////////////////////////////

disableInput = function()
{
	// TODO: can this be done as part of status? Maybe have a "in submit" status (or observable flag) that only applies to GUI and never appears in server side and flag is reset in refreshJobStatus?
	$(":input, a").not(".homeButton").prop("disabled",true);
};

enableInput = function()
{
	// TODO: can this be done as part of status? Maybe have a "in submit" status (or observable flag) that only applies to GUI and never appears in server side and flag is reset in refreshJobStatus?
	$(":input, a").not(".homeButton").prop("disabled",false);
};

// function to reset the URL to go "home"
// we actively want bookmarkable URLs for jobs, which is why we have this
goHome = function()
{
	window.location.assign(window.location.href.replace(window.location.search,'').replace(/[^\/]*$/, "index.html"));
};

goToPageBasedOnState_internal = function(theCurrentPage, theDesiredPage, theGoto)
{
	if (theDesiredPage!==theCurrentPage)
	{
		goToPage(theGoto);
	}
};

goToPageBasedOnState = function(theState, theJobId)
{
	var currentPage = window.location.pathname.toString().replace(/^.*(\\|\/|\:)/, '');
	if (theState.startsWith("MBATCHRUN_"))
	{
		goToPageBasedOnState_internal(currentPage, "mbatchrun.html", "mbatchrun.html?job="+theJobId);
	}
	else if (theState.startsWith("MBATCHCONFIG_"))
	{
		goToPageBasedOnState_internal(currentPage, "mbatchconfig.html", "mbatchconfig.html?job="+theJobId);
	}
	else if (theState.startsWith("NEWJOB_"))
	{
		goToPageBasedOnState_internal(currentPage, "newjob.html", "newjob.html?job="+theJobId);
	}
};

goToEditPage = function(theJobId)
{
	goToPage("editjob.html?job="+theJobId);
};

goToPage = function(theNewPage)
{
	window.location.assign(window.location.href.replace(window.location.search,'').replace(/[^\/]*$/, theNewPage));
};

goAuthUpdate = function()
{
	// .replace(/[^\/]*$/, "")
	// path.substr(0, path.lastIndexOf('/') + 1);
	window.location.assign(window.location.href.replace(window.location.search,'').replace(/[^\/]*$/, "AuthUpdate"));
};

goAuthOut = function()
{
	window.location.assign(window.location.href.replace(window.location.search,'').replace(/[^\/]*$/, "AuthOut"));
};

// This function converts the job ID (which is epoch time) to a readable date and time.
getNowTimestamp = function()
{
	const currentTime = new Date().getTime();
	
	//pad is a function that will convert a one place-holder value interger to two. (Ex. 0 --> 00, 1 --> 01)
	pad = function(nonPadded)
	{
		return (nonPadded < 10) ? '0' + nonPadded.toString() : nonPadded.toString();
	};

	var d = new Date(parseInt(currentTime,10));
	return(d.getFullYear() + "_" + (d.getMonth()+1) + "_" + d.getDate() + "_" + pad(d.getHours()) + pad(d.getMinutes()));
};

// This function converts the job ID (which is epoch time) to a readable date and time.
millisToTimestamp = function(millis)
{
	//pad is a function that will convert a one place-holder value interger to two. (Ex. 0 --> 00, 1 --> 01)
	pad = function(nonPadded)
	{
		return (nonPadded < 10) ? '0' + nonPadded.toString() : nonPadded.toString();
	};

	var d = new Date(parseInt(millis,10));
	var militaryHours = d.getHours(); 
	{
		if(militaryHours > 12)
		{
			var nonMilitaryHours = militaryHours - 12;
			var halfOfDay = "PM";
		}
		else
		{
			nonMilitaryHours  = militaryHours;
			var halfOfDay = "AM";
		}
	}
	return((d.getMonth()+1) + "/" + d.getDate() + "/" + d.getFullYear() + ", " + nonMilitaryHours + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()) + " " + halfOfDay);
};

//////////////////////////////////////////////////////////////////////////////////////
/////////////This calls update if the URL has a job id in it. ////////////////////////
/////////////This allows the user to bookmark a job and come /////////////////////////
/////////////back to it and get the current status////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
function refreshJobStatus(theSelf, theJobId)
{
	$.ajax(
	{
		type: "GET",
		dataType:'json',
		async:false,
		url: "JOBstatus",
		cache: false,
		data:
		{
			jobId: theJobId
		},
		success: function(theJson)
		{
			theSelf.jobId(theJobId);
			theSelf.jobState(theJson.status);
			theSelf.jobMessage(theJson.message);
			theSelf.jobTail(theJson.tail);
			theSelf.jobTag(theJson.tag);
			theSelf.jobOwner(theJson.owner);
			theSelf.jobEmail(theJson.email);
			theSelf.jobAuthUsers(theJson.authUsers);
			theSelf.jobAuthRoles(theJson.authRoles);
			if (theSelf.jobState().endsWith("_WAIT"))
			{
				// update after twenty seconds if status
				// ends in _WAIT
				setTimeout(function()
				{
					//window.location.reload(true);
					refreshJobStatus(theSelf, theJobId);
				},20000);
			}
		},
		error: function(jqXHR, textStatus, errorThrown)
		{
			console.log("JOBstatus :" + textStatus + " and " + errorThrown);
			alert("JOBstatus :" + textStatus + " and " + errorThrown);
		}
	});
};

function updateJobStatus(theSelf, theJobId, theNewStatus)
{
	$.ajax(
	{
		type: "GET",
		dataType:'text',
		async:false,
		url: "JOBupdate",
		cache: false,
		data:
		{
			jobId: theJobId,
			status: theNewStatus
		},
		success: function()
		{
			refreshJobStatus(theSelf, theJobId);
			goToPageBasedOnState(theNewStatus, theJobId);
		},
		error: function(jqXHR, textStatus, errorThrown)
		{
			console.log("JOBupdate :" + textStatus + " and " + errorThrown);
			alert("JOBupdate :" + textStatus + " and " + errorThrown);
		}
	});
};

initializeTooltips = function()
{
	var tooltipsImg = document.querySelectorAll('*[id^="tooltipImage_"]');
	var tooltipsCont = document.querySelectorAll('*[id^="tooltipContent_"]');
	for(i=0; i<tooltipsImg.length; i++)
	{
		//link tooltipImg to tooltipCont by shared value of the respective data attributes
		tippy('#'+tooltipsImg[i].id, {html: '#'+tooltipsCont[i].id } );
	}
};

notUN = function(theValue)
{
	return ((undefined!==theValue)&&(null!==theValue));
};

