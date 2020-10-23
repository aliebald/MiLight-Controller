var zone = 0;
var speed = 100;
var brightness = 80;

function toggleNav() {
	let nav = document.getElementById("sidenav");
	if(nav.style.width === "0px") {
		nav.style.width = "200px";
	} else {
		nav.style.width = "0";
	}
}

function closeNav() {
	document.getElementById("sidenav").style.width = "0";
}

function setZone(zoneNr) {
	switch (zoneNr) {
		case 1:
			zone = 1;
			break;
		case 2:
			zone = 2;
			break;
		case 3:
			zone = 3;
			break;
		case 4:
			zone = 4;
			break;
		default:
			zone = 0; 
	}
	console.log("changed zone to:", zone);
}

function sendCommand(message) {
	send("command", "POST", "text/plain;", `command=${message}&zone=${zone}`, () => {});
}

function send(url, method, contentType, message, replyFunction) {
	console.log("sending", message);
	let xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
		if (this.readyState === 4 && this.status === 200) {
			document.getElementById("response").innerHTML = this.responseText;
			console.log(this.responseText);
			replyFunction(this.responseText);
		}
	};

	xmlhttp.open(method, url, true);
	xmlhttp.setRequestHeader("Content-Type", contentType);
	xmlhttp.send(message);
}

// Slider functions
const slider = document.getElementById("speedSlider");
const number = document.getElementById("speedNumber");
const brightnessSlider = document.getElementById("brightnessSlider");

// Update the current slider value (each time you drag the slider handle)
slider.oninput = function() {
  number.value = this.value;
  speed = this.value;
  console.log("oninput called on slider: ", this.value, speed);
}

number.oninput = function() {
  slider.value = this.value;
  speed = this.value;
  console.log("oninput called on number: ", this.value, speed);
}

brightnessSlider.onmouseup = function () {
	brightness = this.value; // TODO Required?
	console.log("Setting brightness to: ", brightness);
	sendCommand("setBrightness:" + brightness);
}

// Color picker logic
var colorPicker = new iro.ColorPicker("#picker", {
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

function settingsReady (set){
	settings = JSON.parse(set);

	document.getElementById('openBrowserOnStart').checked	= settings.openBrowserOnStart;
	document.getElementById('activeTargetDataLine').value	= settings.activeTargetDataLine;
	document.getElementById('bridgeIpAddress').value		= settings.bridgeIpAddress;
	document.getElementById('bridgePort').valueAsNumber	= settings.bridgePort;

	const dropdown = document.getElementById('activeTargetDataLine');
	dropdown.innerHTML = "";

	settings.possibleTargetDataLines.forEach(function (item, index) {
		if(item === settings.activeTargetDataLine) {
			dropdown.innerHTML += `<option value=\"${item}\">${item}</option>\n`;
		} else {
			dropdown.innerHTML += `<option value=\"${item}\">${item}</option>\n`;
		}
	});

	showCustomColors();
}

function applySettings() {
	// update settings json
	settings.activeTargetDataLine	= document.getElementById('activeTargetDataLine').value;
	settings.openBrowserOnStart		= document.getElementById('openBrowserOnStart').checked;
	settings.bridgeIpAddress		= document.getElementById('bridgeIpAddress').value;
	settings.bridgePort				= document.getElementById('bridgePort').valueAsNumber;

	// send settings json
	console.log(settings);

	// TODO better user feedback, more options, toasts
	let onReply = function(response) {
		if (response === "successfully updated settings") {
			$('#settingsModal').modal('hide');
		}
	};

	send("applySettings", "POST", "application/json;", JSON.stringify(settings), onReply);
}

// Adds the current color of the colorPicker as a customColor to settings.clientSettings.customColors and sends them to the server
function addCustomColor() {
	// check if the exact color already exists
	if(document.getElementById(`${colorPicker.color.hsl.h}${colorPicker.color.hsl.s}${colorPicker.color.hsl.l}`)) {
		console.log("Custom color not added, because it already exists");
	} else {
		const customColor = {
			"hsl": colorPicker.color.hsl,
			"hex": colorPicker.color.hexString
		};

		console.log("adding", customColor);
		settings.clientSettings.customColors.push(customColor);
		applySettings();
		addCustomColorBtn(customColor);
	}
}

// Adds all custom colors saved in the settings file to the site
function showCustomColors() {
	const colorButtons = document.getElementById('colorButtons');
	const insertIndex = colorButtons.innerHTML.indexOf("<!-- Add custom color button -->");
	let customColorBtn = ""

	// Add a button for every custom color
	settings.clientSettings.customColors.forEach(function (item, index) {
		console.log(item);
		customColorBtn += `<div class="col-xl-3 col-lg-3 col-md-3 col-sm-4 py-2"><button type="button" onclick="setColorTo(${item.hsl.h},${item.hsl.s},${item.hsl.l})" class="btn color" style="background: ${item.hex}" id="${item.hsl.h}${item.hsl.s}${item.hsl.l}"></button></div>`;
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
	let customColorBtn = `<div class="col-xl-3 col-lg-3 col-md-3 col-sm-4 py-2"><button type="button" onclick="setColorTo(${customColor.hsl.h},${customColor.hsl.s},${customColor.hsl.l})" class="btn color" style="background: ${customColor.hex}" id="${customColor.hsl.h}${customColor.hsl.s}${customColor.hsl.l}"></button></div>`;

	colorButtons.innerHTML = [colorButtons.innerHTML.slice(0, insertIndex), customColorBtn, colorButtons.innerHTML.slice(insertIndex)].join('');
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
document.getElementById("cyclicLightsTab").onclick = function () {
	sendCommand("setMode:MCyclic");
}

document.getElementById("flashingLightsTab").onclick = function () {
	sendCommand("setMode:MFlashing");
}

document.getElementById("pulseLightTab").onclick = function () {
	sendCommand("setMode:MPulse");
}

document.getElementById("sequentialLightsTab").onclick = function () {
	sendCommand("setMode:MSequential");
}

document.getElementById("sirenLightsTab").onclick = function () {
	sendCommand("setMode:MSiren");
}





















