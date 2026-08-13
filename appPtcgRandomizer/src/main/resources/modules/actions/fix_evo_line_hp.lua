-- Enforces non-decreasing HP up each evolution line by stage.
-- Run after HP randomization. Needs evoLineId on each card.
local randomizer = require("randomizer")

local module
module = {
	id = "fix_evo_line_hp",
	name = "Make Evo Line HP Consistent",
	description = "For each evolution line, ensures HP is non-decreasing by stage. Raise Minimums bumps later stages up. Redistribute swaps inverted values between stages so existing HPs are kept when possible",
	groups = { "Monsters", "HP", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
	},
	seeded = false,
	arguments = {
		{
			name = "mode",
			displayName = "Mode",
			definition = {
                type = "string",
                constraint = {
                    type = "enum",
                    values = { "Raise Minimums", "Redistribute" },
                },
			},
			default = "Redistribute",
		},
	},
	execute = function(context, args)
		return module.fixEvoLineHp(context, args)
	end,
}

function module.raiseMinimums(line)
	local byStage = randomizer.groupBy(line, function(mc)
		return mc.stage:getValue()
	end)
	local prevStageMaxHp = 0

	byStage:sort():each(function(_, cardsAtStage)
		cardsAtStage:each(function(mc)
			if mc:getHp() < prevStageMaxHp then
				mc:setHp(prevStageMaxHp)
			end
		end)
		prevStageMaxHp = cardsAtStage:max("getHp")
	end)
end

function module.swapHp(a, b)
	local tmp = a:getHp()
	a:setHp(b:getHp())
	b:setHp(tmp)
end

function module.cardWithExtremeHp(cards, wantMax)
	local best = nil
	cards:each(function(mc)
		if best == nil then
			best = mc
		elseif wantMax and mc:getHp() > best:getHp() then
			best = mc
		elseif not wantMax and mc:getHp() < best:getHp() then
			best = mc
		end
	end)
	return best
end

-- Bubble-style swaps: when a later stage dips below an earlier one, swap that later
-- stage's lowest HP with the earlier stage's highest. Keeps already ordered values put
function module.redistribute(line)
	local stages = {}
	local byStage = randomizer.groupBy(line, function(mc)
		return mc.stage:getValue()
	end)
	byStage:sort():each(function(_, cardsAtStage)
		table.insert(stages, cardsAtStage)
	end)

	local swapped = true
	while swapped do
		swapped = false
		for i = 1, #stages - 1 do
			local prev = stages[i]
			local nextStage = stages[i + 1]
			while prev:max("getHp") > nextStage:min("getHp") do
				local high = module.cardWithExtremeHp(prev, true)
				local low = module.cardWithExtremeHp(nextStage, false)
				module.swapHp(high, low)
				swapped = true
			end
		end
	end
end

function module.fixEvoLineHp(context, args)
	local byEvoLine = randomizer.groupBy(context.modified:getRandomizableMonsterCards(), "evoLineId")
	byEvoLine:each(function(_, line)
		if args.mode == "Redistribute" then
			module.redistribute(line)
		else
			module.raiseMinimums(line)
		end
	end)
end

return module
