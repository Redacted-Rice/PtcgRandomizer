-- Checks for changes and logs if there are any
local randomizer = require("randomizer")

local script
script = {
	id = "PtcgrChangeDetectorDetect",
	name = "Ptcgr Change Detector Detect",
	description = "Detect and log changes after each module",
	when = "module",
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
		PtcgrChangeDetectorSetup = "0.7",
		PtcgrChangeDetectorSnapshot = "0.9",
	},
	execute = function(context, args)
		return script.detectAndLogChanges(context, args)
	end,
}

function script.detectAndLogChanges(context)
	local changedetector = randomizer.changedetector

	local changes = changedetector.detectChanges()
	if changedetector.hasChanges(changes) then
		local formatOptions = {
			leadingNewline = true,
		}
		if context.executionModule then
			formatOptions.moduleName = context.executionModule
		end

		logger.info(changedetector.formatChangesTable(changes, formatOptions))
	end
end

return script
