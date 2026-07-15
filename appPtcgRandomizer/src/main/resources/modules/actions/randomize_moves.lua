local randomizer = require("randomizer")

local module
module = {
	id = "randomize_moves",
	name = "Randomize Moves",
	description = "Randomizes all move slots from one mixed attack+power pool (include-with-moves)",
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.7",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.randomizeMoves(context, args)
	end,
}

function module.randomizeMoves(context)
	local movePool = context.original:allMoves()
	local moveSlots = randomizer.list(context.modified:getMonsterCards()):flatMapNTimes("getNumMoves")
	randomizer.list(movePool):useToRandomize(moveSlots, function(target, move)
		-- Lua is 1 based, Java and its objects are 0 based
		target.item:setMove(move, target.index - 1)
	end)
end

return module
