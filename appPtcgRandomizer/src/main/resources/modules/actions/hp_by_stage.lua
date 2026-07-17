local randomizer = require("randomizer")

local module
module = {
	id = "hp_by_stage_from_rom",
	name = "HP by Stage from ROM",
	description = "Randomize HP by stage and evo-line max stage using ROM HP pools",
	groups = { "cards" },
	modifies = { "hp" },
	author = "Redacted Rice",
	version = "0.5",
	requires = {
		PtcgRandomizer = "0.2.0",
		set_evo_line_metadata = "0.9",
	},
	-- TODO later: Make this an arg or split scripts back out
	useRomPools = true,
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

-- Hashes card stage and max stage so each permutation has a different key
function module.stageAndMaxStageHash(stage, maxStage)
	return maxStage:getValue() * 10 + stage:getValue()
end

-- TODO later: Make this a config
function module.buildFixedHpPools()
	local function hash(stage, maxStage)
		return maxStage * 10 + stage
	end

	return randomizer.group({
		[hash(0, 0)] = { 50, 50, 60, 60, 60, 70, 70, 70, 80, 90, 100, 120 },
		[hash(0, 1)] = { 30, 40, 40, 50, 50, 60, 70, 80 },
		[hash(1, 1)] = { 50, 60, 70, 70, 80, 90, 100 },
		[hash(0, 2)] = { 30, 30, 40, 40, 50, 50, 60 },
		[hash(1, 2)] = { 50, 60, 60, 70, 70, 80, 90 },
		[hash(2, 2)] = { 80, 90, 100, 100, 110, 120 },
	})
end

-- Builds pools from the original rom for stage/maxStage permutations
function module.buildRomHpPools(monsterCards)
	return randomizer.groupFromField(monsterCards, function(mc)
		return module.stageAndMaxStageHash(mc.stage, mc.evoLineMaxStage)
	end, "getHp")
end

function module.buildHpPoolGroup(context)
	if module.useRomPools then
		return module.buildRomHpPools(context.original:getRandomizableMonsterCards())
	end
	return module.buildFixedHpPools()
end

function module.poolKeyForCard(mc)
	return module.stageAndMaxStageHash(mc.stage, mc.evoLineMaxStage)
end

-- Assign freely from the stage/maxStage pool. Users can run a variation of
-- fix_evo_line_hp after for consistency if desired
function module.randomizeHp(context)
	local hpPoolGroup = module.buildHpPoolGroup(context)

	hpPoolGroup:useToRandomize(context.modified:getRandomizableMonsterCards(),
        module.poolKeyForCard, "setHp", {
		    consumable = true,
	})
end

return module
