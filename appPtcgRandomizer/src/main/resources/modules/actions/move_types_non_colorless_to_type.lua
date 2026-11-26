local enums_def = require("modules.util.enums_def")
local randomizer = require("randomizer")

local module
module = {
	id = "move_types_non_colorless_to_type",
	name = "All Non-Colorless To Type",
	description = "Changes all moves non-colorless energy types to the specified Energy Type",
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
			displayName = "Energy Type",
			description = "Energy Type to change all non-colorless energy costs to be",
			definition = {
				type = "enum",
				constraint = "EnergyType",
				exclude = { enums_def.ENERGY_TYPE_COLORLESS, enums_def.ENERGY_TYPE_UNUSED },
			},
			default = enums_def.ENERGY_TYPE_FIRE,
		},
	},
	execute = function(args)
		return module.setNonColorlessToType(args)
	end,
}

function module.setNonColorlessToType(args)
	local EnergyType = context.EnergyType
	local targetType = EnergyType[args.energyType]
	randomizer.list(context.modified.cards:getRandomizableMonsterCards()):each(function(mon)
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
