-- Logging only. Cards should be unchanged.
local card_sets = require("support.card_sets")

-- Cards used don't really matter. They shouldn't change is the big
-- thing that this test tests
local cards = card_sets.MIXED_COST_CARDS

local unchanged = {
	{
		id = cards[1].id,
		type = cards[1].type,
		numMoves = 2,
		moves = cards[1].moves,
	},
	{
		id = cards[2].id,
		type = cards[2].type,
		numMoves = 2,
		moves = cards[2].moves,
	},
}

local fieldValues = {
	"Lvl", "Type", "HP", "Stage", "Prev Evo", "Num Moves",
	"Retreat", "Weakness", "Resistance", "Category", "Dex #", "Evo Line",
	"Max Stage", "Move 1", "Move 1 Cost", "Move 1 Dmg", "Move 2",
	"Move 2 Cost", "Move 2 Dmg",
}

local cases = {
	{
		name = "default_fields",
		module = "log_monster_cards",
		cards = cards,
		expect = unchanged,
	},
	{
		name = "all_fields",
		module = "log_monster_cards",
		args = {
			fields = fieldValues,
		},
		cards = cards,
		expect = unchanged,
	},
}

for _, field in ipairs(fieldValues) do
	table.insert(cases, {
		name = "field_" .. field:gsub("[^%w]+", "_"),
		module = "log_monster_cards",
		args = {
			fields = { field },
		},
		cards = cards,
		expect = unchanged,
	})
end

return cases
