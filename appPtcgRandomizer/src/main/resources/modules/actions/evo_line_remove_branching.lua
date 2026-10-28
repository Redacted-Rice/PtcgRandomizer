-- Removes branching the same way playable trainers are removed from evo lines: demote all evos.
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")

local module
module = {
	id = "evo_line_remove_branching",
	name = "Remove Branching Evolutions",
	description = "Removes branching evo lines by making all evolutions basics and adjusting any"
			.. " cards in the evo line accordingly."
			.. " Evo line metadata will need to be re-run if needed after this",
	groups = { "Monsters", "Evolutions" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	seeded = false,
	execute = function(context, args)
		return module.fixBranchingEvoLines(context, args)
	end,
}

function module.fixBranchingEvoLines(context, args)
	evo_line_randomize_utils.init(context)

	local modified = randomizer.list(context.modified.cards:getRandomizableMonsterCardsWithProxies())
	local toModifyByPrevEvo = modified:filter(function(card)
		return not card.prevEvoName:isEmpty()
	end):groupBy("prevEvoName:toString")
	-- Get the branching evo cards
	local modifiedBranches = modified:filter(function(card)
		local evos = toModifyByPrevEvo:get(card.name:toString())
		return evos ~= nil and evos:select("name:toString"):removeDuplicates():size() > 1
	end):select("name:toString"):removeDuplicates()

	-- For each branching, demote any evos
	modifiedBranches:each(function(modifiedBranchingName)
		evo_line_randomize_utils.demoteEvosOf(modifiedBranchingName, toModifyByPrevEvo,
				"evo_line_remove_branching", " of branching card " .. modifiedBranchingName)
	end)

	evo_line_randomize_utils.syncEvoAiData(context, modified)
end

return module
