-- Use the same seed on every case
local seed = 42
local card_sets = require("support.card_sets")
local fields = require("support.fields")

local baseline = {
	source = "ROM",
	duplicates = "KEEP_DUPLICATES",
	approach = "MINIMIZE_REPEATS",
	moveKind = "ALL_MOVES",
	withinType = false,
}

local expects = {
	-- Note that these use the num moves from CURRENT not source
	keep_duplicates = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Absorb" }, { name = "WaterPower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Inferno" }, { name = "Ember" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Wave" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Splash" }, { name = "LeechSeed" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "HeatWave" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Surf" }, { name = "Inferno" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Tide" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "SeedPower" }, { name = "VineWhip" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "RazorLeaf" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "SolarBeam" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "FrenzyPlant" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "LeafStorm" }, { name = "LeafStorm" } }) }, -- STAGE_2/STAGE_2
	},
	from_current = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurWaterPower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurLeaf" }, { name = "CurFirePower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurSeed" }, { name = "CurSplash" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurInferno" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "CurWave" }, { name = "CurEmber" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "CurBurn" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "CurInferno" }, { name = "CurHeat" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "CurSurf" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "CurSeedPower" }, { name = "CurVine" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurRazor" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "CurSolar" }, { name = "CurPetal" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "CurStorm" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "CurFrenzy" }, { name = "CurPollen" } }) }, -- STAGE_2/STAGE_2
	},
	remove_duplicates = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Inferno" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "WaterPower" }, { name = "FirePower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "LeechSeed" }, { name = "Ember" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Splash" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Flare" }, { name = "Absorb" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "HeatWave" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Surf" }, { name = "Inferno" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Tide" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "SeedPower" }, { name = "VineWhip" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "RazorLeaf" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "SolarBeam" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "FrenzyPlant" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "LeafStorm" }, { name = "LeafStorm" } }) }, -- STAGE_2/STAGE_2
	},
	fully_random = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Flare" }, { name = "Ember" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "LeechSeed" }, { name = "Splash" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Absorb" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "LeechSeed" }, { name = "Wave" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "HeatWave" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Surf" }, { name = "Tide" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Inferno" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "SeedPower" }, { name = "RazorLeaf" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "SeedPower" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "Pollen" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "FrenzyPlant" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FrenzyPlant" }, { name = "LeafStorm" } }) }, -- STAGE_2/STAGE_2
	},
	attacks = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Wave" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Absorb" }, { name = "CurFirePower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "Splash" }, { name = "Inferno" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "CurWaterPower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "Ember" }, { name = "Flare" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "Bubble" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "Tide" }, { name = "Surf" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Inferno" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "VineWhip" }, { name = "RazorLeaf" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "CurSeedPower" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "SolarBeam" }, { name = "Pollen" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "LeafStorm" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "FrenzyPlant" }, { name = "FrenzyPlant" } }) }, -- STAGE_2/STAGE_2
	},
	powers = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "CurEmber" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "CurInferno" }, { name = "WaterPower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "CurSplash" }, { name = "CurWave" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "FirePower" } }) }, -- BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "CurLeaf" }, { name = "CurSeed" } }) }, -- BASIC/BASIC

		{ id = "MONSTER_010", moves = fields.moves({ { name = "CurBurn" } }) }, -- BASIC/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- BASIC/STAGE_1

		{ id = "MONSTER_011", moves = fields.moves({ { name = "CurHeat" }, { name = "CurInferno" } }) }, -- STAGE_1/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "CurSurf" } }) }, -- STAGE_1/STAGE_1

		{ id = "MONSTER_014", moves = fields.moves({ { name = "CurVine" }, { name = "CurRazor" } }) }, -- BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "SeedPower" } }) }, -- BASIC/STAGE_2

		{ id = "MONSTER_016", moves = fields.moves({ { name = "CurPetal" }, { name = "CurSolar" } }) }, -- STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- STAGE_1/STAGE_2

		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "CurFrenzy" } }) }, -- STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "CurStorm" }, { name = "CurPollen" } }) }, -- STAGE_2/STAGE_2
	},
	within_type = {
		{ id = "MONSTER_001", moves = fields.moves({ { name = "Ember" } }) }, -- FIRE BASIC/BASIC
		{ id = "MONSTER_002", moves = fields.moves({ { name = "Flare" }, { name = "Inferno" } }) }, -- FIRE BASIC/BASIC
		{ id = "MONSTER_003_1", moves = fields.moves() }, -- FIRE BASIC/BASIC
		{ id = "MONSTER_004", moves = fields.moves({ { name = "WaterPower" }, { name = "Wave" } }) }, -- WATER BASIC/BASIC
		{ id = "MONSTER_005", moves = fields.moves({ { name = "Splash" } }) }, -- WATER BASIC/BASIC
		{ id = "MONSTER_006", moves = fields.moves({ { name = "LeechSeed" }, { name = "Absorb" } }) }, -- GRASS BASIC/BASIC
		{ id = "MONSTER_010", moves = fields.moves({ { name = "HeatWave" } }) }, -- FIRE BASIC/STAGE_1
		{ id = "MONSTER_011", moves = fields.moves({ { name = "Inferno" }, { name = "Inferno" } }) }, -- FIRE STAGE_1/STAGE_1
		{ id = "MONSTER_012", moves = fields.moves() }, -- WATER BASIC/STAGE_1
		{ id = "MONSTER_013", moves = fields.moves({ { name = "Surf" } }) }, -- WATER STAGE_1/STAGE_1
		{ id = "MONSTER_014", moves = fields.moves({ { name = "SeedPower" }, { name = "VineWhip" } }) }, -- GRASS BASIC/STAGE_2
		{ id = "MONSTER_015", moves = fields.moves({ { name = "RazorLeaf" } }) }, -- GRASS BASIC/STAGE_2
		{ id = "MONSTER_016", moves = fields.moves({ { name = "PetalDance" }, { name = "SolarBeam" } }) }, -- GRASS STAGE_1/STAGE_2
		{ id = "MONSTER_017", moves = fields.moves() }, -- GRASS STAGE_1/STAGE_2
		{ id = "MONSTER_018_1", moves = fields.moves({ { name = "FrenzyPlant" } }) }, -- GRASS STAGE_2/STAGE_2
		{ id = "MONSTER_019", moves = fields.moves({ { name = "LeafStorm" }, { name = "LeafStorm" } }) }, -- GRASS STAGE_2/STAGE_2
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
		module = "moves_stage_max_stage",
		seed = seed,
		args = args,
		original = card_sets.STD_TEST_CARDS_ROM,
		modified = card_sets.STD_TEST_CARDS_CURRENT,
		expect = expects[name],
	}
end

return {
	caseFor("keep_duplicates", {}),
	caseFor("from_current", { source = "CURRENT" }),
	caseFor("remove_duplicates", { duplicates = "REMOVE_DUPLICATES" }),
	caseFor("fully_random", { approach = "FULLY_RANDOM" }),
	caseFor("attacks", { moveKind = "ATTACKS" }),
	caseFor("powers", { moveKind = "POWERS" }),
	caseFor("within_type", { withinType = true }),
}
