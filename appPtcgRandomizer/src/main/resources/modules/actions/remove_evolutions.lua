-- Turns every monster into a basic with no previous evolution link.
local randomizer = require("randomizer")

local module
module = {
	id = "remove_evolutions",
	name = "Remove Evolutions",
	description = "Removes evolutions by making all monsters basics and cleaning up related ROM internals",
	groups = { "Monsters", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	execute = function(context, args)
		return module.removeEvolutions(context, args)
	end,
}

function module.removeEvolutions(context)
	local basicStage = context.EvolutionStage.BASIC
	local hasEvolution = context.CardAiFlags.HAS_EVOLUTION
	local encourageEvo = context.CardAiFlags.ENCOURAGE_EVO

	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(card)
		card.stage = basicStage
		card.prevEvoName:clear()
		card.aiFlags:remove(hasEvolution)
		card.aiFlags:remove(encourageEvo)
	end)
end

return module
