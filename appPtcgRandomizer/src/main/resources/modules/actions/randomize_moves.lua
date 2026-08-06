local randomizer = require("randomizer")

local module
module = {
	id = "randomize_moves",
	name = "Randomize Existing Moves",
	description = "Randomizes existing move slots using a mixed attack+power pool of any type. Keeps the same number of moves per card.",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.8",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeMoves(context, args)
	end,
}

function module.randomizeMoves(context)
	randomizer.changedetector.pushMoveChangeDisplay({ "name", "damage" })

	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local moveTargets = randomizer.list(context.modified:getRandomizableMoves(false, false))
	-- Get the pool of moves to draw from. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local movePool = randomizer.list(context.modified:getRandomizableMoves(true, false))

	-- Randomize each target slot by setting the move on the host card so it sticks
	-- (we shouldn't modify the move directly)
	movePool:useToRandomize(moveTargets, function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
