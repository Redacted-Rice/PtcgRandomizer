-- Use the same seed on every case
local seed = 42

-- Mixed stages, and 40 shows up twice so KEEP vs REMOVE is visible.
local mixedOriginal = {
	{ name = "BasicA", hp = 40, stage = "BASIC" },
	{ name = "BasicB", hp = 90, stage = "BASIC" },
	{ name = "Stage1A", hp = 40, stage = "STAGE_1" },
	{ name = "Stage1B", hp = 120, stage = "STAGE_1" },
	{ name = "Stage2A", hp = 50, stage = "STAGE_2" },
	{ name = "Stage2B", hp = 100, stage = "STAGE_2" },
}

-- Same cards but different values so we can tell ROM from CURRENT.
local mixedModified = {
	{ name = "BasicA", hp = 10, stage = "BASIC" },
	{ name = "BasicB", hp = 20, stage = "BASIC" },
	{ name = "Stage1A", hp = 30, stage = "STAGE_1" },
	{ name = "Stage1B", hp = 60, stage = "STAGE_1" },
	{ name = "Stage2A", hp = 70, stage = "STAGE_2" },
	{ name = "Stage2B", hp = 80, stage = "STAGE_2" },
}

return {
	{
		name = "all_together",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = mixedOriginal,
		modified = mixedModified,
		-- Two 40s kept. Stage1A got a stage 2 HP, Stage2B a basic with the choosen seed
		expect = {
			{ name = "BasicA", hp = 40 },
			{ name = "BasicB", hp = 40 },
			{ name = "Stage1A", hp = 100 },
			{ name = "Stage1B", hp = 120 },
			{ name = "Stage2A", hp = 50 },
			{ name = "Stage2B", hp = 90 },
		},
	},
	{
		name = "by_stage",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "BY_STAGE",
		},
		original = mixedOriginal,
		modified = mixedModified,
		-- Each card stayed in its own stage's HP pool.
		expect = {
			{ name = "BasicA", hp = 40 },
			{ name = "BasicB", hp = 90 },
			{ name = "Stage1A", hp = 120 },
			{ name = "Stage1B", hp = 40 },
			{ name = "Stage2A", hp = 100 },
			{ name = "Stage2B", hp = 50 },
		},
	},
	{
		name = "from_current",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "CURRENT",
			duplicates = "KEEP_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = mixedOriginal,
		modified = mixedModified,
		-- Values from modified not original
		expect = {
			{ name = "BasicA", hp = 30 },
			{ name = "BasicB", hp = 10 },
			{ name = "Stage1A", hp = 80 },
			{ name = "Stage1B", hp = 60 },
			{ name = "Stage2A", hp = 70 },
			{ name = "Stage2B", hp = 20 },
		},
	},
	{
		name = "remove_duplicates",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "REMOVE_DUPLICATES",
			approach = "MINIMIZE_REPEATS",
			grouping = "ALL_TOGETHER",
		},
		original = mixedOriginal,
		modified = mixedModified,
		-- Only one 40. The extra slot refilled with 50 with this seed
		expect = {
			{ name = "BasicA", hp = 40 },
			{ name = "BasicB", hp = 50 },
			{ name = "Stage1A", hp = 90 },
			{ name = "Stage1B", hp = 100 },
			{ name = "Stage2A", hp = 120 },
			{ name = "Stage2B", hp = 50 },
		},
	},
	{
		name = "fully_random",
		module = "shuffle_hp",
		seed = seed,
		args = {
			source = "ROM",
			duplicates = "KEEP_DUPLICATES",
			approach = "FULLY_RANDOM",
			grouping = "ALL_TOGETHER",
		},
		original = mixedOriginal,
		modified = mixedModified,
		-- 40 landed three times and 100 was unused showing its truly random and
		-- and not pulling all values first
		expect = {
			{ name = "BasicA", hp = 40 },
			{ name = "BasicB", hp = 120 },
			{ name = "Stage1A", hp = 40 },
			{ name = "Stage1B", hp = 90 },
			{ name = "Stage2A", hp = 40 },
			{ name = "Stage2B", hp = 50 },
		},
	},
}
