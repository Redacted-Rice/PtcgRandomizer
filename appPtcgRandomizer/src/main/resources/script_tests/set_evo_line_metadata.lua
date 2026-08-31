-- Derives evoLineId and evoLineMaxStage from prevEvoName chains.
-- LineOne is a two stager, LineTwo is a three stager.
return {
	{
		name = "assigns_line_metadata",
		module = "set_evo_line_metadata",
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
			{ id = "MONSTER_001", evoLineId = 1, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_002", evoLineId = 1, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_003_1", evoLineId = 1, evoLineMaxStage = "STAGE_1" },
			{ id = "MONSTER_010", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_011", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_012", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_013", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_014", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
			{ id = "MONSTER_023", evoLineId = 3, evoLineMaxStage = "BASIC" },
			{ id = "MONSTER_024", evoLineId = 3, evoLineMaxStage = "BASIC" },
		},
	},
}
