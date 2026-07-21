local randomizer = require("randomizer")

local module
module = {
	id = "all_moves_match_type",
	name = "All Moves Match Type",
	description = "Sets each move's non-colorless energy costs to match its card's type",
	seeded = false,
	groups = { "moves" },
	modifies = { "moves" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.setMoveCostsToMatchType(context, args)
	end,
}

-- Keeps colorless costs as is and only change non colorless energy to the card's type
function module.setMoveCostsToMatchType(context)
	local EnergyType = context.EnergyType
	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(mon)
		local energyType = mon.type:convertToEnergyType()
		for moveSlot = 0, mon:getNumMoves() - 1 do
			local move = mon:getMove(moveSlot)
			local colorlessCost = move:getCost(EnergyType.COLORLESS)
			local nonColorlessCost = move:getNonColorlessEnergyCosts()
			move:clearCosts()

			if energyType == EnergyType.COLORLESS then
				move:setCost(EnergyType.COLORLESS, colorlessCost + nonColorlessCost)
			else
				move:setCost(EnergyType.COLORLESS, colorlessCost)
				move:setCost(energyType, nonColorlessCost)
			end
			-- True = force set even for assignments
			mon:setMove(move, moveSlot, true)
		end
	end)
end

return module
