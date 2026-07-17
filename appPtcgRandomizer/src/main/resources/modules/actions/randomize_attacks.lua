local randomizer = require("randomizer")

local module
module = {
	id = "randomize_attacks",
	name = "Randomize Existing Attacks",
	description = "Randomizes existing attack slots using an attack pool of any type. Keeps the same number of attacks per card.",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeAttacks(context, args)
	end,
}

function module.randomizeAttacks(context)
	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local attackTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):filter("isAttack")
	-- Get the pool of moves to draw from. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local attackPool = randomizer.list(context.modified:getRandomizableMoves(true, false)):filter("isAttack")

	-- Randomize each target slot by setting the move on the host card so it sticks
	-- (we shouldn't modify the move directly)
	attackPool:useToRandomize(attackTargets, function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
