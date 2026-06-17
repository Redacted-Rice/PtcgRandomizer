-- Configure the change detector for PTCG Randomizer
return {
	name = "changedetector_setup",
	description = "Setup change detection on cards being randomized",
	when = "randomize",
	author = "Redacted Rice",
	version = "0.1",
	requires = {
		UniversalRandomizerJava = "0.5.0",
		PtcgRandomizer = "0.2.0",
	},

	execute = function(context)
		local changedetector = require("randomizer").changedetector

		-- Respect the GUI/config toggle for whether change detection runs at all
		local isActive = context.config and context.config.changeDetectionActive or false
		changedetector.configure(isActive)

		if context.modified then
			local monsterCards = context.modified:getMonsterCards()

			-- Table layout is configured here so formatting stays simple in detectChanges()
			changedetector.monitor("Monster Cards", monsterCards, {
				title = "Monster Cards",
				headerEvery = 30, -- repeat column headers every 30 data rows
				trailingHeader = true,
				primaryKey = { header = "ID", align = "right", numeric = true, getter = function(obj) return obj:getIdValue() end },
				description = { header = "Name", getter = function(obj) return obj.name:toString() end },
				fields = {
					{ field = "hp", header = "HP", align = "right", getter = function(obj) return obj:getHp() end },
				},
			})

			local entries = changedetector.getMonitoredEntryNames()
			if #entries > 0 then
				logger.info("Change detection configured with " .. #entries .. " monitoring entries")
				for _, entryName in ipairs(entries) do
					logger.info("  - Monitoring: " .. entryName)
				end
			end
		end

		logger.info("Prescript changedetector_setup completed")
	end,
}
