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

		-- Get active flag from config
		local isActive = context.config and context.config.changeDetectionActive or false
		changedetector.configure(isActive)

		-- Setup monitoring for monster cards
		-- HP uses a getter, other fields are public
		local monsterCardFields = {
            -- TODO Add more later
			{name = "hp", getter = function(obj) return obj:getHp() end},
		}

		if context.modified then
			local monsterCards = context.modified:getMonsterCards()
			changedetector.monitor("Monster Cards", monsterCards, monsterCardFields, function(obj)
				-- Format as "cardName (cardId)"
				local name = "Unknown"
				if obj.name and obj.name.toString then
					name = obj.name:toString()
				end
				local id = obj.id or "?"
				return name .. " (" .. tostring(id) .. ")"
			end)

			-- Log what we're tracking
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

