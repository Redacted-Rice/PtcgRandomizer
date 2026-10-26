-- Removes playable trainer proxies from evo lines by demoting evolutions off each proxy.
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")

local module
module = {
	id = "evo_line_playable_trainers",
	name = "Remove Playable Trainers From Evo Lines",
	description = "Removes playable trainer cards from evolution lines. Any evolutions will become basics and any"
			.. " cards in the evo line will be adjusted accordingly."
			.. " Evo line metadata will need to be re-run if needed after this",
	groups = { "Monsters", "Evolutions", "Trainers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {},
	seeded = false,
	execute = function(context, args)
		return module.fixPlayableTrainers(context, args)
	end,
}

function module.fixPlayableTrainers(context, args)
	evo_line_randomize_utils.init(context)

	local modified = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	local toModifyByPrevEvo = modified:filter(function(card)
		return not card.prevEvoName:isEmpty()
	end):groupBy("prevEvoName:toString")
	-- Get the proxies
	local modifiedProxyNames = modified:filter(function(card)
		return card.isTrainerProxy
	end):select("name:toString"):removeDuplicates()

	-- For each proxy, demote any evos
	modifiedProxyNames:each(function(modifiedProxyName)
		evo_line_randomize_utils.demoteEvosOf(modifiedProxyName, toModifyByPrevEvo,
				"evo_line_playable_trainers", " of proxy " .. modifiedProxyName)
	end)

	evo_line_randomize_utils.syncEvoAiData(context, modified)
end

return module
