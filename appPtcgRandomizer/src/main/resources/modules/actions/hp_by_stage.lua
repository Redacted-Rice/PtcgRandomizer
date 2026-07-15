local randomizer = require("randomizer")

local module = {
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
		return module.buildRomHpPools(context.original:getMonsterCards())
	end
	return module.buildFixedHpPools()
end

function module.poolKeyForCard(mc)
	return module.stageAndMaxStageHash(mc.stage, mc.evoLineMaxStage)
end

-- Assign freely from the stage/maxStage pool, then enforce non-decreasing HP up each evo line
function module.randomizeHp(context)
	local monsterCards = context.modified:getMonsterCards()
	local hpPoolGroup = module.buildHpPoolGroup(context)

	hpPoolGroup:useToRandomize(monsterCards, module.poolKeyForCard, "setHp", {
		consumable = true,
	})

    -- TODO later: Move this into a separate module for fixing the HP in lines
	randomizer.groupBy(monsterCards, "evoLineId"):each(function(_, line)
		-- Sort in place so setHp mutates the real wrappers (List:sort deep-copies)
		table.sort(line.items, function(a, b)
			return a.stage:getValue() < b.stage:getValue()
		end)

		local prevHp = 0
		line:each(function(mc)
			if mc:getHp() < prevHp then
				mc:setHp(prevHp)
			end
			prevHp = mc:getHp()
		end)
	end)
end

return module
