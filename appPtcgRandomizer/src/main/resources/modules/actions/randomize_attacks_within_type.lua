local randomizer = require("randomizer")

local module
module = {
	id = "randomize_attacks_within_type",
	name = "Randomize Attacks Within Type",
	description = "Randomizes attack slots using an attack pool built only from same-type cards",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeAttacksWithinType(context, args)
	end,
}

local function setMoveFromTarget(target, move)
	target.card:setMove(move, target.slot)
end

function module.randomizeAttacksWithinType(context)
	-- Get all the slots that we are randomizing
	local attackSlots = randomizer.list(context.modified:getMonsterCards()):flatMapNTimes(
		"getNumMoves",
		function(card, index)
			if card:getMove(index - 1):isAttack() then
				return { card = card, slot = index - 1 }
			end
		end
	)

    -- TODO later: Need to figure out how to handle exclusions with this as we can't pull from
    -- moves directly as they won't keep the type for now. Maybe add the host card's type to
    -- the moves?
	-- Group them by type
	local attackSlotsByType = randomizer.groupBy(attackSlots, function(target)
		return target.card.type
	end)

	-- Get all the attacks from those slots and group them by type
	local attackPoolsByType = attackSlotsByType:applyToEachList("select", function(target)
		return target.card:getMove(target.slot)
	end)

	attackPoolsByType:useToRandomize(attackSlotsByType:toList(), function(target)
		return target.card.type
	end, setMoveFromTarget)
end

return module
