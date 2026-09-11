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
		{ id = "MONSTER_001", moves = fields.moves({ { name = "FirePower" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Wave" }, { name = "HeatWave" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Pollen" }, { name = "Ember" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Splash" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "SoakPower" }, { name = "WaterPower" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "LeafStorm" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Tide" }, { name = "VineWhip" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Inferno" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "LeechSeed" }, { name = "Bubble" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "SeedPower" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Inferno" }, { name = "PetalDance" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "Absorb" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Surf" }, { name = "Burn" } }) },
	},
	by_stage = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "HeatWave" } }) }, -- BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Ember" }, { name = "SeedPower" } }) }, -- BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Wave" }, { name = "Splash" } }) }, -- BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Ember" } }) }, -- BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Burn" }, { name = "WaterPower" } }) }, -- BASIC
		{ id = "MONSTER_010", moves = fields.moves({ { name = "LeechSeed" } }) }, -- BASIC
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Absorb" }, { name = "VineWhip" } }) }, -- BASIC
		{ id = "MONSTER_015", moves = fields.moves({ { name = "FirePower" } }) }, -- BASIC

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Inferno" }, { name = "Pollen" } }) }, -- STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "PetalDance" } }) }, -- STAGE_1
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Tide" }, { name = "SolarBeam" } }) }, -- STAGE_1
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "LeafStorm" } }) }, -- STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FrenzyPlant" }, { name = "FrenzyPlant" } }) }, -- STAGE_2
	},
	from_current = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurInferno" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurSeedPower" }, { name = "CurSeed" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurFrenzy" }, { name = "CurSolar" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurEmber" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "CurVine" }, { name = "CurStorm" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "CurSurf" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "CurBurn" }, { name = "CurLeaf" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "CurWave" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "CurPollen" }, { name = "CurWaterPower" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurFirePower" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "CurRazor" }, { name = "CurSplash" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "CurHeat" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "CurPetal" }, { name = "CurInferno" } }) },
	},
	remove_duplicates = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "WaterPower" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "FirePower" }, { name = "VineWhip" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Splash" }, { name = "Bubble" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Tide" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "PetalDance" }, { name = "Inferno" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "Absorb" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "SoakPower" }, { name = "HeatWave" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "RazorLeaf" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Pollen" }, { name = "SeedPower" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Surf" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Flare" }, { name = "SolarBeam" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "LeechSeed" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Ember" }, { name = "Wave" } }) },
	},
	fully_random = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "FirePower" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "SolarBeam" }, { name = "LeafStorm" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Flare" }, { name = "Tide" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "SoakPower" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Wave" }, { name = "VineWhip" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Absorb" }, { name = "LeafStorm" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "Surf" }, { name = "WaterPower" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "Splash" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "WaterPower" }, { name = "FrenzyPlant" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "Splash" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "Ember" }, { name = "Tide" } }) },
	},
	attacks = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Splash" }, { name = "CurFirePower" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "RazorLeaf" }, { name = "Bubble" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurWaterPower" } }) },
		{ id = "MONSTER_006", moves = fields.moves({ { name = "PetalDance" }, { name = "Tide" } }) },
		{ id = "MONSTER_010", moves = fields.moves({ { name = "VineWhip" } }) },
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Absorb" }, { name = "LeafStorm" } }) },
		{ id = "MONSTER_012", moves = fields.moves() },
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Pollen" } }) },
		{ id = "MONSTER_014", moves = fields.moves({ { name = "SolarBeam" }, { name = "Flare" } }) },
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurSeedPower" } }) },
		{ id = "MONSTER_016", moves = fields.moves({ { name = "Burn" }, { name = "Inferno" } }) },
		{ id = "MONSTER_017", moves = fields.moves() },
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "Inferno" } }) },
		{ id = "MONSTER_019", moves = fields.moves({ { name = "LeechSeed" }, { name = "Wave" } }) },
	},
	powers = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurEmber" } }) },
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurInferno" }, { name = "FirePower" } }) },
		{ id = "MONSTER_003_1", moves = fields.moves() },
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurSplash" }, { name = "CurWave" } }) },
		{ id = "MONSTER_005", moves = fields.moves({ { name = "WaterPower" } }) },
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
		{ id = "MONSTER_001", moves = fields.moves({ { name = "FirePower" } }) }, -- FIRE
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Inferno" }, { name = "Ember" } }) }, -- FIRE
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- FIRE
		{ id = "MONSTER_010", moves = fields.moves({ { name = "Burn" } }) }, -- FIRE
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Ember" }, { name = "Flare" } }) }, -- FIRE

		{ id = "MONSTER_004", moves = fields.moves({ { name = "Wave" }, { name = "Surf" } }) }, -- WATER
		{ id = "MONSTER_005", moves = fields.moves({ { name = "SoakPower" } }) }, -- WATER
		{ id = "MONSTER_012", moves = fields.moves() }, -- WATER
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Bubble" } }) }, -- WATER

		{ id = "MONSTER_006", moves = fields.moves({ { name = "Absorb" }, { name = "LeafStorm" } }) }, -- GRASS
		{ id = "MONSTER_014", moves = fields.moves({ { name = "PetalDance" }, { name = "RazorLeaf" } }) }, -- GRASS
		{ id = "MONSTER_015", moves = fields.moves({ { name = "SolarBeam" } }) }, -- GRASS
		{ id = "MONSTER_016", moves = fields.moves({ { name = "LeechSeed" }, { name = "Pollen" } }) }, -- GRASS
		{ id = "MONSTER_017", moves = fields.moves() }, -- GRASS
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "VineWhip" } }) }, -- GRASS
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FrenzyPlant" }, { name = "SeedPower" } }) }, -- GRASS
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
		module = "moves_together_and_stage",
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
