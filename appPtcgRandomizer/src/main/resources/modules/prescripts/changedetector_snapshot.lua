-- Takes a snapshot for change detection before each module
return {
	id = "PtcgrChangeDetectorSnapshot",
	name = "Ptcgr Change Detector Snapshot",
	description = "Take snapshot before each module",
	when = "module",
	author = "Redacted Rice",
	version = "0.9.0",
	requires = {
		PtcgRandomizer = "0.2.0",
		PtcgrChangeDetectorSetup = "0.7.0",
	},

	execute = function(context)
		local changedetector = require("randomizer").changedetector
		changedetector.takeSnapshots()
	end,
}
