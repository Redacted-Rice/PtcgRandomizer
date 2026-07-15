local randomizer = require("randomizer")

local module
module = {
	id = "randomize_moves_within_type",
	name = "Randomize Moves Within Type",
	description = "Randomizes all move slots using a mixed pool built only from same-type cards",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeMovesWithinType(context, args)
	end,
}

local function setMoveFromTarget(target, move)
	target.item:setMove(move, target.index - 1)
end

function module.randomizeMovesWithinType(context)
	local slots = randomizer.list(context.modified:getMonsterCards())
        :flatMapNTimes("getNumMoves")

    -- TODO later: Need to figure out how to handle exclusions with this as we can't pull from
    -- moves directly as they won't keep the type for now. Maybe add the host card's type to
    -- the moves?
	-- Group them by type
	local slotsByType = randomizer.groupBy(slots, function(target)
		return target.item.type
	end)

	-- Get all the attacks from those slots and group them by type
	local poolsByType = slotsByType:applyToEachList("select", function(target)
		return target.item:getMove(target.index - 1)
	end)

	poolsByType:useToRandomize(slots, function(target)
		return target.item.type
	end, setMoveFromTarget)
end

return module
