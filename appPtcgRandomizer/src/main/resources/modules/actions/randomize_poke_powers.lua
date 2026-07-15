local randomizer = require("randomizer")

local module
module = {
	id = "randomize_poke_powers",
	name = "Randomize Poke Powers",
	description = "Randomizes poke power only, leaving attacks unchanged",
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
	-- Get all the slots that we are randomizing
	local powerSlots = randomizer.list(context.modified:getMonsterCards()):flatMapNTimes(
		"getNumMoves",
		function(card, index)
			if card:getMove(index - 1):isPokePower() then
				return { card = card, slot = index - 1 }
			end
		end
	)

	-- Get all poke powers from modified moves
	local powerPool = randomizer.list(context.modified:allMoves()):filter(function(move)
		return move:isPokePower()
	end)

	-- And randomize them
	powerPool:useToRandomize(powerSlots, function(target, move)
		target.card:setMove(move, target.slot)
	end)
end

return module
