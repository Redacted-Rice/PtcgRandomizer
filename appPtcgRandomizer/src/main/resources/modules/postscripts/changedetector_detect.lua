-- Checks for changes and logs if there are any
return {
	id = "PtcgrChangeDetectorDetect",
	name = "Ptcgr Change Detector Detect",
	description = "Detect and log changes after each module",
	when = "module",
	author = "Redacted Rice",
	version = "0.9.0",
	requires = {
		PtcgRandomizer = "0.2.0",
		PtcgrChangeDetectorSetup = "0.7.0",
		PtcgrChangeDetectorSnapshot = "0.9.0",
	},

	execute = function(context)
		local changedetector = require("randomizer").changedetector

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
	end,
}
