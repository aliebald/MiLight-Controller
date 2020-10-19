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
	let url = "command=" + message + "&zone=" + zone;
	console.log("sending", url);
	let xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState === 4 && this.status === 200) {
			document.getElementById("demo").innerHTML =
			this.responseText;
			console.log(this.responseText);
		}
	};

	xhttp.open("GET", url, true);
	xhttp.send(message);
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
// TODO add saturation
colorPicker.on('input:end', function(color) {
	let c = (Math.floor((color.red / 32)) << 5) + (Math.floor((color.green / 32)) << 2) + Math.floor((color.blue / 64));
	// Since MiLight uses 8-Bit color, convert to 8 bit. Warning: This is WIP, since it seems like MiLight does not follow the standard 8 bit color scheme
	console.log("Color: red: " + color.red + ", green: " + color.green + ", blue: " + color.blue + ", 8 bit color: " + c + " = " + (c >>> 0).toString(2))
	sendCommand("setColorTo:" + c);
});

colorPicker.on('color:change', function(color) {
	console.log(color.hexString);
	document.getElementById("addColorBtn").style.background = color.hexString;
});

// Mode selector tabs
const cyclicLightsTab = document.getElementById("cyclicLightsTab");
const flashingLightsTab = document.getElementById("flashingLightsTab");
const pulseLightTab = document.getElementById("pulseLightTab");
const sequentialLightsTab = document.getElementById("sequentialLightsTab");
const sirenLightsTab = document.getElementById("sirenLightsTab");

cyclicLightsTab.onclick = function () {
	sendCommand("setMode:MCyclic");
}

flashingLightsTab.onclick = function () {
	sendCommand("setMode:MFlashing");
}

pulseLightTab.onclick = function () {
	sendCommand("setMode:MPulse");
}

sequentialLightsTab.onclick = function () {
	sendCommand("setMode:MSequential");
}

sirenLightsTab.onclick = function () {
	sendCommand("setMode:MSiren");
}





















