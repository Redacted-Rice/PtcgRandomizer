-- Shared match previous / redistribute logic for evo line field fixers.
local randomizer = require("randomizer")

local fix_evo_line_utils = {}

-- fieldName is the name of the field to fix, e.g. hp, retreatCost
function fix_evo_line_utils.create(fieldName)
	local utils = {}

	-- card in the list with the highest or lowest field value
	function utils.cardWithExtreme(cards, maxNotMin)
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
	function utils.matchPrevious(line)
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

	-- swap high/low across adjacent stages until the line is ordered
	function utils.redistribute(line)
		local stages = {}
		local byStage = randomizer.groupBy(line, "stage")
		byStage:sort():each(function(_, cardsAtStage)
			table.insert(stages, cardsAtStage)
		end)

		local swapped = true
		while swapped do
			swapped = false
			for i = 1, #stages - 1 do
				local prev = stages[i]
				local nextStage = stages[i + 1]
				while prev:max(fieldName) > nextStage:min(fieldName) do
					local highCard = utils.cardWithExtreme(prev, true)
					local lowCard = utils.cardWithExtreme(nextStage, false)
					local tmp = highCard[fieldName]
					highCard[fieldName] = lowCard[fieldName]
					lowCard[fieldName] = tmp
					swapped = true
				end
			end
		end
	end

	-- run the chosen mode on every evo line in the modified set
	function utils.fixEvoLines(context, args)
		local byEvoLine = randomizer.groupBy(context.modified:getRandomizableMonsterCards(), "evoLineId")
		byEvoLine:each(function(_, line)
			if args.mode == "Redistribute" then
				utils.redistribute(line)
			else
				utils.matchPrevious(line)
			end
		end)
	end

	return utils
end

-- fieldDisplayName is the display name of the field, e.g. HP, retreat cost (for mode arg text)
-- fieldDisplayPlural is the plural of the field display name, e.g. HPs, retreat costs (for mode arg text)
function fix_evo_line_utils.modeArg(fieldDisplayName, fieldDisplayPlural)
	return {
		name = "mode",
		displayName = "Mode",
		description = "'Match Previous' raises later stage cards " .. fieldDisplayName
			.. " to the highest " .. fieldDisplayName .. " seen in earlier stages. 'Redistribute' swaps around existing "
			.. fieldDisplayPlural .. " so existing values are kept when possible",
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

return fix_evo_line_utils
