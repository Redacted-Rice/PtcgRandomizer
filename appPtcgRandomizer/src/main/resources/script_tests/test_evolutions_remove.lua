local card_sets = require("support.card_sets")

local aiFlagCards = {
	{ id = "MONSTER_001", name = "BaseMon", type = "MONSTER_FIRE", stage = "BASIC",
		aiFlags = { "BENCH_UTILITY", "HAS_EVOLUTION", "ENCOURAGE_EVO" } },
	{ id = "MONSTER_002", name = "EvoMon", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "BaseMon", aiFlags = { "ENCOURAGE_EVO" } },
	{ id = "MONSTER_003_1", name = "SoloMon", type = "MONSTER_WATER", stage = "BASIC",
		aiFlags = { "BENCH_UTILITY" } },
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
		name = "clears_evo_links_and_ai_flags",
		module = "evolutions_remove",
		args = {},
		original = testCards,
		modified = testCards,
		expect = {
			{ id = "MONSTER_001", stage = "BASIC", prevEvoName = "",
				aiFlags = { "BENCH_UTILITY" } },
			{ id = "MONSTER_002", stage = "BASIC", prevEvoName = "", aiFlags = {} },
			{ id = "MONSTER_003_1", stage = "BASIC", aiFlags = { "BENCH_UTILITY" } },
		},
	},
}
