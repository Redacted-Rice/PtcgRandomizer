-- Reassigns dex numbers so evo branches appear in depth-first pokedex order.
-- Shared ancestors (multiple evoBranchIds) are assigned once on the first branch
-- that reaches them; later branches skip already-named cards.
-- Run after dex number randomization. Needs evoLineId and evoBranchIds on each card.
local randomizer = require("randomizer")
local branch_utils = require("modules.util.branch_utils")
local conversion_utils = require("modules.util.conversion_utils")

local module
module = {
	id = "dex_number_fix_evo_line",
	name = "Make Dex Numbers Consistent for Evo Lines",
	description = "Updates dex numbers so evo branches are shown depth-first "
		.. "(basic, then each branch through its stages) in the pokedex",
	groups = { "Monsters", "Pokedex", "Evolutions", "Support", "Consistency" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoBranchIds", type = "List<integer>" },
	},
	seeded = false,
	execute = function(context, args)
		return module.fixDexNumbers(context, args)
	end,
}

function module.lowestBasicDexNumber(line)
	local basics = randomizer.groupBy(line, "stage"):get("BASIC")
	if basics == nil or basics:isEmpty() then
		return nil
	end
	return basics:min(function(mc)
		return conversion_utils.byteToUnsigned(mc.dexNumber)
	end)
end

-- Depth first along one branch assigning dex numbers
-- One dex per card name - names already assigned (shared earlier in the line) are skipped
function module.assignBranch(line, branchId, startId, assignedNames)
	local dexId = startId
	line:filter(function(mc)
		return branch_utils.hasBranchId(mc, branchId)
	end):groupBy("stage"):sort():each(function(_, cardsAtStage)
		cardsAtStage:groupBy("name:toString"):each(function(name, named)
			if assignedNames[name] ~= nil then
				return
			end
			named:each(function(mc)
				mc.dexNumber = conversion_utils.byteToSigned(dexId)
			end)
			assignedNames[name] = dexId
			dexId = dexId + 1
		end)
	end)
	return dexId
end

-- For each line, go through each branch one at a time
-- so they are assigned in depth first order
function module.assignLine(line, startId, assignedNames)
	local dexId = startId
	local branchIds = branch_utils.branchIds(line)
	table.sort(branchIds)
	for _, branchId in ipairs(branchIds) do
		dexId = module.assignBranch(line, branchId, dexId, assignedNames)
	end
	return dexId
end

function module.fixDexNumbers(context)
	local cards = context.modified:getRandomizableMonsterCards()
	local nextId = 1
	local assignedNames = {}

	-- Lines ordered by lowest basic dex so relative pokedex placement stays stable.
	-- Within each line, branches get consecutive ids.
	randomizer.groupBy(cards, "evoLineId"):mapToList(function(_, line)
		return {
			line = line,
			lowestBasicDexNum = module.lowestBasicDexNumber(line) or 999,
		}
	end):sort(function(a, b)
			return a.lowestBasicDexNum < b.lowestBasicDexNum
	end):each(function(entry)
			nextId = module.assignLine(entry.line, nextId, assignedNames)
	end)
end

return module
