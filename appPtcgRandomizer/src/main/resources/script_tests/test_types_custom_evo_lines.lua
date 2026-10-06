local seed = 42

local typeWeights = {
	FIRE = 2,
	WATER = 1,
	LIGHTNING = 1,
	GRASS = 1,
	PSYCHIC = 1,
	FIGHTING = 0,
	COLORLESS = 0,
}

-- 6 lines to match the 6 in type weights
local cards = {
	-- Split branch evo for funsies
	{ id = "MONSTER_001", name = "FireBase", type = "MONSTER_FIRE", stage = "BASIC",
			prevEvoName = "", evoLineId = 1 },
	{ id = "MONSTER_002", name = "FireBase", type = "MONSTER_FIRE", stage = "BASIC",
			prevEvoName = "", evoLineId = 1 },
	{ id = "MONSTER_003_1", name = "FireEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
			prevEvoName = "FireBase", evoLineId = 1 },
	{ id = "MONSTER_004", name = "FireEvo2", type = "MONSTER_FIRE", stage = "STAGE_1",
			prevEvoName = "FireBase", evoLineId = 1 },

	{ id = "MONSTER_010", name = "WaterBase", type = "MONSTER_WATER", stage = "BASIC",
			prevEvoName = "", evoLineId = 2 },
	{ id = "MONSTER_011", name = "WaterMid", type = "MONSTER_WATER", stage = "STAGE_1",
			prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_012", name = "WaterMid", type = "MONSTER_WATER", stage = "STAGE_1",
			prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_013", name = "WaterTop", type = "MONSTER_WATER", stage = "STAGE_2",
			prevEvoName = "WaterMid", evoLineId = 2 },

	{ id = "MONSTER_014", name = "LightningBase", type = "MONSTER_LIGHTNING", stage = "BASIC",
			prevEvoName = "", evoLineId = 3 },
	{ id = "MONSTER_015", name = "LightningEvo", type = "MONSTER_LIGHTNING", stage = "STAGE_1",
			prevEvoName = "LightningBase", evoLineId = 3 },

	{ id = "MONSTER_023", name = "GrassBase", type = "MONSTER_GRASS", stage = "BASIC",
			prevEvoName = "", evoLineId = 4 },

	{ id = "MONSTER_024", name = "PsyBase", type = "MONSTER_PSYCHIC", stage = "BASIC",
			prevEvoName = "", evoLineId = 5 },

	{ id = "MONSTER_031", name = "FireLineTwoBase", type = "MONSTER_FIRE", stage = "BASIC",
			prevEvoName = "", evoLineId = 6 },
	{ id = "MONSTER_032", name = "FireLineTwoEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
			prevEvoName = "FireLineTwoBase", evoLineId = 6 },
}

return {
	{
		name = "rom_weights_minimize_repeats",
		module = "types_custom_evo_lines",
		seed = seed,
		args = {
			approach = "MINIMIZE_REPEATS",
			typeWeights = typeWeights,
		},
		original = cards,
		modified = cards,
		expect = {
			-- Match type weights exactly
			{ id = "MONSTER_001", type = "MONSTER_GRASS" },
			{ id = "MONSTER_002", type = "MONSTER_GRASS" },
			{ id = "MONSTER_003_1", type = "MONSTER_GRASS" },

			{ id = "MONSTER_010", type = "MONSTER_FIRE" },
			{ id = "MONSTER_011", type = "MONSTER_FIRE" },
			{ id = "MONSTER_012", type = "MONSTER_FIRE" },
			{ id = "MONSTER_013", type = "MONSTER_FIRE" },

			{ id = "MONSTER_014", type = "MONSTER_WATER" },
			{ id = "MONSTER_015", type = "MONSTER_WATER" },

			{ id = "MONSTER_023", type = "MONSTER_FIRE" },

			{ id = "MONSTER_024", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_031", type = "MONSTER_LIGHTNING" },
			{ id = "MONSTER_032", type = "MONSTER_LIGHTNING" },
		},
	},
	{
		name = "rom_weights_fully_random",
		module = "types_custom_evo_lines",
		seed = seed,
		args = {
			approach = "FULLY_RANDOM",
			typeWeights = typeWeights,
		},
		original = cards,
		modified = cards,
		expect = {
			{ id = "MONSTER_001", type = "MONSTER_GRASS" },
			{ id = "MONSTER_002", type = "MONSTER_GRASS" },
			{ id = "MONSTER_003_1", type = "MONSTER_GRASS" },

			{ id = "MONSTER_010", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_011", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_012", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_013", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_014", type = "MONSTER_FIRE" },
			{ id = "MONSTER_015", type = "MONSTER_FIRE" },

			{ id = "MONSTER_023", type = "MONSTER_FIRE" },

			{ id = "MONSTER_024", type = "MONSTER_GRASS" },

			{ id = "MONSTER_031", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_032", type = "MONSTER_PSYCHIC" },
		},
	},
}
