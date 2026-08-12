-- Enforces non-decreasing HP up each evolution line by stage.
-- Run after HP randomization. Needs evoLineId on each card.
local randomizer = require("randomizer")

local module
module = {
	id = "fix_evo_line_hp",
	name = "Make Evo Line HP Consistent",
	description = "For each evolution line, ensures each stage has at least as much HP as the previous stage by increasing higher stage HPs to at least the highest value of the previous stage",
	groups = { "Monsters", "HP", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	seeded = false,
	execute = function(context, args)
		return module.fixEvoLineHp(context, args)
	end,
}

-- TODO later: Add an option to shuffle around HPs instead of just increasing?
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
