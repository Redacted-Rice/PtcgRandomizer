local randomizer = require("randomizer")

local module
module = {
	id = "all_moves_colorless",
	name = "All Moves Colorless",
	description = "Sets all move energy costs to colorless",
	seeded = false,
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.setAllMovesColorless(context, args)
	end,
}

function module.setAllMovesColorless(context)
	local EnergyType = context.EnergyType
	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(mon)
		for moveSlot = 0, mon:getNumMoves() - 1 do
			local move = mon:getMove(moveSlot)
			local totalCost = move:getCost(EnergyType.COLORLESS) + move:getNonColorlessEnergyCosts()
			move:clearCosts()
			move:setCost(EnergyType.COLORLESS, totalCost)
			mon:setMove(move, moveSlot)
		end
	end)
end

return module
