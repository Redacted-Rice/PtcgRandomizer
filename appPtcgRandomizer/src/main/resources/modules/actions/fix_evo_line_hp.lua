-- Enforces non-decreasing HP up each evolution line by stage.
-- Run after HP randomization and set_evo_line_metadata.
local randomizer = require("randomizer")

local module
module = {
	id = "fix_evo_line_hp",
	name = "Fix Evo Line HP",
	description = "Raises HP within each evolution line so it is non decreasing by stage",
	groups = { "cards" },
	modifies = { "hp" },
	author = "Redacted Rice",
	version = "0.5",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	seeded = false,
	execute = function(context, args)
		return module.fixEvoLineHp(context, args)
	end,
}

function module.fixEvoLineHp(context)
	local byEvoLine = randomizer.groupBy(context.modified:getRandomizableMonsterCards(), "evoLineId")
	byEvoLine:each(function(_, line)
		-- Group by EvolutionStage's underlying value so sort is numeric
		local byStage = randomizer.groupBy(line, function(mc)
			return mc.stage:getValue()
		end)
		local prevStageMaxHp = 0

		-- Sort by stage so we go through in the right order and continue until its all reached
		byStage:sort():each(function(_, cardsAtStage)
			cardsAtStage:each(function(mc)
				if mc:getHp() < prevStageMaxHp then
					mc:setHp(prevStageMaxHp)
				end
			end)
			prevStageMaxHp = cardsAtStage:max("getHp")
		end)
	end)
end

return module
