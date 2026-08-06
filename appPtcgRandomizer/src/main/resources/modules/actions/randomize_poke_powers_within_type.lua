local randomizer = require("randomizer")

local module
module = {
	id = "randomize_poke_powers_within_type",
	name = "Randomize Existing Poke Powers Within Type",
	description = "Randomizes existing poke power slots using a poke power pool built only from same type cards. Keeps the same number of poke powers per card.",
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

function module.randomizePokePowersWithinType(context)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local powerTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):filter("isPokePower")
	-- Get the pools of moves to use by type. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local poolsByType = randomizer.list(context.modified:getRandomizableMoves(true, false)):
		filter("isPokePower"):groupBy("getSourceCard:type")

	-- Randomize each move based on its host card's type and setting the move slot on the
	-- host card to the new value so it sticks (we shouldn't modify the move directly)
	poolsByType:useToRandomize(powerTargets, "getSourceCard:type", function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
