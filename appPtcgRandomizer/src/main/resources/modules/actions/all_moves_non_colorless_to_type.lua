local randomizer = require("randomizer")

local module
module = {
	id = "all_moves_non_colorless_to_type",
	name = "All Non-Colorless To Type",
	description = "Changes all moves non-colorless energy types to the specified type",
	seeded = false,
	groups = { "Monsters", "Moves", "Attacks", "Energy Type" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "energyType",
			definition = {
				type = "enum",
				constraint = "EnergyType",
				exclude = { "COLORLESS", "UNUSED_TYPE" },
			},
			default = "FIRE",
		},
	},
	execute = function(context, args)
		return module.setNonColorlessToType(context, args)
	end,
}

function module.setNonColorlessToType(context, args)
	local EnergyType = context.EnergyType
	local targetType = EnergyType[args.energyType]
	randomizer.list(context.modified:getRandomizableMonsterCards()):each(function(mon)
		for moveSlot = 0, mon:getNumMoves() - 1 do
			local move = mon:getMove(moveSlot)
			local colorlessCost = move:getCost(EnergyType.COLORLESS)
			local nonColorlessCost = move:getNonColorlessEnergyCosts()
			move:clearCosts()
			move:setCost(EnergyType.COLORLESS, colorlessCost)
			move:setCost(targetType, nonColorlessCost)
			-- True = force set even for assignments
			mon:setMove(move, moveSlot, true)
		end
	end)
end

return module
