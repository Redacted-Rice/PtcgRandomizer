local seed = 42

-- Original: one proxy is basic of 1-3-2, another is lone basic.
local restoreOriginalCards = {
	{ id = "MONSTER_140", name = "Proxy3Basic", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 1, evoLineMaxStage = "STAGE_2", dexNumber = 0,
		evoBranchIds = { 1, 2, 3 }, isTrainerProxy = true },
	{ id = "MONSTER_141", name = "Proxy3S1A", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_2", dexNumber = 80,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_142", name = "Proxy3S2A", type = "MONSTER_FIGHTING", stage = "STAGE_2",
		prevEvoName = "Proxy3S1A", evoLineId = 1, evoLineMaxStage = "STAGE_2", dexNumber = 81,
		evoBranchIds = { 1 } },
	{ id = "MONSTER_143", name = "Proxy3S1B", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_2", dexNumber = 100,
		evoBranchIds = { 2 } },
	{ id = "MONSTER_144_1", name = "Proxy3S2B", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "Proxy3S1B", evoLineId = 1, evoLineMaxStage = "STAGE_2", dexNumber = 101,
		evoBranchIds = { 2 } },
	-- Multiple versions of the same card
	{ id = "MONSTER_145_1", name = "Proxy3S1C", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_1", dexNumber = 90,
		evoBranchIds = { 3 } },
	{ id = "MONSTER_146_1", name = "Proxy3S1C", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_1", dexNumber = 90,
		evoBranchIds = { 3 } },

	-- Two basic only proxies for testing cases
	{ id = "MONSTER_011", name = "ProxyBasic1", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 3, evoLineMaxStage = "BASIC", isTrainerProxy = true },
	{ id = "MONSTER_015", name = "ProxyBasic2", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 4, evoLineMaxStage = "BASIC", isTrainerProxy = true },
}

local restoreModifiedCards = {
	-- 1-3-2 line with the proxy swapped out
	{ id = "MONSTER_001", name = "NonProxy3Basic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 1, evoLineMaxStage = "STAGE_2",
		aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_141", name = "Proxy3S1A", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "NonProxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_2",
		aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_142", name = "Proxy3S2A", type = "MONSTER_FIGHTING", stage = "STAGE_2",
		prevEvoName = "Proxy3S1A", evoLineId = 1, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = {} },
	{ id = "MONSTER_143", name = "Proxy3S1B", type = "MONSTER_WATER", stage = "STAGE_1",
		prevEvoName = "NonProxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_144_1", name = "Proxy3S2B", type = "MONSTER_WATER", stage = "STAGE_2",
		prevEvoName = "Proxy3S1B", evoLineId = 1, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = {} },
	-- Two of the same name
	{ id = "MONSTER_145_1", name = "Proxy3S1C", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "NonProxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },
	{ id = "MONSTER_145_2", name = "Proxy3S1C", type = "MONSTER_FIGHTING", stage = "STAGE_1",
		prevEvoName = "NonProxy3Basic", evoLineId = 1, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },

	-- Basic only proxy in a 1-2-1 line
	{ id = "MONSTER_011", name = "ProxyBasic1", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 2, evoLineMaxStage = "STAGE_2", isTrainerProxy = true,
		aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_087", name = "Proxy2S1A", type = "MONSTER_COLORLESS", stage = "STAGE_1",
		prevEvoName = "ProxyBasic1", evoLineId = 2, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
	-- Two of the same name
	{ id = "MONSTER_088", name = "Proxy2S2A", type = "MONSTER_COLORLESS", stage = "STAGE_2",
		prevEvoName = "Proxy2S1A", evoLineId = 2, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = {} },
	{ id = "MONSTER_089", name = "Proxy2S2A", type = "MONSTER_COLORLESS", stage = "STAGE_2",
		prevEvoName = "Proxy2S1A", evoLineId = 2, evoLineMaxStage = "STAGE_2",
		aiInfo = "NONE", aiFlags = {} },
	-- Also two of the same name
	{ id = "MONSTER_110", name = "Proxy2S1B", type = "MONSTER_COLORLESS", stage = "STAGE_1",
		prevEvoName = "ProxyBasic1", evoLineId = 2, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },
	{ id = "MONSTER_111", name = "Proxy2S1B", type = "MONSTER_COLORLESS", stage = "STAGE_1",
		prevEvoName = "ProxyBasic1", evoLineId = 2, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },

	-- Some free basics for testing the swap with basic action
	{ id = "MONSTER_021", name = "NonProxyBasic1", type = "MONSTER_GRASS", stage = "BASIC",
		prevEvoName = "", evoLineId = 4, evoLineMaxStage = "BASIC",
		aiInfo = "NONE", aiFlags = {} },
	{ id = "MONSTER_027", name = "NonProxyBasic2", type = "MONSTER_GRASS", stage = "BASIC",
		prevEvoName = "", evoLineId = 5, evoLineMaxStage = "BASIC",
		aiInfo = "NONE", aiFlags = {} },

	-- Proxy that stayed basic only
	{ id = "MONSTER_015", name = "ProxyBasic2", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 6, evoLineMaxStage = "BASIC", isTrainerProxy = true,
		aiInfo = "NONE", aiFlags = {} },

	-- Proxy from 1-3-2 became part of a 1-1-0 line
	{ id = "MONSTER_140", name = "Proxy3Basic", type = "MONSTER_COLORLESS", stage = "BASIC",
		prevEvoName = "", evoLineId = 2, evoLineMaxStage = "STAGE_1", isTrainerProxy = true,
		aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_042", name = "AnotherStage1", type = "MONSTER_GRASS", stage = "STAGE_1",
		prevEvoName = "Proxy3Basic", evoLineId = 2, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },

	-- Just a normal line so we can see its not affected
	{ id = "MONSTER_004", name = "NonProxy2Basic", type = "MONSTER_FIRE", stage = "BASIC",
		prevEvoName = "", evoLineId = 7, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
	{ id = "MONSTER_005", name = "NonProxy2S1", type = "MONSTER_FIRE", stage = "STAGE_1",
		prevEvoName = "NonProxy2Basic", evoLineId = 7, evoLineMaxStage = "STAGE_1",
		aiInfo = "NONE", aiFlags = {} },
}

local function caseFor(name, args, original, modified, expect, caseSeed)
	return {
		name = name,
		module = "evo_line_playable_trainers",
		seed = caseSeed or seed,
		args = args,
		original = original,
		modified = modified or original,
		expect = expect,
	}
end

return {
	caseFor(
		"restore_swaps_onto_matching_shapes",
		{ action = "Restore Original Lines" },
		restoreOriginalCards,
		restoreModifiedCards,
		{
			-- Proxy3 swapped back into its original line (the only valid line for it)
			{ id = "MONSTER_140", name = "Proxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_141", name = "Proxy3S1A", stage = "STAGE_1", prevEvoName = "Proxy3Basic",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_142", name = "Proxy3S2A", stage = "STAGE_2", prevEvoName = "Proxy3S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_143", name = "Proxy3S1B", stage = "STAGE_1", prevEvoName = "Proxy3Basic",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_144_1", name = "Proxy3S2B", stage = "STAGE_2", prevEvoName = "Proxy3S1B",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_1", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "Proxy3Basic",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_2", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "Proxy3Basic",
				aiInfo = "NONE", aiFlags = {} },
			-- And the basic was swapped with the line proxy3 was in
			{ id = "MONSTER_001", name = "NonProxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_042", name = "AnotherStage1", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = {} },

			-- Proxy basic 1 swapped back to non-evolving
			{ id = "MONSTER_011", name = "ProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- And one of the free basics was swapped into its slot
			{ id = "MONSTER_027", name = "NonProxyBasic2", stage = "BASIC", prevEvoName = "",
			aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_087", name = "Proxy2S1A", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_088", name = "Proxy2S2A", stage = "STAGE_2", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_089", name = "Proxy2S2A", stage = "STAGE_2", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_110", name = "Proxy2S1B", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_111", name = "Proxy2S1B", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = {} },

			-- Other proxy an non-basic proxy remain solo as was
			{ id = "MONSTER_021", name = "NonProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_015", name = "ProxyBasic2", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- Non-proxy line is not modified either
			{ id = "MONSTER_004", name = "NonProxy2Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_005", name = "NonProxy2S1", stage = "STAGE_1", prevEvoName = "NonProxy2Basic",
				aiInfo = "NONE", aiFlags = {} },
		}
	),

	caseFor(
		"remove_from_lines_demotes_descendants",
		{ action = "Remove From Evo Lines" },
		restoreOriginalCards,
		restoreModifiedCards,
		{
			-- Non-proxy line not touches
			{ id = "MONSTER_001", name = "NonProxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_141", name = "Proxy3S1A", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_142", name = "Proxy3S2A", stage = "STAGE_2", prevEvoName = "Proxy3S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_143", name = "Proxy3S1B", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_144_1", name = "Proxy3S2B", stage = "STAGE_2", prevEvoName = "Proxy3S1B",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_1", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_2", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = {} },

			-- 1-2-1 line with proxy becomes solo proxy and a 1-1 and a solo line
			{ id = "MONSTER_011", name = "ProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- 1-1 line from the proxy demotion
			{ id = "MONSTER_087", name = "Proxy2S1A", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_088", name = "Proxy2S2A", stage = "STAGE_1", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_089", name = "Proxy2S2A", stage = "STAGE_1", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			-- Basic line from the proxy demotion
			{ id = "MONSTER_110", name = "Proxy2S1B", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_111", name = "Proxy2S1B", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },

			-- Non proxy basics untouched
			{ id = "MONSTER_021", name = "NonProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_027", name = "NonProxyBasic2", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },

			-- Other proxies go back to basic as well
			{ id = "MONSTER_015", name = "ProxyBasic2", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_140", name = "Proxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- And form a new basic only line
			{ id = "MONSTER_042", name = "AnotherStage1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- Another non-proxy line is untouched
			{ id = "MONSTER_004", name = "NonProxy2Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_005", name = "NonProxy2S1", stage = "STAGE_1", prevEvoName = "NonProxy2Basic",
				aiInfo = "NONE", aiFlags = {} },
		}
	),

	caseFor(
		"replace_with_basic_swaps_onto_lone_basic",
		{ action = "Replace With Basic" },
		restoreOriginalCards,
		restoreModifiedCards,
		{
			-- Non proxy line untouched
			{ id = "MONSTER_001", name = "NonProxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_141", name = "Proxy3S1A", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "ENCOURAGE_EVO", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_142", name = "Proxy3S2A", stage = "STAGE_2", prevEvoName = "Proxy3S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_143", name = "Proxy3S1B", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_144_1", name = "Proxy3S2B", stage = "STAGE_2", prevEvoName = "Proxy3S1B",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_1", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_145_2", name = "Proxy3S1C", stage = "STAGE_1", prevEvoName = "NonProxy3Basic",
				aiInfo = "NONE", aiFlags = {} },

			-- Proxy basic 1 swapped back to non-evolving
			{ id = "MONSTER_011", name = "ProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- And one of the free basics was swapped into its slot
			{ id = "MONSTER_027", name = "NonProxyBasic2", stage = "BASIC", prevEvoName = "",
			aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_087", name = "Proxy2S1A", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_088", name = "Proxy2S2A", stage = "STAGE_2", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_089", name = "Proxy2S2A", stage = "STAGE_2", prevEvoName = "Proxy2S1A",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_110", name = "Proxy2S1B", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = {} },
			{ id = "MONSTER_111", name = "Proxy2S1B", stage = "STAGE_1", prevEvoName = "NonProxyBasic2",
				aiInfo = "NONE", aiFlags = {} },

			-- Already basic only proxy is untouched
			{ id = "MONSTER_015", name = "ProxyBasic2", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },

			-- Another proxy was also swapped with the other free basic
			{ id = "MONSTER_140", name = "Proxy3Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = {} },
			-- Non proxy basic 1 swapped back to evolving
			{ id = "MONSTER_021", name = "NonProxyBasic1", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_042", name = "AnotherStage1", stage = "STAGE_1", prevEvoName = "NonProxyBasic1",
				aiInfo = "NONE", aiFlags = {} },

			-- Another non-proxy line is untouched
			{ id = "MONSTER_004", name = "NonProxy2Basic", stage = "BASIC", prevEvoName = "",
				aiInfo = "NONE", aiFlags = { "HAS_EVOLUTION" } },
			{ id = "MONSTER_005", name = "NonProxy2S1", stage = "STAGE_1", prevEvoName = "NonProxy2Basic",
				aiInfo = "NONE", aiFlags = {} },
		}
	),
}
