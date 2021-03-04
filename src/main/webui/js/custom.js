/*
 *  Copyright 2020 Alexander Liebald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

let zones = [1,1,1,1];
let brightness = 80;

const zoneAll	= document.getElementById("zoneAll");
const zone1 	= document.getElementById("zone1");
const zone2 	= document.getElementById("zone2");
const zone3 	= document.getElementById("zone3");
const zone4 	= document.getElementById("zone4");

zone1.onclick = function () {
	zoneAll.classList.remove("active");
	if(zone1.classList.toggle("active")) {
		zones[0] = 1;
	} else {
		zones[0] = 0;
	}
	console.log(zones.toString());
}

zone2.onclick = function () {
	zoneAll.classList.remove("active");
	if(zone2.classList.toggle("active")) {
		zones[1] = 1;
	} else {
		zones[1] = 0;
	}
	console.log(zones.toString());
}

zone3.onclick = function () {
	zoneAll.classList.remove("active");
	if(zone3.classList.toggle("active")) {
		zones[2] = 1;
	} else {
		zones[2] = 0;
	}
	console.log(zones.toString());
}

zone4.onclick = function () {
	zoneAll.classList.remove("active");
	if(zone4.classList.toggle("active")){
		zones[3] = 1;
	} else {
		zones[3] = 0;
	}
	console.log(zones.toString());
}

zoneAll.onclick = function () {
	if (zoneAll.classList.toggle("active")) {
		zone1.classList.add("active");
		zone2.classList.add("active");
		zone3.classList.add("active");
		zone4.classList.add("active");
		zones[0] = 1;
		zones[1] = 1;
		zones[2] = 1;
		zones[3] = 1;
	} else {
		zone1.classList.remove("active");
		zone2.classList.remove("active");
		zone3.classList.remove("active");
		zone4.classList.remove("active");
		zones[0] = 0;
		zones[1] = 0;
		zones[2] = 0;
		zones[3] = 0;
	}
	console.log(zones.toString());
}

function sendCommand(message) {
	send("command", "POST", "text/plain;", `command=${message}&zones=${zones.toString()}`, () => {});
}

function send(url, method, contentType, message, replyFunction) {
	console.log("sending", message);
	let xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
		if (this.readyState === 4 && this.status === 200) {
			document.getElementById("response").innerHTML = this.responseText;
			console.log(this.responseText);
			replyFunction(this.responseText);

			// Show error toast if response starts with ERROR
			if(this.responseText.startsWith("ERROR")) {
				document.getElementById("generalCommunicationErrorMessage").innerHTML = this.responseText;
				$('#generalCommunicationError').toast('show');
			}
		}
	};

	xmlhttp.open(method, url, true);
	xmlhttp.setRequestHeader("Content-Type", contentType);
	xmlhttp.send(message);
}

// Slider functions
const beatCooldownSlider = document.getElementById("beatCooldownSlider");
const beatCooldownNum = document.getElementById("beatCooldownNum");
const beatSensitivitySlider = document.getElementById("beatSensitivitySlider");
const beatSensitivityNum = document.getElementById("beatSensitivityNum");
const brightnessSlider = document.getElementById("brightnessSlider");

// Update the current slider value (each time you drag the slider handle)
beatCooldownSlider.addEventListener("input", function() {
	beatCooldownNum.value = this.value;
});

beatCooldownSlider.addEventListener("change", function() {
	console.log("# onended called on beatCooldownSlider: ", this.value);
	settings.beatCooldown = this.value;
	applySettings();
});

beatCooldownNum.addEventListener("input", function() {
	if (this.value > 1000) {
		beatCooldownSlider.value = 1000;
		this.value = 1000;
	} else if (this.value < 0) {
		beatCooldownSlider.value = 0;
		this.value = 0;
	} else if (this.value === "") {
		beatCooldownSlider.value = 0;
	} else {
		beatCooldownSlider.value = this.value;
	}
});

beatCooldownNum.addEventListener("change", function() {
	console.log("# change called on beatCooldownNum: ", this.value);
	settings.beatCooldown = this.value;
	applySettings();
});

// Sensitivity slider
beatSensitivitySlider.addEventListener("input", function() {
	beatSensitivityNum.value = this.value;
});

beatSensitivitySlider.addEventListener("change", function() {
	console.log("# change called on beatSensitivitySlider: ", this.value);
	settings.sensitivity = this.value;
	applySettings();
});

beatSensitivityNum.addEventListener("input", function() {
	if (this.value > 1.5) {
		beatSensitivitySlider.value = 1.5;
		this.value = 1.5;
	} else if (this.value < 0.50) {
		beatSensitivitySlider.value = 0.5;
	} else if (this.value === "") {
		beatSensitivitySlider.value = 0.5;
	} else {
		beatSensitivitySlider.value = this.value;
	}
});

beatSensitivityNum.addEventListener("change", function() {
	if (this.value < 0.5) {
		this.value = 0.5;
	}
	console.log("# change called on beatSensitivityNum: ", this.value);
	settings.sensitivity = this.value;
	applySettings();
});

brightnessSlider.onmouseup = function () {
	brightness = this.value; // TODO Required?
	console.log("Setting brightness to: ", brightness);
	sendCommand("setBrightness:" + brightness);
}

// Color picker logic
const colorPicker = new iro.ColorPicker("#picker", {
	color: "#ff0000",
	width: 300,
	borderWidth: 3,
	borderColor: "#bfbfbf",
	layout: [
		{
			component: iro.ui.Wheel,
			options: {}
		},
	]
});

// Send a color change command when the user set a new color
let lastUpdate = (new Date()).getTime();
colorPicker.on('input:change', function (color) {
	const addCustomColorButton = document.getElementById("addCustomColorButton");
	const curTime = (new Date()).getTime();
	if (curTime > lastUpdate + 150) {
		lastUpdate = curTime;

		// Change color of addCustomColorButton
		addCustomColorButton.style.background = color.hexString;
		setColorTo(color.hsl.h, color.hsl.s, color.hsl.s);
	}
});

// Takes the h, s and l values of a hsl color object and sends the converted 8 bit, MiLight compatible color, to the server
function setColorTo(h, s, l) {
		// Adjust the color a bit, since MiLight seems to have its colorscheme a bit off
		let adjustedColor = h + 40;
		if (adjustedColor > 360) {
			adjustedColor -= 360;
		}

		// scale down to 8 bit and send
		sendCommand("setColorTo:" + Math.round((adjustedColor) * (256 / 360)));

		//TODO include saturation (when supported by the Bridge);
}

// Settings
let settings;
send("settings.json", "GET", "application/json;", "settings.json", settingsReady);

// Applies settings to ui
function settingsReady (set) {
	settings = JSON.parse(set);

	document.getElementById("debugMode").checked 			= settings.clientSettings.debugMode;
	document.getElementById('openBrowserOnStart').checked	= settings.openBrowserOnStart;
	document.getElementById('bridgePort').valueAsNumber	= settings.bridgePort;

	beatCooldownSlider.value		= settings.beatCooldown;
	beatCooldownNum.value			= settings.beatCooldown;
	beatSensitivitySlider.value		= settings.sensitivity;
	beatSensitivityNum.value		= settings.sensitivity;

	const bridgeSelector 			= document.getElementById('bridgeIpAddress');
	const targetDataLineSelector	= document.getElementById('activeTargetDataLine');

	// Update targetDataLine selector
	if (settings.activeTargetDataLine === "none") {
		targetDataLineSelector.innerHTML = "<option value=\"none\" selected>none</option>\n";
	} else {
		targetDataLineSelector.innerHTML = "<option value=\"none\">none</option>\n";
	}

	settings.possibleTargetDataLines.forEach(function (item) {
		if (item === settings.activeTargetDataLine) {
			targetDataLineSelector.innerHTML += `<option value=\"${item}\" selected>${item}</option>\n`;
		} else {
			targetDataLineSelector.innerHTML += `<option value=\"${item}\">${item}</option>\n`;
		}
	});

	// Update bridge ip address selector
	if (settings.possibleBridgeIpAddresses.length > 0) {
		bridgeSelector.innerHTML = "";
	} else {
		bridgeSelector.innerHTML = "<option value=\"\">No bridge found in your local network</option>\n";
	}

	settings.possibleBridgeIpAddresses.forEach(function (item) {
		if (item === settings.bridgeIpAddress) {
			bridgeSelector.innerHTML += `<option value=\"${item}\" selected>${item}</option>\n`;
		} else {
			bridgeSelector.innerHTML += `<option value=\"${item}\">${item}</option>\n`;
		}
	});

	bridgeSelector.value			= settings.bridgeIpAddress;
	targetDataLineSelector.value	= settings.activeTargetDataLine;

	showCustomColors();

	// If no bridge is selected, show settings
	if (settings.bridgeIpAddress === "") {
		$('#settingsModal').modal('show');
	}

	// check if debug mode is enabled
	switchDebugMode();
}

function applySettings() {
	// update settings json
	settings.activeTargetDataLine		= document.getElementById('activeTargetDataLine').value;
	settings.openBrowserOnStart			= document.getElementById('openBrowserOnStart').checked;
	settings.clientSettings.debugMode	= document.getElementById("debugMode").checked;
	settings.bridgePort					= document.getElementById('bridgePort').valueAsNumber;

	if (document.getElementById("forceBridgeIpAddress").value !== "") {
		settings.bridgeIpAddress = document.getElementById('forceBridgeIpAddress').value;
	} else {
		settings.bridgeIpAddress = document.getElementById('bridgeIpAddress').value;
	}

	// check if no bridge is selected
	if (settings.bridgeIpAddress === "") {
		document.getElementById('saveSettingsErrorMessage').innerHTML = "Please select a bridge before applying changes.";
		$('#saveSettingsError').toast('show');
		return;
	}

	// send settings json
	console.log(settings);

	// TODO better user feedback, more options
	let onReply = function(response) {
		if (response.startsWith("ERROR")) {
			// Cut off error message and update settings
			const endError = response.indexOf("ERROR-END") + 9;
			console.log(response);
			deleteCustomColors();
			settingsReady(response.slice(endError));

			document.getElementById('saveSettingsErrorMessage').innerHTML = response.slice(0, endError - 9);
			$('#saveSettingsError').toast('show');
		} else {
			$('#settingsModal').modal('hide');
			$('#saveSettingsSuccess').toast('show');
			$('#saveSettingsError').toast('hide');
		}
	};

	send("applySettings", "POST", "application/json;", JSON.stringify(settings), onReply);
}

// Adds the current color of the colorPicker as a customColor to settings.clientSettings.customColors and sends them to the server
function addCustomColor() {
	// check if the exact color already exists
	if(document.getElementById(`${colorPicker.color.hsl.h}${colorPicker.color.hsl.s}${colorPicker.color.hsl.l}`)) {
		$('#customColorError').toast('show');
	} else {
		const customColor = {
			"hsl": colorPicker.color.hsl,
			"hex": colorPicker.color.hexString
		};
		$('#customColorError').toast('hide');
		settings.clientSettings.customColors.push(customColor);
		applySettings();
		addCustomColorBtn(customColor);
	}
}

// Adds all custom colors saved in the settings file to the site
function showCustomColors() {
	const colorButtons = document.getElementById('colorButtons');
	const insertIndex = colorButtons.innerHTML.indexOf("<!-- Add custom color button -->");
	let customColorBtn = "";

	// Add a button for every custom color
	settings.clientSettings.customColors.forEach(function (item, index) {
		console.log(item);
		customColorBtn += `<div class="col-xl-4 col-lg-4 col-md-4 col-sm-6 py-2 colorBtn">`
			+ `<button type="button" onclick="setColorTo(${item.hsl.h},${item.hsl.s},${item.hsl.l})" class="btn color" style="background: ${item.hex}" id="${item.hsl.h}${item.hsl.s}${item.hsl.l}"></button>`
			+ `<button type="button" onclick="removeCustomColor('${item.hsl.h}${item.hsl.s}${item.hsl.l}')" class="btn colorRemove noDisplay">`
			+ `<i class="far fa-trash-alt fa-lg"></i></button></div>`;
	});

	colorButtons.innerHTML = [colorButtons.innerHTML.slice(0, insertIndex), customColorBtn, colorButtons.innerHTML.slice(insertIndex)].join('');
}

// removes all custom colors
function deleteCustomColors() {
	const startIndex = colorButtons.innerHTML.indexOf("<!-- custom colors -->") + 22;
	const endIndex = colorButtons.innerHTML.indexOf("<!-- Add custom color button -->");

	colorButtons.innerHTML = [colorButtons.innerHTML.slice(0, startIndex), colorButtons.innerHTML.slice(endIndex)].join('');
}

// Adds a single custom color button to the site
function addCustomColorBtn(customColor) {
	const colorButtons = document.getElementById('colorButtons');
	const insertIndex = colorButtons.innerHTML.indexOf("<!-- Add custom color button -->");
	let customColorBtn =
		`<div class="col-xl-4 col-lg-4 col-md-4 col-sm-6 py-2 colorBtn">`
		+ `<button type="button" onclick="setColorTo(${customColor.hsl.h},${customColor.hsl.s},${customColor.hsl.l})" class="btn color" style="background: ${customColor.hex}" id="${customColor.hsl.h}${customColor.hsl.s}${customColor.hsl.l}"></button>`
		+ `<button type="button" onclick="removeCustomColor('${customColor.hsl.h}${customColor.hsl.s}${customColor.hsl.l}')" class="btn colorRemove noDisplay">`
		+ `<i class="far fa-trash-alt fa-lg"></i></button></div>`;

	colorButtons.innerHTML = [colorButtons.innerHTML.slice(0, insertIndex), customColorBtn, colorButtons.innerHTML.slice(insertIndex)].join('');
}

function removeCustomColor(customColorHSLId) {
	console.log("removing: " + customColorHSLId);
	const old = [...settings.clientSettings.customColors];
	let index;
	for (let i = 0; i < old.length; i++) {
		console.log(`${old[i].hsl.h}${old[i].hsl.s}${old[i].hsl.l}`);
		if (`${old[i].hsl.h}${old[i].hsl.s}${old[i].hsl.l}` === customColorHSLId) {
			index = i;
			console.log("found it at", i);
			break;
		}
	}
	settings.clientSettings.customColors = [];
	for (let i = 0; i < old.length; i++) {
		if (i !== index) {
			settings.clientSettings.customColors.push(old[i]);
		}
	}

	deleteCustomColors();
	applySettings();
	showCustomColors();
}

// Reset all settings to default
function resetSettings() {
	if (window.confirm("Do you really want to reset all settings to default?")) {
		const onReply = function(response) {
			deleteCustomColors();
			settingsReady(response);
		};

		// Tell the server to reset the settings and get the default settings
		send("resetSettings", "GET", "text/plain;", "", onReply);
	}
}

// Toggle edit mode for custom colors
function toggleColorEdit() {
	let buttons = $('.color');
	for (let i = 0; i < buttons.length; i++) {
		buttons[i].classList.toggle("colorEdit")
	}

	let delButtons = $('.colorRemove');
	for (let i = 0; i < delButtons.length; i++) {
		delButtons[i].classList.toggle("noDisplay")
	}
}

// debug checkbox
document.getElementById("debugMode").addEventListener('change', function (event) {
	settings.clientSettings.debugMode = event.target.checked;
	switchDebugMode();
})

// sets visibility of debug elements
function switchDebugMode () {
	if (settings.clientSettings.debugMode) {
		document.getElementById("response").style.visibility					= "visible";
		document.getElementById("forceBridgeIpAddressElem").style.visibility	= "visible";
	} else {
		document.getElementById("response").style.visibility					= "hidden";
		document.getElementById("forceBridgeIpAddressElem").style.visibility	= "hidden";
	}
}

//  Mode selector tabs: build in modes
document.getElementById("colorWheelTab").onclick = function () {
	sendCommand("setMode:ColorWheel");
}

document.getElementById("breathingColorWheelTab").onclick = function () {
	sendCommand("setMode:BreathingColorWheel");
}

document.getElementById("partyTab").onclick = function () {
	sendCommand("setMode:Party");
}

document.getElementById("partyMultipleColorsTab").onclick = function () {
	sendCommand("setMode:PartyMultipleColors");
}

document.getElementById("flashRedTab").onclick = function () {
	sendCommand("setMode:FlashRed");
}

document.getElementById("flashGreenTab").onclick = function () {
	sendCommand("setMode:FlashGreen");
}

document.getElementById("flashBlueTab").onclick = function () {
	sendCommand("setMode:FlashBlue");
}

// Mode selector tabs: music modes
function setMusicMode(musicMode, elemID) {
	console.log("setMusicMode("+musicMode+", "+elemID+");")
	if(settings.activeTargetDataLine !== "none") {
		sendCommand("setMode:M" + musicMode);
	} else {
		$('#missingAudioInputError').toast('show');
	}
}

document.getElementById("cyclicLightsTab").onclick = function () {
	setMusicMode("Cyclic", "cyclicLightsTab");
}

document.getElementById("cyclicLightsMCTab").onclick = function () {
	setMusicMode("CyclicMultipleColors", "cyclicLightsMCTab");
}

document.getElementById("flashingLightsTab").onclick = function () {
	setMusicMode("Flashing", "flashingLightsTab");
}

document.getElementById("pulseLightTab").onclick = function () {
	setMusicMode("Pulse", "pulseLightTab");
}

document.getElementById("sequentialLightsTab").onclick = function () {
	setMusicMode("Sequential", "sequentialLightsTab");
}

document.getElementById("sirenLightsTab").onclick = function () {
	setMusicMode("Siren", "sirenLightsTab");
}


// $('.toast').toast("show");

const toastElList = [].slice.call(document.querySelectorAll('.toast'))
const toastList = toastElList.map(function (toastEl) {
	return new bootstrap.Toast(toastEl)
})

// TODO find a way around this
// show and discard toasts to avoid them blocking on/off buttons, even though they are invisible
toastList.map((toast) => {
	toast.show();
});

setTimeout(function() {
	toastList.map((toast) => {
		toast.hide();
	});
}, 200);














