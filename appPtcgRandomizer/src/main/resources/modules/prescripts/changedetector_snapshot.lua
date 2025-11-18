-- Takes a snapshot for change detection before each module
return {
	name = "changedetector_snapshot",
	description = "Take snapshot before each module",
	when = "module",
	author = "Redacted Rice",
	version = "0.1",
	requires = {
		UniversalRandomizerJava = "0.5.0",
		PtcgRandomizer = "0.2.0",
	},

	execute = function(context)
		local changedetector = require("randomizer").changedetector
		changedetector.takeSnapshots()
	end,
}

