local randomizer = require("randomizer")

local module
module = {
	id = "randomize_attacks_within_type",
	name = "Randomize Existing Attacks Within Type",
	description = "Randomizes existing attack slots using an attack pool built only from same type cards. Keeps the same number of attacks per card.",
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

function module.randomizeAttacksWithinType(context)
	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local attackTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):filter("isAttack")
	-- Get the pools of moves to use by type. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local poolsByType = randomizer.list(context.modified:getRandomizableMoves(true, false)):
		filter("isAttack"):groupBy("getSourceCard:type")

	-- Randomize each move based on its host card's type and setting the move slot on the
	-- host card to the new value so it sticks (we shouldn't modify the move directly)
	poolsByType:useToRandomize(attackTargets, "getSourceCard:type", function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
