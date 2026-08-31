-- Use the same seed on every case
local seed = 42

-- Typed evo lines with reprints plus a second FIRE line so KEEP vs REMOVE is visible.
local original = {
	{ id = "MONSTER_001", name = "FireBase", type = "MONSTER_FIRE", stage = "BASIC", evoLineId = 1 },
	{ id = "MONSTER_002", name = "FireBase", type = "MONSTER_FIRE", stage = "BASIC", evoLineId = 1 },
	{ id = "MONSTER_003_1", name = "FireEvo", type = "MONSTER_FIRE", stage = "STAGE_1", prevEvoName = "FireBase", evoLineId = 1 },

	{ id = "MONSTER_010", name = "WaterBase", type = "MONSTER_WATER", stage = "BASIC", evoLineId = 2 },
	{ id = "MONSTER_011", name = "WaterMid", type = "MONSTER_WATER", stage = "STAGE_1", prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_012", name = "WaterMid", type = "MONSTER_WATER", stage = "STAGE_1", prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_013", name = "WaterTop", type = "MONSTER_WATER", stage = "STAGE_2", prevEvoName = "WaterMid", evoLineId = 2 },

	{ id = "MONSTER_014", name = "LightningBase", type = "MONSTER_LIGHTNING", stage = "BASIC", evoLineId = 3 },
	{ id = "MONSTER_015", name = "LightningEvo", type = "MONSTER_LIGHTNING", stage = "STAGE_1", prevEvoName = "LightningBase", evoLineId = 3 },

	{ id = "MONSTER_023", name = "GrassBase", type = "MONSTER_GRASS", stage = "BASIC", evoLineId = 4 },

	{ id = "MONSTER_024", name = "PsyBase", type = "MONSTER_PSYCHIC", stage = "BASIC", evoLineId = 5 },

	{ id = "MONSTER_031", name = "FireLineTwoBase", type = "MONSTER_FIRE", stage = "BASIC", evoLineId = 6 },
	{ id = "MONSTER_032", name = "FireLineTwoEvo", type = "MONSTER_FIRE", stage = "STAGE_1", prevEvoName = "FireLineTwoBase", evoLineId = 6 },
}

-- Same cards with different types so ROM vs CURRENT is visible.
local modified = {
	{ id = "MONSTER_001", name = "FireBase", type = "MONSTER_COLORLESS", stage = "BASIC", evoLineId = 1 },
	{ id = "MONSTER_002", name = "FireBase", type = "MONSTER_COLORLESS", stage = "BASIC", evoLineId = 1 },
	{ id = "MONSTER_003_1", name = "FireEvo", type = "MONSTER_COLORLESS", stage = "STAGE_1", prevEvoName = "FireBase", evoLineId = 1 },

	{ id = "MONSTER_010", name = "WaterBase", type = "MONSTER_FIGHTING", stage = "BASIC", evoLineId = 2 },
	{ id = "MONSTER_011", name = "WaterMid", type = "MONSTER_FIGHTING", stage = "STAGE_1", prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_012", name = "WaterMid", type = "MONSTER_FIGHTING", stage = "STAGE_1", prevEvoName = "WaterBase", evoLineId = 2 },
	{ id = "MONSTER_013", name = "WaterTop", type = "MONSTER_FIGHTING", stage = "STAGE_2", prevEvoName = "WaterMid", evoLineId = 2 },

	{ id = "MONSTER_014", name = "LightningBase", type = "MONSTER_PSYCHIC", stage = "BASIC", evoLineId = 3 },
	{ id = "MONSTER_015", name = "LightningEvo", type = "MONSTER_PSYCHIC", stage = "STAGE_1", prevEvoName = "LightningBase", evoLineId = 3 },

	{ id = "MONSTER_023", name = "GrassBase", type = "MONSTER_PSYCHIC", stage = "BASIC", evoLineId = 4 },

	{ id = "MONSTER_024", name = "PsyBase", type = "MONSTER_PSYCHIC", stage = "BASIC", evoLineId = 5 },

	{ id = "MONSTER_031", name = "FireLineTwoBase", type = "MONSTER_GRASS", stage = "BASIC", evoLineId = 6 },
	{ id = "MONSTER_032", name = "FireLineTwoEvo", type = "MONSTER_GRASS", stage = "STAGE_1", prevEvoName = "FireLineTwoBase", evoLineId = 6 },
}

return {
	{
		name = "keep_duplicates",
		module = "even_rando_evo_line_types",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = original,
		modified = modified,
		-- ROM has 2 FIRE lines and 1 of other types. This distro matches
		expect = {
			{ id = "MONSTER_001", type = "MONSTER_FIRE" },
			{ id = "MONSTER_002", type = "MONSTER_FIRE" },
			{ id = "MONSTER_003_1", type = "MONSTER_FIRE" },

			{ id = "MONSTER_010", type = "MONSTER_GRASS" },
			{ id = "MONSTER_011", type = "MONSTER_GRASS" },
			{ id = "MONSTER_012", type = "MONSTER_GRASS" },
			{ id = "MONSTER_013", type = "MONSTER_GRASS" },

			{ id = "MONSTER_014", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_015", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_023", type = "MONSTER_WATER" },

			{ id = "MONSTER_024", type = "MONSTER_FIRE" },

			{ id = "MONSTER_031", type = "MONSTER_LIGHTNING" },
			{ id = "MONSTER_032", type = "MONSTER_LIGHTNING" },
		},
	},
	{
		name = "from_current",
		module = "even_rando_evo_line_types",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = original,
		modified = modified,
		-- CURRENT has 3 PSYCHIC lines and 1 of other types. This distro matches
		expect = {
			{ id = "MONSTER_001", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_002", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_003_1", type = "MONSTER_COLORLESS" },

			{ id = "MONSTER_010", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_011", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_012", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_013", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_014", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_015", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_023", type = "MONSTER_FIGHTING" },

			{ id = "MONSTER_024", type = "MONSTER_GRASS" },

			{ id = "MONSTER_031", type = "MONSTER_PSYCHIC" },
			{ id = "MONSTER_032", type = "MONSTER_PSYCHIC" },
		},
	},
	{
		name = "remove_duplicates",
		module = "even_rando_evo_line_types",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
		},
		original = original,
		modified = modified,
		-- Four unique CURRENT types, then two refills. FIGHTING and PSYCHIC double up, not PSYCHIC only like keep_duplicates.
		expect = {
			{ id = "MONSTER_001", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_002", type = "MONSTER_COLORLESS" },
			{ id = "MONSTER_003_1", type = "MONSTER_COLORLESS" },

			{ id = "MONSTER_010", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_011", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_012", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_013", type = "MONSTER_FIGHTING" },

			{ id = "MONSTER_014", type = "MONSTER_GRASS" },
			{ id = "MONSTER_015", type = "MONSTER_GRASS" },

			{ id = "MONSTER_023", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_024", type = "MONSTER_PSYCHIC" },

			{ id = "MONSTER_031", type = "MONSTER_FIGHTING" },
			{ id = "MONSTER_032", type = "MONSTER_FIGHTING" },
		},
	},
	{
		name = "fully_random",
		module = "even_rando_evo_line_types",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
		},
		original = original,
		modified = modified,
		-- FIRE landed on four lines. GRASS and LIGHTNING were unused.
		-- This is improbable but completely valid for FULLY_RANDOM
		expect = {
			{ id = "MONSTER_001", type = "MONSTER_FIRE" },
			{ id = "MONSTER_002", type = "MONSTER_FIRE" },
			{ id = "MONSTER_003_1", type = "MONSTER_FIRE" },

			{ id = "MONSTER_010", type = "MONSTER_FIRE" },
			{ id = "MONSTER_011", type = "MONSTER_FIRE" },
			{ id = "MONSTER_012", type = "MONSTER_FIRE" },
			{ id = "MONSTER_013", type = "MONSTER_FIRE" },

			{ id = "MONSTER_014", type = "MONSTER_FIRE" },
			{ id = "MONSTER_015", type = "MONSTER_FIRE" },

			{ id = "MONSTER_023", type = "MONSTER_GRASS" },

			{ id = "MONSTER_024", type = "MONSTER_FIRE" },

			{ id = "MONSTER_031", type = "MONSTER_WATER" },
			{ id = "MONSTER_032", type = "MONSTER_WATER" },
		},
	},
}
