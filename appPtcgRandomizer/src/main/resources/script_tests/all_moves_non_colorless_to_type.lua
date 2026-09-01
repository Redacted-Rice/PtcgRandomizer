-- Same mixed costs on every case. Only the target energy type changes.
local card_sets = require("support.card_sets")
local fields = require("support.fields")

local function caseFor(energyType)
	return {
		name = energyType:lower(),
		module = "all_moves_non_colorless_to_type",
		args = {
			energyType = energyType,
		},
		cards = card_sets.MIXED_COST_CARDS,
		expect = {
			{
				id = "MONSTER_001",
				moves = fields.moves({
					{ name = "Burn", costs = fields.costs({ [energyType] = 2, COLORLESS = 1 }) },
					{ name = "Forest Fire", costs = fields.costs({ [energyType] = 2, COLORLESS = 1 }) },
				}),
			},
			{
				id = "MONSTER_002",
				moves = fields.moves({
					{ name = "Tackle", costs = fields.costs({ [energyType] = 2, COLORLESS = 1 }) },
					{ name = "Slam", costs = fields.costs({ [energyType] = 2 }) },
				}),
			},
		},
	}
end

return {
	caseFor("FIRE"),
	caseFor("GRASS"),
	caseFor("LIGHTNING"),
	caseFor("WATER"),
	caseFor("FIGHTING"),
	caseFor("PSYCHIC"),
}
