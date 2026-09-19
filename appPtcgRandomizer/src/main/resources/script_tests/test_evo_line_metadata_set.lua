-- Derives evoLineId, evoBranchIds, and evoLineMaxStage from prevEvoName chains.
-- LineOne is a two stager, LineTwo is a three stager.
-- Branch shapes come from support.card_sets (single-line expects remapped to evoLineId 1).
local card_sets = require("support.card_sets")

local function metadataExpect(cards)
	local expect = {}
	for _, card in ipairs(cards) do
		table.insert(expect, {
			id = card.id,
			evoLineId = 1,
			evoBranchIds = card.evoBranchIds,
			evoLineMaxStage = card.evoLineMaxStage,
		})
	end
	return expect
end

return {
	{
		name = "assigns_line_metadata",
		module = "evo_line_metadata_set",
		cards = {
			{ id = "MONSTER_001", name = "LineOneBase", type = "MONSTER_FIRE", stage = "BASIC" },
			{ id = "MONSTER_002", name = "LineOneBase", type = "MONSTER_FIRE", stage = "BASIC" },
			{ id = "MONSTER_003_1", name = "LineOneEvo", type = "MONSTER_FIRE", stage = "STAGE_1", prevEvoName = "LineOneBase" },
			{ id = "MONSTER_010", name = "LineTwoBase", type = "MONSTER_WATER", stage = "BASIC" },
			{ id = "MONSTER_011", name = "LineTwoMid", type = "MONSTER_WATER", stage = "STAGE_1", prevEvoName = "LineTwoBase" },
			{ id = "MONSTER_012", name = "LineTwoMid", type = "MONSTER_WATER", stage = "STAGE_1", prevEvoName = "LineTwoBase" },
			{ id = "MONSTER_013", name = "LineTwoTop", type = "MONSTER_WATER", stage = "STAGE_2", prevEvoName = "LineTwoMid" },
			{ id = "MONSTER_014", name = "LineTwoTop", type = "MONSTER_WATER", stage = "STAGE_2", prevEvoName = "LineTwoMid" },
			{ id = "MONSTER_023", name = "Solo", type = "MONSTER_GRASS", stage = "BASIC" },
			{ id = "MONSTER_024", name = "Solo", type = "MONSTER_GRASS", stage = "BASIC" },
		},
		expect = {
			{ id = "MONSTER_001", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_002", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_003_1", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_010", evoLineId = 2, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_011", evoLineId = 2, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_012", evoLineId = 2, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_013", evoLineId = 2, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_014", evoLineId = 2, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_023", evoLineId = 3, evoBranchIds = { 3 }, evoLineMaxStage = "BASIC" },
			{ id = "MONSTER_024", evoLineId = 3, evoBranchIds = { 3 }, evoLineMaxStage = "BASIC" },
		},
	},
	{
		name = "assigns_branch_metadata_for_1_to_3",
		module = "evo_line_metadata_set",
		cards = card_sets.EVO_BRANCH_1_TO_3,
		expect = metadataExpect(card_sets.EVO_BRANCH_1_TO_3),
	},
	{
		-- Two root basics with no prevEvo are separate evo lines under metadata_set,
		-- even though evo_line_cards keeps them on one evoLineId for pooling.
		name = "assigns_branch_metadata_for_2_to_3",
		module = "evo_line_metadata_set",
		cards = card_sets.EVO_BRANCH_2_TO_3,
		expect = {
			{ id = "MONSTER_116", evoLineId = 1, evoBranchIds = { 1, 2, 3 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_117", evoLineId = 2, evoBranchIds = { 4 }, evoLineMaxStage = "BASIC" },
			{ id = "MONSTER_118", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_119", evoLineId = 1, evoBranchIds = { 2 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_120", evoLineId = 1, evoBranchIds = { 3 }, evoLineMaxStage = "STAGE_1" },
		},
	},
	{
		name = "assigns_branch_metadata_for_3_to_2",
		module = "evo_line_metadata_set",
		cards = card_sets.EVO_BRANCH_3_TO_2,
		expect = metadataExpect(card_sets.EVO_BRANCH_3_TO_2),
	},
	{
		name = "assigns_branch_metadata_for_proxy_3_to_2",
		module = "evo_line_metadata_set",
		cards = card_sets.EVO_BRANCH_PROXY_3_TO_2,
		expect = metadataExpect(card_sets.EVO_BRANCH_PROXY_3_TO_2),
	},
	{
		name = "clears_stale_metadata_before_reapply",
		module = "evo_line_metadata_set",
		cards = {
			-- Stale ids/branches/max from a prior layout; chains below are a simple stage-1 line
			{ id = "MONSTER_001", name = "ReapplyBase", type = "MONSTER_FIRE", stage = "BASIC",
				evoLineId = 99, evoBranchIds = { 7, 8 }, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_002", name = "ReapplyEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
				prevEvoName = "ReapplyBase",
				evoLineId = 99, evoBranchIds = { 7 }, evoLineMaxStage = "STAGE_2" },
		},
		expect = {
			{ id = "MONSTER_001", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_002", evoLineId = 1, evoBranchIds = { 1 }, evoLineMaxStage = "STAGE_1" },
		},
	},
}
