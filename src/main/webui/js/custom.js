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
colorPicker.on('input:change', function (color){
	let curTime = (new Date()).getTime();
	if (curTime > lastUpdate + 150) {
		lastUpdate = curTime;

		// Adjust the color a bit, since MiLight seems to have its colorscheme a bit off
		let adjustment = 40;
		let adjustedColor = color.hsl.h + adjustment;
		if (adjustedColor > 360) {
			adjustedColor -= 360;
		}

		// scale down to 8 bit
		let c = Math.round((adjustedColor) * (256 / 360));
		sendCommand("setColorTo:" + c);

		//TODO include saturation (when supported by the Bridge);
	}
})

// Settings
let settings;
send("settings.json", "GET", "application/json;", "settings.json", settingsReady);

function settingsReady (set){
	settings = JSON.parse(set);

	document.getElementById('openBrowserOnStart').checked	= settings.openBrowserOnStart;
	document.getElementById('activeTargetDataLine').value	= settings.activeTargetDataLine;
	document.getElementById('bridgeIpAddress').value		= settings.bridgeIpAddress;
	document.getElementById('bridgePort').valueAsNumber	= settings.bridgePort;

	settings.possibleTargetDataLines.forEach(function (item, index) {
		if(item === settings.activeTargetDataLine) {
			document.getElementById('activeTargetDataLine').innerHTML = document.getElementById('activeTargetDataLine').innerHTML + "<option value=\"" + item + "\" selected>" + item + "</option>\n";
		} else {
			document.getElementById('activeTargetDataLine').innerHTML = document.getElementById('activeTargetDataLine').innerHTML + "<option value=\"" + item + "\">" + item + "</option>\n";
		}
	});
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

	send("applySettings", "POST", "application/json;", JSON.stringify(settings), onReply)
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





















