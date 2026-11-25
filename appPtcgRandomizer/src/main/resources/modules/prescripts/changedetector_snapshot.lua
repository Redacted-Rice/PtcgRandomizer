-- Takes a snapshot for change detection before each module
local randomizer = require("randomizer")

local script
script = {
	id = "PtcgrChangeDetectorSnapshot",
	name = "Ptcgr Change Detector Snapshot",
	description = "Take snapshot before each module",
	when = "module",
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
		PtcgrChangeDetectorSetup = "0.9",
	},
	execute = function(args)
		return script.takeSnapshots(args)
	end,
}

function script.takeSnapshots()
	randomizer.changedetector.takeSnapshots()
end

return script
