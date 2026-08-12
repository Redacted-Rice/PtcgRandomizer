local randomizer = require("randomizer")

local module
module = {
	id = "randomize_moves_within_type",
	name = "Randomize Existing Moves Within Type",
	description = "Randomizes existing move slots using a mixed pool built only from same type cards. Keeps the same number of moves per card.",
	groups = { "Monsters", "Moves" },
	modifies = { "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeMovesWithinType(context, args)
	end,
}

function module.randomizeMovesWithinType(context)
	randomizer.changedetector.pushMoveChangeDisplay({ "name", "energyCost", "damage" })

	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local moveTargets = randomizer.list(context.modified:getRandomizableMoves(false, false))
	-- Get the pools of moves to use by type. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local poolsByType = randomizer.list(context.modified:getRandomizableMoves(true, false)):
		groupBy("getSourceCard:type")

	-- Randomize each move based on its host card's type and setting the move slot on the
	-- host card to the new value so it sticks (we shouldn't modify the move directly)
	poolsByType:useToRandomize(moveTargets, "getSourceCard:type", function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
