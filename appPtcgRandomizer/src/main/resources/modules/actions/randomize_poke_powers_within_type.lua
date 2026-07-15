local randomizer = require("randomizer")

local module
module = {
	id = "randomize_poke_powers_within_type",
	name = "Randomize Poke Powers Within Type",
	description = "Randomizes poke power only within each energy type, leaving attacks unchanged",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizePokePowersWithinType(context, args)
	end,
}

local function setMoveFromTarget(target, move)
	target.card:setMove(move, target.slot)
end

function module.randomizePokePowersWithinType(context)
	-- Get all the slots that we are randomizing
	local powerSlots = randomizer.list(context.modified:getMonsterCards()):flatMapNTimes(
		"getNumMoves",
		function(card, index)
			if card:getMove(index - 1):isPokePower() then
				return { card = card, slot = index - 1 }
			end
		end
	)

    -- TODO later: Need to figure out how to handle exclusions with this as we can't pull from
    -- moves directly as they won't keep the type for now. Maybe add the host card's type to
    -- the moves?
	-- Group them by type
	local powerSlotsByType = randomizer.groupBy(powerSlots, function(target)
		return target.card.type
	end)

    -- And then pull out the moves
	local powerPoolsByType = powerSlotsByType:applyToEachList("select", function(target)
		return target.card:getMove(target.slot)
	end)

	powerPoolsByType:useToRandomize(powerSlotsByType:toList(), function(target)
		return target.card.type
	end, setMoveFromTarget)
end

return module
