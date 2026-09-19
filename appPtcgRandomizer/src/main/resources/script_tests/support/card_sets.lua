-- Shared card sets for script tests. Not a test file. require("support.card_sets")
-- Only sets shared by multiple tests live here.
local fields = require("support.fields")
local card_sets = {}
-- Full deck used by shuffle / numMoves / moves / custom HP / fix_evo_line tests.
-- HP is a signed byte so stay <= 120, and only use multiples of 10.
-- dexNumber uses 1-151. ROM is sequential within each evo line. CURRENT has inversions and gaps.
-- retreatCost is ROM range 0-3.
-- Moves are name-only (plus POWER category where needed). numMoves matches move count.
-- Solo basics share an HP and a move name so KEEP vs REMOVE is visible.
card_sets.STD_TEST_CARDS_ROM = {
	-- Solo basics. BASIC/BASIC bucket.
	{ id = "MONSTER_001", name = "SoloOne", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 10, hp = 40, dexNumber = 1, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "Ember" }, { name = "FirePower", category = "POWER" } }) },
	{ id = "MONSTER_002", name = "SoloTwo", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 11, hp = 40, dexNumber = 2, retreatCost = 1, numMoves = 1,
		moves = fields.moves({ { name = "Ember" } }) },
	{ id = "MONSTER_003_1", name = "SoloThree", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 12, hp = 50, dexNumber = 3, retreatCost = 2, numMoves = 2,
		moves = fields.moves({ { name = "Flare" }, { name = "Inferno" } }) },
	{ id = "MONSTER_004", name = "SoloFour", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 13, hp = 60, dexNumber = 4, retreatCost = 2, numMoves = 1,
		moves = fields.moves({ { name = "Splash" } }) },
	{ id = "MONSTER_005", name = "SoloFive", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 14, hp = 70, dexNumber = 5, retreatCost = 3, numMoves = 2,
		moves = fields.moves({ { name = "Wave" }, { name = "WaterPower", category = "POWER" } }) },
	-- Needs moves so withinType max-stage has a GRASS BASIC/BASIC pool (CURRENT also has slots here).
	{ id = "MONSTER_006", name = "SoloSix", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 15, hp = 80, dexNumber = 6, retreatCost = 3, numMoves = 2,
		moves = fields.moves({ { name = "Absorb" }, { name = "LeechSeed" } }) },

	-- Ordered on ROM. Inversion for fix_evo lives on CURRENT instead.
	{ id = "MONSTER_010", name = "InvertedBasic", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 1, hp = 40, dexNumber = 25, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "HeatWave" }, { name = "Burn" } }) },
	{ id = "MONSTER_011", name = "InvertedEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "InvertedBasic", evoLineId = 1, hp = 80, dexNumber = 26, retreatCost = 3, numMoves = 1,
		moves = fields.moves({ { name = "Inferno" } }) },

	-- Ordered two-stager. Already increasing so fix should leave it alone.
	{ id = "MONSTER_012", name = "OrderedBasic", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 2, hp = 30, dexNumber = 60, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "Bubble" }, { name = "SoakPower", category = "POWER" } }) },
	{ id = "MONSTER_013", name = "OrderedEvo", type = "MONSTER_WATER", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "OrderedBasic", evoLineId = 2, hp = 50, dexNumber = 61, retreatCost = 2, numMoves = 2,
		moves = fields.moves({ { name = "Surf" }, { name = "Tide" } }) },

	-- Split three-stager. Same-name reprints so fix works across duplicates.
	{ id = "MONSTER_014", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 90, dexNumber = 148, retreatCost = 3, numMoves = 1,
		moves = fields.moves({ { name = "VineWhip" } }) },
	{ id = "MONSTER_015", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 40, dexNumber = 148, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "RazorLeaf" }, { name = "SeedPower", category = "POWER" } }) },
	{ id = "MONSTER_016", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 50, dexNumber = 149, retreatCost = 2, numMoves = 1,
		moves = fields.moves({ { name = "PetalDance" } }) },
	{ id = "MONSTER_017", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 30, dexNumber = 149, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "SolarBeam" }, { name = "Pollen" } }) },
	{ id = "MONSTER_018_1", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 60, dexNumber = 150, retreatCost = 2, numMoves = 2,
		moves = fields.moves({ { name = "FrenzyPlant" }, { name = "LeafStorm" } }) },
	{ id = "MONSTER_019", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 110, dexNumber = 150, retreatCost = 3, numMoves = 0,
		moves = fields.moves() },
}

-- Same cards with different hp / numMoves / move names so ROM vs CURRENT is visible.
card_sets.STD_TEST_CARDS_CURRENT = {
	{ id = "MONSTER_001", name = "SoloOne", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 10, hp = 10, dexNumber = 7, retreatCost = 0, numMoves = 1,
		moves = fields.moves({ { name = "CurEmber" } }) },
	{ id = "MONSTER_002", name = "SoloTwo", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 11, hp = 20, dexNumber = 22, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "CurInferno" }, { name = "CurFirePower", category = "POWER" } }) },
	{ id = "MONSTER_003_1", name = "SoloThree", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 12, hp = 30, dexNumber = 38, retreatCost = 1, numMoves = 0,
		moves = fields.moves() },
	{ id = "MONSTER_004", name = "SoloFour", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 13, hp = 100, dexNumber = 72, retreatCost = 2, numMoves = 2,
		moves = fields.moves({ { name = "CurSplash" }, { name = "CurWave" } }) },
	{ id = "MONSTER_005", name = "SoloFive", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 14, hp = 110, dexNumber = 89, retreatCost = 1, numMoves = 1,
		moves = fields.moves({ { name = "CurWaterPower", category = "POWER" } }) },
	{ id = "MONSTER_006", name = "SoloSix", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 15, hp = 120, dexNumber = 127, retreatCost = 3, numMoves = 2,
		moves = fields.moves({ { name = "CurLeaf" }, { name = "CurSeed" } }) },

	-- Inverted two-stager. Basic stronger than evo so fix_evo raise/redistribute both do something.
	{ id = "MONSTER_010", name = "InvertedBasic", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 1, hp = 80, dexNumber = 105, retreatCost = 3, numMoves = 1,
		moves = fields.moves({ { name = "CurBurn" } }) },
	{ id = "MONSTER_011", name = "InvertedEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "InvertedBasic", evoLineId = 1, hp = 40, dexNumber = 95, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "CurHeat" }, { name = "CurInferno" } }) },

	{ id = "MONSTER_012", name = "OrderedBasic", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 2, hp = 50, dexNumber = 50, retreatCost = 1, numMoves = 0,
		moves = fields.moves() },
	{ id = "MONSTER_013", name = "OrderedEvo", type = "MONSTER_WATER", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "OrderedBasic", evoLineId = 2, hp = 60, dexNumber = 80, retreatCost = 2, numMoves = 1,
		moves = fields.moves({ { name = "CurSurf" } }) },

	{ id = "MONSTER_014", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 20, dexNumber = 60, retreatCost = 0, numMoves = 2,
		moves = fields.moves({ { name = "CurVine" }, { name = "CurRazor" } }) },
	{ id = "MONSTER_015", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 80, dexNumber = 120, retreatCost = 3, numMoves = 1,
		moves = fields.moves({ { name = "CurSeedPower", category = "POWER" } }) },
	{ id = "MONSTER_016", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 40, dexNumber = 100, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "CurPetal" }, { name = "CurSolar" } }) },
	{ id = "MONSTER_017", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 100, dexNumber = 70, retreatCost = 3, numMoves = 0,
		moves = fields.moves() },
	{ id = "MONSTER_018_1", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 10, dexNumber = 151, retreatCost = 3, numMoves = 1,
		moves = fields.moves({ { name = "CurFrenzy" } }) },
	{ id = "MONSTER_019", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 30, dexNumber = 55, retreatCost = 1, numMoves = 2,
		moves = fields.moves({ { name = "CurStorm" }, { name = "CurPollen" } }) },
}

-- Two cards with mixed typed costs for all_moves_* and log_monster_cards.
-- MONSTER_001 is FIRE and MONSTER_002 is COLORLESS so both conversion cases are covered.
-- Fire/water deck for evo line card modules. Duplicate ids at basic, 2nd, and 3rd stages.
card_sets.EVO_LINE_CARDS_ROM = {
	{ id = "MONSTER_003_1", name = "FireABasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_003_2", name = "FireABasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_038_1", name = "FireA2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireABasic", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_038_2", name = "FireA2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireABasic", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_059_1", name = "FireA3rd", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "FireA2nd", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_059_2", name = "FireA3rd", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "FireA2nd", evoLineId = 1, evoLineMaxStage = "STAGE_2" },

	{ id = "MONSTER_025_1", name = "FireBBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_025_2", name = "FireBBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_025_3", name = "FireBBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_026_1", name = "FireB2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireBBasic", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_026_2", name = "FireB2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireBBasic", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_081_1", name = "FireB3rd", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "FireB2nd", evoLineId = 2, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_081_2", name = "FireB3rd", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "FireB2nd", evoLineId = 2, evoLineMaxStage = "STAGE_2" },

	{ id = "MONSTER_092_1", name = "FireCBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 3, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_092_2", name = "FireCBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 3, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_093_1", name = "FireC2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireCBasic", evoLineId = 3, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_093_2", name = "FireC2nd", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "FireCBasic", evoLineId = 3, evoLineMaxStage = "STAGE_1" },

	{ id = "MONSTER_080", name = "FireDBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 7, evoLineMaxStage = "BASIC" },

	{ id = "MONSTER_018_1", name = "FireEBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 9, evoLineMaxStage = "BASIC" },
	{ id = "MONSTER_018_2", name = "FireEBasic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 9, evoLineMaxStage = "BASIC" },

	{ id = "MONSTER_039_1", name = "WaterABasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_039_2", name = "WaterABasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_039_3", name = "WaterABasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_052_1", name = "WaterA2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterABasic", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_052_2", name = "WaterA2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterABasic", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_149_1", name = "WaterA3rd", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "WaterA2nd", evoLineId = 4, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_149_2", name = "WaterA3rd", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "WaterA2nd", evoLineId = 4, evoLineMaxStage = "STAGE_2" },

	{ id = "MONSTER_135_1", name = "WaterBBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 5, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_135_2", name = "WaterBBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 5, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_145_1", name = "WaterB2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterBBasic", evoLineId = 5, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_145_2", name = "WaterB2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterBBasic", evoLineId = 5, evoLineMaxStage = "STAGE_1" },

	{ id = "MONSTER_151_1", name = "WaterCBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_151_2", name = "WaterCBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_151_3", name = "WaterCBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_105_1", name = "WaterC2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterCBasic", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_105_2", name = "WaterC2nd", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "WaterCBasic", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_079_1", name = "WaterC3rd", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "WaterC2nd", evoLineId = 6, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_079_2", name = "WaterC3rd", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "WaterC2nd", evoLineId = 6, evoLineMaxStage = "STAGE_2" },

	{ id = "MONSTER_094", name = "WaterDBasic", type = "MONSTER_WATER", stage = "BASIC",
		prevEvoName = "", evoLineId = 8, evoLineMaxStage = "BASIC" },
}

-- Grass and fighting only. Smaller deck with one eevee-style branch line.
card_sets.EVO_LINE_CARDS_CURRENT = {
	{ id = "MONSTER_001", name = "GrassABasic", type = "MONSTER_GRASS", stage = "BASIC",
		prevEvoName = "", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_002", name = "GrassA2nd", type = "MONSTER_GRASS", stage = "STAGE_1",
		prevEvoName = "GrassABasic", evoLineId = 1, evoLineMaxStage = "STAGE_2" },
	{ id = "MONSTER_003_1", name = "GrassA3rd", type = "MONSTER_GRASS", stage = "STAGE_2",
		prevEvoName = "GrassA2nd", evoLineId = 1, evoLineMaxStage = "STAGE_2" },

	{ id = "MONSTER_004", name = "GrassBBasic", type = "MONSTER_GRASS", stage = "BASIC",
		prevEvoName = "", evoLineId = 3, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_005", name = "GrassB2nd", type = "MONSTER_GRASS", stage = "STAGE_1",
		prevEvoName = "GrassBBasic", evoLineId = 3, evoLineMaxStage = "STAGE_1" },

	{ id = "MONSTER_007", name = "GrassCBasic", type = "MONSTER_GRASS", stage = "BASIC",
		prevEvoName = "", evoLineId = 7, evoLineMaxStage = "BASIC" },

	{ id = "MONSTER_080", name = "FightABasic", type = "MONSTER_FIGHTING", stage = "BASIC",
		prevEvoName = "", evoLineId = 5, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_081_1", name = "FightABranchA", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "FightABasic", evoLineId = 5, evoLineMaxStage = "STAGE_1" },
	{ id = "MONSTER_081_2", name = "FightABranchB", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "FightABasic", evoLineId = 5, evoLineMaxStage = "STAGE_1" },

	{ id = "MONSTER_012", name = "FightBBasic", type = "MONSTER_FIGHTING", stage = "BASIC",
		prevEvoName = "", evoLineId = 8, evoLineMaxStage = "BASIC" },
}

-- Branching evo line shapes (1->3, 2->3, 3->2). Isolated evoLineIds so pools dont mix.
-- evoBranchIds match evo_line_metadata_set (tips first; shared ancestors hold all branch ids).
-- dexNumber seeds line ordering only (lowest basic dex).
card_sets.EVO_BRANCH_1_TO_3 = {
	{ id = "MONSTER_100", name = "Branch1Basic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 100, evoLineMaxStage = "STAGE_1", dexNumber = 10,
		evoBranchIds = { 1, 2, 3 } },
	{ id = "MONSTER_101_1", name = "Branch1S1A", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch1Basic", evoLineId = 100, evoLineMaxStage = "STAGE_1", dexNumber = 20,
		evoBranchIds = { 3 } },
	{ id = "MONSTER_102", name = "Branch1S1B", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch1Basic", evoLineId = 100, evoLineMaxStage = "STAGE_1", dexNumber = 30,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_103", name = "Branch1S1C", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch1Basic", evoLineId = 100, evoLineMaxStage = "STAGE_1", dexNumber = 40,
		evoBranchIds = { 2 } },
}

card_sets.EVO_BRANCH_2_TO_3 = {
	{ id = "MONSTER_116", name = "Branch2BasicA", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 101, evoLineMaxStage = "STAGE_1", dexNumber = 10,
		evoBranchIds = { 1, 2, 3 } },
	{ id = "MONSTER_117", name = "Branch2BasicB", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 101, evoLineMaxStage = "STAGE_1", dexNumber = 15,
		evoBranchIds = { 4 } },
	{ id = "MONSTER_118", name = "Branch2S1A", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch2BasicA", evoLineId = 101, evoLineMaxStage = "STAGE_1", dexNumber = 20,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_119", name = "Branch2S1B", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch2BasicA", evoLineId = 101, evoLineMaxStage = "STAGE_1", dexNumber = 30,
		evoBranchIds = { 2 } },
	{ id = "MONSTER_120", name = "Branch2S1C", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch2BasicA", evoLineId = 101, evoLineMaxStage = "STAGE_1", dexNumber = 40,
		evoBranchIds = { 3 } },
}

-- 1 basic -> 3 stage 1 -> 2 stage 2 (S1C is a tip with no stage 2).
card_sets.EVO_BRANCH_3_TO_2 = {
	{ id = "MONSTER_121", name = "Branch3Basic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 102, evoLineMaxStage = "STAGE_2", dexNumber = 50,
		evoBranchIds = { 1, 2, 3 } },
	{ id = "MONSTER_129", name = "Branch3S1A", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch3Basic", evoLineId = 102, evoLineMaxStage = "STAGE_2", dexNumber = 80,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_130", name = "Branch3S1B", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch3Basic", evoLineId = 102, evoLineMaxStage = "STAGE_2", dexNumber = 90,
		evoBranchIds = { 2 } },
	{ id = "MONSTER_131", name = "Branch3S1C", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "Branch3Basic", evoLineId = 102, evoLineMaxStage = "STAGE_1", dexNumber = 100,
		evoBranchIds = { 3 } },
	{ id = "MONSTER_138", name = "Branch3S2A", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "Branch3S1A", evoLineId = 102, evoLineMaxStage = "STAGE_2", dexNumber = 81,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_139", name = "Branch3S2B", type = "MONSTER_FIRE", stage = "STAGE_2",
		prevEvoName = "Branch3S1B", evoLineId = 102, evoLineMaxStage = "STAGE_2", dexNumber = 91,
		evoBranchIds = { 2 } },
}

-- Same 3->2 shape with a trainer proxy as the basic (Mysterious Fossil / Clefairy Doll style).
-- Proxy is structural only; S1A/S2A share branch 1 and must get consecutive dex numbers.
card_sets.EVO_BRANCH_PROXY_3_TO_2 = {
	{ id = "MONSTER_148", name = "Proxy3Basic", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 103, evoLineMaxStage = "STAGE_2", dexNumber = 0,
		evoBranchIds = { 1, 2, 3 }, isTrainerProxy = true },
	{ id = "MONSTER_140", name = "Proxy3S1A", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 103, evoLineMaxStage = "STAGE_2", dexNumber = 80,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_141", name = "Proxy3S1B", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 103, evoLineMaxStage = "STAGE_1", dexNumber = 90,
		evoBranchIds = { 3 } },
	{ id = "MONSTER_142", name = "Proxy3S1C", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 103, evoLineMaxStage = "STAGE_2", dexNumber = 100,
		evoBranchIds = { 2 } },
	{ id = "MONSTER_143", name = "Proxy3S2A", type = "MONSTER_FIGHTING", stage = "STAGE_2",
		prevEvoName = "Proxy3S1A", evoLineId = 103, evoLineMaxStage = "STAGE_2", dexNumber = 81,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_147", name = "Proxy3S2B", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "Proxy3S1C", evoLineId = 103, evoLineMaxStage = "STAGE_2", dexNumber = 101,
		evoBranchIds = { 2 } },
}

card_sets.MIXED_COST_CARDS = {
	{
		id = "MONSTER_001", type = "MONSTER_FIRE", moves = fields.moves({
			{ name = "Burn", costs = fields.costs({ FIRE = 2, COLORLESS = 1 }) },
			-- can handle multi-types (not sure the game actually supports this)
			{ name = "Forest Fire", costs = fields.costs({ FIRE = 1, GRASS = 1, COLORLESS = 1 }) },
		}),
	},
	{
		id = "MONSTER_002", type = "MONSTER_COLORLESS", moves = fields.moves({
			-- same shape as Forest Fire so non-colorless conversion is visible on a colorless card
			{ name = "Tackle", costs = fields.costs({ FIGHTING = 1, GRASS = 1, COLORLESS = 1 }) },
			{ name = "Slam", costs = fields.costs({ FIGHTING = 2 }) },
		}),
	},
}

return card_sets
