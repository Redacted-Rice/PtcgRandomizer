-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")
local fields = require("support.fields")

local baseline = {
	source = "ROM",
	duplicates = "KEEP_DUPLICATES",
	approach = "MINIMIZE_REPEATS",
	grouping = "ALL_TOGETHER",
	moveKind = "ALL_MOVES",
	withinType = false,
}

local expects = {
	-- Note that these use the num moves from CURRENT not source
	all_together = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Bubble" }, { name = "Flare" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Ember" }, { name = "Inferno" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "SoakPower" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Pollen" }, { name = "HeatWave" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "SolarBeam" }, { name = "FirePower" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Tide" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Absorb" }, { name = "LeafStorm" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Wave" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "SeedPower" }, { name = "LeechSeed" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "WaterPower" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Splash" }, { name = "Inferno" } }) },
	},
	by_stage = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "HeatWave" } }) }, -- BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "SoakPower" }, { name = "Flare" } }) }, -- BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "RazorLeaf" }, { name = "WaterPower" } }) }, -- BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Burn" } }) }, -- BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Inferno" }, { name = "SeedPower" } }) }, -- BASIC
		{ id = "MONSTER_010", moves = fields.moves({ { name = "Absorb" } }) }, -- BASIC
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Ember" }, { name = "LeechSeed" } }) }, -- BASIC
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Bubble" } }) }, -- BASIC

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Inferno" }, { name = "Tide" } }) }, -- STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Pollen" } }) }, -- STAGE_1
		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "Surf" } }) }, -- STAGE_1
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "LeafStorm" } }) }, -- STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FrenzyPlant" }, { name = "LeafStorm" } }) }, -- STAGE_2
	},
	from_current = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurFrenzy" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurEmber" }, { name = "CurSplash" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurSeedPower" }, { name = "CurSolar" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurInferno" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "CurHeat" }, { name = "CurWave" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "CurPetal" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "CurWaterPower" }, { name = "CurVine" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "CurPollen" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "CurInferno" }, { name = "CurStorm" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurSurf" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "CurFirePower" }, { name = "CurBurn" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "CurSeed" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "CurRazor" }, { name = "CurLeaf" } }) },
	},
	remove_duplicates = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Absorb" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "WaterPower" }, { name = "Bubble" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "SeedPower" }, { name = "Inferno" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Burn" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "LeafStorm" }, { name = "LeechSeed" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "SolarBeam" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "VineWhip" }, { name = "Pollen" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Wave" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "HeatWave" }, { name = "RazorLeaf" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Ember" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Flare" }, { name = "PetalDance" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FirePower" }, { name = "Splash" } }) },
	},
	fully_random = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Tide" }, { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "HeatWave" }, { name = "WaterPower" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Inferno" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "LeafStorm" }, { name = "Tide" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "Tide" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Wave" }, { name = "SolarBeam" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Tide" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "HeatWave" }, { name = "RazorLeaf" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Inferno" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "Wave" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "SolarBeam" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Splash" }, { name = "SoakPower" } }) },
	},
	attacks = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Pollen" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "RazorLeaf" }, { name = "CurFirePower" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Ember" }, { name = "PetalDance" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurWaterPower" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Surf" }, { name = "Inferno" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "VineWhip" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Splash" }, { name = "Bubble" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "LeechSeed" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Inferno" }, { name = "Burn" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurSeedPower" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Flare" }, { name = "Ember" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "HeatWave" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Wave" }, { name = "Absorb" } }) },
	},
	powers = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurEmber" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurInferno" }, { name = "WaterPower" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurSplash" }, { name = "CurWave" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "SeedPower" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "CurLeaf" }, { name = "CurSeed" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "CurBurn" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "CurHeat" }, { name = "CurInferno" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "CurSurf" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "CurVine" }, { name = "CurRazor" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "SoakPower" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "CurPetal" }, { name = "CurSolar" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "CurFrenzy" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "CurStorm" }, { name = "CurPollen" } }) },
	},
	within_type = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Inferno" } }) }, -- FIRE
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Ember" }, { name = "Inferno" } }) }, -- FIRE
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- FIRE
		{ id = "MONSTER_010", moves = fields.moves({ { name = "Ember" } }) }, -- FIRE
		{ id = "MONSTER_011", moves = fields.moves({ { name = "HeatWave" }, { name = "FirePower" } }) }, -- FIRE

		{ id = "MONSTER_004", moves = fields.moves({ { name = "Tide" }, { name = "Surf" } }) }, -- WATER
		{ id = "MONSTER_005", moves = fields.moves({ { name = "SoakPower" } }) }, -- WATER
		{ id = "MONSTER_012", moves = fields.moves() }, -- WATER
		{ id = "MONSTER_013", moves = fields.moves({ { name = "WaterPower" } }) }, -- WATER

		{ id = "MONSTER_006", moves = fields.moves({ { name = "SeedPower" }, { name = "LeechSeed" } }) }, -- GRASS
		{ id = "MONSTER_014", moves = fields.moves({ { name = "PetalDance" }, { name = "SolarBeam" } }) }, -- GRASS
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Pollen" } }) }, -- GRASS
		{ id = "MONSTER_016", moves = fields.moves({ { name = "VineWhip" }, { name = "RazorLeaf" } }) }, -- GRASS
		{ id = "MONSTER_017", moves = fields.moves() }, -- GRASS
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "LeafStorm" } }) }, -- GRASS
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Absorb" }, { name = "FrenzyPlant" } }) }, -- GRASS
	},
}

local function caseFor(name, overrides)
	local args = {}
	for key, value in pairs(baseline) do
		args[key] = value
	end
	for key, value in pairs(overrides) do
		args[key] = value
	end
	return {
		name = name,
		module = "randomize_moves",
		seed = seed,
		args = args,
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = expects[name],
	}
end

return {
	caseFor("all_together", {}),
	caseFor("by_stage", { grouping = "BY_STAGE" }),
	caseFor("from_current", { source = "CURRENT" }),
	caseFor("remove_duplicates", { duplicates = "REMOVE_DUPLICATES" }),
	caseFor("fully_random", { approach = "FULLY_RANDOM" }),
	caseFor("attacks", { moveKind = "ATTACKS" }),
	caseFor("powers", { moveKind = "POWERS" }),
	caseFor("within_type", { withinType = true }),
}
