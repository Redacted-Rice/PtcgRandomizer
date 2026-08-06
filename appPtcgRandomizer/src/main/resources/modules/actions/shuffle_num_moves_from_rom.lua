local randomizer = require("randomizer")

local module
module = {
	id = "shuffle_num_moves_from_rom",
	name = "Shuffle Num Moves from ROM",
	description = "Shuffles each card's active move count using ROM move counts",
	groups = { "moves" },
	modifies = { "numMoves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.shuffleNumMoves(context, args)
	end,
}

-- TODO later: Add args/options
function module.shuffleNumMoves(context)
	randomizer.changedetector.pushMoveChangeDisplay({ "name" })

	local numMovesPool = randomizer.list(context.original:getRandomizableMonsterCards()):select("getNumMoves")
	local monsterMod = context.modified:getRandomizableMonsterCards()

	numMovesPool:useToRandomize(monsterMod, "setNumMoves", {
		consumable = true,
	})
end

return module
