-- Shared card sets for script tests. Not a test file. require("support.card_sets")
-- Only sets shared by multiple tests live here.
local card_sets = {}

-- Full deck used by shuffle / numMoves / moves / custom HP / fix_evo_line tests.
-- HP is a signed byte so stay <= 120, and only use multiples of 10.
-- Moves are name-only (plus POWER category where needed). numMoves matches move count.
-- Solo basics share an HP and a move name so KEEP vs REMOVE is visible.
card_sets.STD_TEST_CARDS_ROM = {
	-- Solo basics. BASIC/BASIC bucket.
	{ id = "MONSTER_001", name = "SoloOne", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 10, hp = 40, numMoves = 2,
		moves = { { name = "Ember" }, { name = "FirePower", category = "POWER" } } },
	{ id = "MONSTER_002", name = "SoloTwo", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 11, hp = 40, numMoves = 1,
		moves = { { name = "Ember" } } },
	{ id = "MONSTER_003_1", name = "SoloThree", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 12, hp = 50, numMoves = 2,
		moves = { { name = "Flare" }, { name = "Inferno" } } },
	{ id = "MONSTER_004", name = "SoloFour", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 13, hp = 60, numMoves = 1,
		moves = { { name = "Splash" } } },
	{ id = "MONSTER_005", name = "SoloFive", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 14, hp = 70, numMoves = 2,
		moves = { { name = "Wave" }, { name = "WaterPower", category = "POWER" } } },
	-- Needs moves so withinType max-stage has a GRASS BASIC/BASIC pool (CURRENT also has slots here).
	{ id = "MONSTER_006", name = "SoloSix", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 15, hp = 80, numMoves = 2,
		moves = { { name = "Absorb" }, { name = "LeechSeed" } } },

	-- Ordered on ROM. Inversion for fix_evo lives on CURRENT instead.
	{ id = "MONSTER_010", name = "InvertedBasic", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 1, hp = 40, numMoves = 2,
		moves = { { name = "HeatWave" }, { name = "Burn" } } },
	{ id = "MONSTER_011", name = "InvertedEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "InvertedBasic", evoLineId = 1, hp = 80, numMoves = 1,
		moves = { { name = "Inferno" } } },

	-- Ordered two-stager. Already increasing so fix should leave it alone.
	{ id = "MONSTER_012", name = "OrderedBasic", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 2, hp = 30, numMoves = 2,
		moves = { { name = "Bubble" }, { name = "SoakPower", category = "POWER" } } },
	{ id = "MONSTER_013", name = "OrderedEvo", type = "MONSTER_WATER", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "OrderedBasic", evoLineId = 2, hp = 50, numMoves = 2,
		moves = { { name = "Surf" }, { name = "Tide" } } },

	-- Split three-stager. Same-name reprints so fix works across duplicates.
	{ id = "MONSTER_014", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 90, numMoves = 1,
		moves = { { name = "VineWhip" } } },
	{ id = "MONSTER_015", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 40, numMoves = 2,
		moves = { { name = "RazorLeaf" }, { name = "SeedPower", category = "POWER" } } },
	{ id = "MONSTER_016", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 50, numMoves = 1,
		moves = { { name = "PetalDance" } } },
	{ id = "MONSTER_017", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 30, numMoves = 2,
		moves = { { name = "SolarBeam" }, { name = "Pollen" } } },
	{ id = "MONSTER_018_1", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 60, numMoves = 2,
		moves = { { name = "FrenzyPlant" }, { name = "LeafStorm" } } },
	{ id = "MONSTER_019", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 110, numMoves = 0,
		moves = {} },
}

-- Same cards with different hp / numMoves / move names so ROM vs CURRENT is visible.
card_sets.STD_TEST_CARDS_CURRENT = {
	{ id = "MONSTER_001", name = "SoloOne", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 10, hp = 10, numMoves = 1,
		moves = { { name = "CurEmber" } } },
	{ id = "MONSTER_002", name = "SoloTwo", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 11, hp = 20, numMoves = 2,
		moves = { { name = "CurInferno" }, { name = "CurFirePower", category = "POWER" } } },
	{ id = "MONSTER_003_1", name = "SoloThree", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 12, hp = 30, numMoves = 0,
		moves = {} },
	{ id = "MONSTER_004", name = "SoloFour", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 13, hp = 100, numMoves = 2,
		moves = { { name = "CurSplash" }, { name = "CurWave" } } },
	{ id = "MONSTER_005", name = "SoloFive", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 14, hp = 110, numMoves = 1,
		moves = { { name = "CurWaterPower", category = "POWER" } } },
	{ id = "MONSTER_006", name = "SoloSix", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "BASIC", evoLineId = 15, hp = 120, numMoves = 2,
		moves = { { name = "CurLeaf" }, { name = "CurSeed" } } },

	-- Inverted two-stager. Basic stronger than evo so fix_evo raise/redistribute both do something.
	{ id = "MONSTER_010", name = "InvertedBasic", type = "MONSTER_FIRE", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 1, hp = 80, numMoves = 1,
		moves = { { name = "CurBurn" } } },
	{ id = "MONSTER_011", name = "InvertedEvo", type = "MONSTER_FIRE", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "InvertedBasic", evoLineId = 1, hp = 40, numMoves = 2,
		moves = { { name = "CurHeat" }, { name = "CurInferno" } } },

	{ id = "MONSTER_012", name = "OrderedBasic", type = "MONSTER_WATER", stage = "BASIC",
		evoLineMaxStage = "STAGE_1", evoLineId = 2, hp = 50, numMoves = 0,
		moves = {} },
	{ id = "MONSTER_013", name = "OrderedEvo", type = "MONSTER_WATER", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_1", prevEvoName = "OrderedBasic", evoLineId = 2, hp = 60, numMoves = 1,
		moves = { { name = "CurSurf" } } },

	{ id = "MONSTER_014", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 20, numMoves = 2,
		moves = { { name = "CurVine" }, { name = "CurRazor" } } },
	{ id = "MONSTER_015", name = "SplitBasic", type = "MONSTER_GRASS", stage = "BASIC",
		evoLineMaxStage = "STAGE_2", evoLineId = 3, hp = 80, numMoves = 1,
		moves = { { name = "CurSeedPower", category = "POWER" } } },
	{ id = "MONSTER_016", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 40, numMoves = 2,
		moves = { { name = "CurPetal" }, { name = "CurSolar" } } },
	{ id = "MONSTER_017", name = "SplitEvo", type = "MONSTER_GRASS", stage = "STAGE_1",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitBasic", evoLineId = 3, hp = 100, numMoves = 0,
		moves = {} },
	{ id = "MONSTER_018_1", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 10, numMoves = 1,
		moves = { { name = "CurFrenzy" } } },
	{ id = "MONSTER_019", name = "SecondEvo", type = "MONSTER_GRASS", stage = "STAGE_2",
		evoLineMaxStage = "STAGE_2", prevEvoName = "SplitEvo", evoLineId = 3, hp = 30, numMoves = 2,
		moves = { { name = "CurStorm" }, { name = "CurPollen" } } },
}

-- Two cards with mixed typed costs for all_moves_* and log_monster_cards.
-- MONSTER_001 is FIRE and MONSTER_002 is COLORLESS so both conversion cases are covered.
card_sets.MIXED_COST_CARDS = {
	{
		id = "MONSTER_001", type = "MONSTER_FIRE", moves = {
			{ name = "Burn", costs = { FIRE = 2, COLORLESS = 1 } },
			-- can handle multi-types (not sure the game actually supports this)
			{ name = "Forest Fire", costs = { FIRE = 1, GRASS = 1, COLORLESS = 1 } },
		},
	},
	{
		id = "MONSTER_002", type = "MONSTER_COLORLESS", moves = {
			-- same shape as Forest Fire so non-colorless conversion is visible on a colorless card
			{ name = "Tackle", costs = { FIGHTING = 1, GRASS = 1, COLORLESS = 1 } },
			{ name = "Slam", costs = { FIGHTING = 2 } },
		},
	},
}

return card_sets
