local randomizer = require("randomizer")

local module
module = {
	id = "randomize_attacks",
	name = "Randomize Attacks",
	description = "Randomizes attack slots from the attack pool, leaving poke-powers unchanged",
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
	-- Get all the slots that we are randomizing
	local attackSlots = randomizer.list(context.modified:getMonsterCards()):flatMapNTimes(
		"getNumMoves",
		function(card, index)
			if card:getMove(index - 1):isAttack() then
				return { card = card, slot = index - 1 }
			end
		end
	)

	-- Get all attacks from modified moves
	local attackPool = randomizer.list(context.modified:allMoves()):filter(function(move)
		return move:isAttack()
	end)

	-- And randomize them
	attackPool:useToRandomize(attackSlots, function(target, move)
		target.card:setMove(move, target.slot)
	end)
end

return module
