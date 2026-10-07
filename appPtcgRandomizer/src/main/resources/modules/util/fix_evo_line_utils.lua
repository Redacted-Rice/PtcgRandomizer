-- Shared match previous / redistribute logic for evo line field fixers.
-- Supports branch aware grouping so branching lines can be fixed together,
-- per branch path, or with no continuity between shared roots and branch tips.
local branch_utils = require("modules.util.branch_utils")
local randomizer = require("randomizer")

local fix_evo_line_utils = {}

-- fieldName is the name of the field to fix, e.g. hp, retreatCost
function fix_evo_line_utils.create(fieldName)
	return {
		cardWithExtreme = function(cards, maxNotMin)
			return fix_evo_line_utils.cardWithExtreme(fieldName, cards, maxNotMin)
		end,
		matchPrevious = function(line)
			fix_evo_line_utils.matchPrevious(fieldName, line)
		end,
		redistribute = function(line)
			return fix_evo_line_utils.redistribute(fieldName, line)
		end,
		applyMode = function(cards, args)
			return fix_evo_line_utils.applyMode(fieldName, cards, args)
		end,
		fixTogether = function(line, args)
			fix_evo_line_utils.fixTogether(fieldName, line, args)
		end,
		fixSeparate = function(line, args)
			fix_evo_line_utils.fixSeparate(fieldName, line, args)
		end,
		fixIndividual = function(line, args)
			fix_evo_line_utils.fixIndividual(fieldName, line, args)
		end,
		fixLine = function(line, args)
			fix_evo_line_utils.fixLine(fieldName, line, args)
		end,
		fixEvoLines = function(context, args)
			fix_evo_line_utils.fixEvoLines(fieldName, context, args)
		end,
	}
end


-------------------- Field fixers -------------------------------


-- card in the list with the highest or lowest field value
function fix_evo_line_utils.cardWithExtreme(fieldName, cards, maxNotMin)
	local best = nil
	cards:each(function(mc)
		if best == nil then
			best = mc
		elseif maxNotMin and mc[fieldName] > best[fieldName] then
			best = mc
		elseif not maxNotMin and mc[fieldName] < best[fieldName] then
			best = mc
		end
	end)
	return best
end

-- raise later stages to the max value seen in all earlier stages
function fix_evo_line_utils.matchPrevious(fieldName, line)
	local byStage = randomizer.groupBy(line, "stage")
	local prevStageMax = 0

	byStage:sort():each(function(_, cardsAtStage)
		cardsAtStage:each(function(mc)
			if mc[fieldName] < prevStageMax then
				mc[fieldName] = prevStageMax
			end
		end)
		prevStageMax = cardsAtStage:max(fieldName)
	end)
end

-- swap high/low across adjacent stages until the line is ordered.
-- Returns true if any swap occurred.
function fix_evo_line_utils.redistribute(fieldName, line)
	local stages = {}
	local byStage = randomizer.groupBy(line, "stage")
	byStage:sort():each(function(_, cardsAtStage)
		table.insert(stages, cardsAtStage)
	end)

	local swappedAny = false
	local swapped = true
	while swapped do
		swapped = false
		for i = 1, #stages - 1 do
			local prev = stages[i]
			local nextStage = stages[i + 1]
			while prev:max(fieldName) > nextStage:min(fieldName) do
				local highCard = fix_evo_line_utils.cardWithExtreme(fieldName, prev, true)
				local lowCard = fix_evo_line_utils.cardWithExtreme(fieldName, nextStage, false)
				local tmp = highCard[fieldName]
				highCard[fieldName] = lowCard[fieldName]
				lowCard[fieldName] = tmp
				swapped = true
				swappedAny = true
			end
		end
	end
	return swappedAny
end

-- Applies either the match previous or redistribute mode to the cards
-- Returns true if any changes were made
function fix_evo_line_utils.applyMode(fieldName, cards, args)
	if cards == nil or cards:isEmpty() then
		return false
	end
	if args.mode == "Redistribute" then
		return fix_evo_line_utils.redistribute(fieldName, cards)
	end
	fix_evo_line_utils.matchPrevious(fieldName, cards)
	return false
end

-- Option 1: no continuity between different branch id sets. Cards that share
-- the same evoBranchIds set (e.g. all {1,2} cards, all {1} cards) are evaluated
-- together. Other sets are ignored for that pass.
function fix_evo_line_utils.fixSeparate(fieldName, line, args)
	line:groupBy(function(mc)
		return branch_utils.branchIdsKey(mc)
	end):each(function(_, group)
		fix_evo_line_utils.applyMode(fieldName, group, args)
	end)
end

-- Option 2: each branch path (shared ancestors + that branch's cards) is
-- made consistent independently. Cross branch stage values need not order.
-- Branch order is shuffled so Redistribute does not always favor lower ids.
function fix_evo_line_utils.fixIndividual(fieldName, line, args)
	local branchIds = branch_utils.branchIds(line)
	if #branchIds == 0 then
		logger.warn("No branch IDs found for line %s, resolving all together", line:map(function(mc)
			return mc.name
		end):join(", "))
		fix_evo_line_utils.applyMode(fieldName, line, args)
		return
	end

	randomizer.List.backedBy(branchIds):shuffle():each(function(branchId)
		fix_evo_line_utils.applyMode(fieldName, line:filter(function(mc)
			return branch_utils.hasBranchId(mc, branchId)
		end), args)
	end)
end

-- Option 3: one pool per stage across the whole evo line
function fix_evo_line_utils.fixTogether(fieldName, line, args)
	-- just call apply mode on the whole line
	fix_evo_line_utils.applyMode(fieldName, line, args)
end

function fix_evo_line_utils.fixLine(fieldName, line, args)
	local handling = args.branchHandling or fix_evo_line_utils.BRANCH_TOGETHER
	if handling == fix_evo_line_utils.BRANCH_SEPARATE then
		fix_evo_line_utils.fixSeparate(fieldName, line, args)
	elseif handling == fix_evo_line_utils.BRANCH_INDIVIDUAL then
		fix_evo_line_utils.fixIndividual(fieldName, line, args)
	else
		fix_evo_line_utils.fixTogether(fieldName, line, args)
	end
end

-- run the chosen mode on every evo line in the modified set
function fix_evo_line_utils.fixEvoLines(fieldName, context, args)
	local byEvoLine = randomizer.groupBy(context.modified:getRandomizableMonsterCards(),
			"evoLineId")
	byEvoLine:each(function(_, line)
		fix_evo_line_utils.fixLine(fieldName, line, args)
	end)
end


-------------------- Argument defs -------------------------


-- fieldDisplayName is the display name of the field, e.g. HP, retreat cost (for mode arg text)
-- fieldDisplayPlural is the plural of the field display name, e.g. HPs, retreat costs (for mode
-- arg text)
function fix_evo_line_utils.modeArg(fieldDisplayName, fieldDisplayPlural)
	return {
		name = "mode",
		displayName = "Mode",
		description = "'Match Previous' raises later stage cards " .. fieldDisplayName
				.. " to the highest " .. fieldDisplayName .. " seen in earlier stages."
				.. " 'Redistribute' swaps around existing " .. fieldDisplayPlural
				.. " so existing values are kept when possible",
		definition = {
			type = "string",
			constraint = {
				type = "enum",
				values = { "Match Previous", "Redistribute" },
			},
		},
		default = "Redistribute",
	}
end


fix_evo_line_utils.BRANCH_SEPARATE = "Separate Branches"
fix_evo_line_utils.BRANCH_INDIVIDUAL = "Individual Branches"
fix_evo_line_utils.BRANCH_TOGETHER = "All Together"

fix_evo_line_utils.BRANCH_HANDLING_ORDER = {
	fix_evo_line_utils.BRANCH_TOGETHER,
	fix_evo_line_utils.BRANCH_INDIVIDUAL,
	fix_evo_line_utils.BRANCH_SEPARATE,
}

fix_evo_line_utils.BRANCH_HANDLING_DESCRIPTIONS = {
	[fix_evo_line_utils.BRANCH_TOGETHER] = "'All Together' orders by stage across the whole line",
	[fix_evo_line_utils.BRANCH_INDIVIDUAL] = "'Individual Branches' makes each branch path "
			.. " consistent (shared root plus that branch) without comparing across branches",
	[fix_evo_line_utils.BRANCH_SEPARATE] = "'Separate Branches' fixes shared roots and each branch"
			.. " tips with no continuity between them",
}

-- defaultValue: required default BRANCH_* value (must remain after excludes)
-- excludeValues: optional list of BRANCH_* constants to omit from the enum
function fix_evo_line_utils.branchHandlingArg(defaultValue, excludeValues)
	local excluded = {}
	if excludeValues ~= nil then
		for _, value in ipairs(excludeValues) do
			excluded[value] = true
		end
	end

	local values = {}
	local descriptionParts = {}
	for _, value in ipairs(fix_evo_line_utils.BRANCH_HANDLING_ORDER) do
		if not excluded[value] then
			table.insert(values, value)
			table.insert(descriptionParts, fix_evo_line_utils.BRANCH_HANDLING_DESCRIPTIONS[value])
		end
	end

	return {
		name = "branchHandling",
		displayName = "Branch Handling",
		description = table.concat(descriptionParts, ". ") .. ".",
		definition = {
			type = "string",
			constraint = {
				type = "enum",
				values = values,
			},
		},
		default = defaultValue,
	}
end

return fix_evo_line_utils
