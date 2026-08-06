local randomizer = require("randomizer")

local module
module = {
	id = "randomize_poke_powers",
	name = "Randomize Existing Poke Powers",
	description = "Randomizes existing poke power slots using a poke power pool of any type. Keeps the same number of poke powers per card.",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizePokePowers(context, args)
	end,
}

function module.randomizePokePowers(context)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	-- Get the moves we are randomizing -- do not include assigned moves or empty moves so we only
	-- randomize moves actually allowed to be randomized
	local powerTargets = randomizer.list(context.modified:getRandomizableMoves(false, false)):filter("isPokePower")
	-- Get the pool of moves to draw from. This time include assigned moves
	-- (as long as they aren't also excluded from the pool) but not empty ones
	local powerPool = randomizer.list(context.modified:getRandomizableMoves(true, false)):filter("isPokePower")

	-- Randomize each target slot by setting the move on the host card so it sticks
	-- (we shouldn't modify the move directly)
	powerPool:useToRandomize(powerTargets, function(target, move)
		target:getSourceCard():setMove(move, target:getSourceMoveIndex())
	end)
end

return module
