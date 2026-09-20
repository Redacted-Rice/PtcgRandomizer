local card_sets = require("support.card_sets")

local aiFlagCards = {
	{
		id = "MONSTER_001",
		name = "BaseMon",
		type = "MONSTER_FIRE",
		stage = "BASIC",
		aiInfo = "UNK_03",
		aiFlags = { "HAS_EVOLUTION" },
	},
	{
		id = "MONSTER_002",
		name = "EvoMon",
		type = "MONSTER_FIRE",
		stage = "STAGE_1",
		prevEvoName = "BaseMon",
		aiInfo = "ENCOURAGE_EVO",
	},
	{ id = "MONSTER_003_1", name = "SoloMon", type = "MONSTER_WATER", stage = "BASIC", aiInfo = "BENCH_UTILITY" },
}

local function cardsWithAiFlagOverrides()
	local byId = {}
	for _, card in ipairs(aiFlagCards) do
		byId[card.id] = card
	end

	local merged = {}
	for _, card in ipairs(card_sets.STD_TEST_CARDS_ROM) do
		table.insert(merged, byId[card.id] or card)
	end
	return merged
end

local testCards = cardsWithAiFlagOverrides()

return {
	{
		name = "clears_evo_links_has_evolution_and_encourage_evo",
		module = "evolutions_remove",
		args = {},
		original = testCards,
		modified = testCards,
		expect = {
			-- UNK_03 is a distinct aiInfo value, not ENCOURAGE_EVO — leave it
			{ id = "MONSTER_001", stage = "BASIC", prevEvoName = "", aiInfo = "UNK_03", aiFlags = {} },
			{ id = "MONSTER_002", stage = "BASIC", prevEvoName = "", aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_003_1", stage = "BASIC", aiInfo = "BENCH_UTILITY", aiFlags = {} },
		},
	},
}
