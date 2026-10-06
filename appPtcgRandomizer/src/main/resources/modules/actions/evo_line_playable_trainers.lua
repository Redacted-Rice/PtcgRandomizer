-- Adjusts playable trainer proxies in evo lines
-- restore by swapping onto a matching line shape, demote lines they anchored, or swap for a basic.
local common_field_defs = require("modules.util.common_field_defs")
local evo_line_randomize_utils = require("modules.util.evo_line_randomize_utils")
local randomizer = require("randomizer")
local utils = require("randomizer.utils")
local logger = require("randomizer.logger")

-- Playable trainer evo line fixups
local ACTION_REPLACE_WITH_BASIC = "Replace With Basic"
local ACTION_RESTORE_ORIGINAL_LINES = "Restore Original Lines"
local ACTION_REMOVE_FROM_LINES = "Remove From Evo Lines"
local ARG_DEF_TRAINER_PROXY_EVO_ACTION = {
	name = "action",
	displayName = "Action",
	description = "`Replace With Basic` swaps each trainer part of an evo line with a random"
			.. " non-evolving basic so no trainers will be part of an evo line."
			.. " `Restore Original Lines` looks up each trainer's original line shape"
			.. " and swaps that trainer with a basic of a current line that matches the shape."
			.. " `Remove From Evo Lines` removes the trainer from the evo line and shifts all evos"
			.. " down a stage (STAGE_1 to BASIC, STAGE_2 to STAGE_1).",
	definition = {
		type = "string",
		constraint = {
			type = "enum",
			values = {
				ACTION_REPLACE_WITH_BASIC,
				ACTION_RESTORE_ORIGINAL_LINES,
				ACTION_REMOVE_FROM_LINES,
			},
		},
	},
	default = ACTION_REPLACE_WITH_BASIC,
}

local module
module = {
	id = "evo_line_playable_trainers",
	name = "Modify Playable Trainer Evo Lines",
	description = "Restores, removes, or replaces playable trainer cards in evolution lines."
			.. " Evo line metadata will need to be re-run if needed after this",
	groups = { "Monsters", "Evolutions", "Trainers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineId", type = "integer" },
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		ARG_DEF_TRAINER_PROXY_EVO_ACTION,
	},
	seeded = true,
	execute = function(context, args)
		return module.fixPlayableTrainers(context, args)
	end,
}

function module.fixPlayableTrainers(context, args)
	evo_line_randomize_utils.init(context)

	local modified = randomizer.list(context.modified:getRandomizableMonsterCardsWithProxies())
	local toModifyByName = randomizer.groupBy(modified, "name:toString")
	local toModifyByPrevEvo = modified:filter(function(card)
		return not card.prevEvoName:isEmpty()
	end):groupBy("prevEvoName:toString")
	local modifiedProxyNames = modified:filter(function(card)
		return card.isTrainerProxy
	end):select("name:toString"):removeDuplicates()

	local action = tostring(args.action)
	if action == ACTION_RESTORE_ORIGINAL_LINES then
		local original = randomizer.list(context.original:getRandomizableMonsterCardsWithProxies())
		module.restoreOriginalLines(original, modified, modifiedProxyNames, toModifyByName, toModifyByPrevEvo)
	elseif action == ACTION_REMOVE_FROM_LINES then
		module.removeFromLines(modifiedProxyNames, toModifyByPrevEvo)
	elseif action == ACTION_REPLACE_WITH_BASIC then
		module.replaceWithBasic(modified, toModifyByName, toModifyByPrevEvo, modifiedProxyNames)
	else
		error("Unknown playable trainer evo action: " .. action)
	end

	-- Ensure our evo line ai data is up to date after this
	evo_line_randomize_utils.syncEvoAiData(context, modified)
end

---------------------------- RESTORE --------------------------------

-- For each trainer, read its original ROM line shape, find a current line with that shape,
-- and swap the trainer with that line's basic. Pure swap so HAS_EVOLUTION stays consistent.
function module.restoreOriginalLines(original, modified, modifiedProxyNames, toModifyByName,
			toModifyByPrevEvo)
	local origByName = original:groupBy("name:toString")

	-- original evoLineId -> ShapeKey
	local origEvoLineIdToShapeKey = original:groupBy("evoLineId"):mapToTable(function(key, cards)
		return key, evo_line_randomize_utils.evoLineShapeKey(cards)
	end)
	-- evoLineId -> ShapeKey
	local modifiedEvoLineIdToShapeKey = modified:groupBy("evoLineId"):mapToTable(function(key, cards)
		return key, evo_line_randomize_utils.evoLineShapeKey(cards)
	end)
	-- ShapeKey -> {{line1Cards}, {line2Cards}, ...}
	local modifiedNonProxyLinesByShapeKey = modified:groupBy("evoLineId"):remap(function(_, cards)
		-- Filter out proxies
		if cards:findFirst("isTrainerProxy") ~= nil then
			return nil, nil
		end
		return evo_line_randomize_utils.evoLineShapeKey(cards), cards
	end)

	modifiedProxyNames:each(function(modifiedProxyName)
		-- Gets all cards with the proxyName and get the evo line id from the first
		local origCards = origByName:get(modifiedProxyName)
		if origCards == nil or origCards:isEmpty() then
			logger.warn("evo_line_playable_trainers: no original line for " .. modifiedProxyName)
			return
		end
		local origEvoLineId = origCards:get(1).evoLineId
		-- Get the shape for that evo line id
		local originalShape = origEvoLineIdToShapeKey[origEvoLineId]

		-- Same for the modified
		local modCards = toModifyByName:get(modifiedProxyName)
		if modCards == nil or modCards:isEmpty() then
			logger.warn("evo_line_playable_trainers: no modified line for " .. modifiedProxyName)
			return
		end
		local modEvoLineId = modCards:get(1).evoLineId
		local newShape = modifiedEvoLineIdToShapeKey[modEvoLineId]

		-- If its already the right shape, were done
		if newShape == originalShape then
			logger.debug("evo_line_playable_trainers " .. modifiedProxyName
					.. " already on shape " .. originalShape)
			return
		end

		-- Get the available lines based on the original shape
		local lines = modifiedNonProxyLinesByShapeKey:get(originalShape)
		if lines == nil or lines:isEmpty() then
			logger.warn("evo_line_playable_trainers: no current line shaped "
					.. originalShape .. " to restore " .. modifiedProxyName)
			return
		end

		-- Choose a random one from the list and perform the swap
		-- Its consumed so it won't get selected by any other passes
		local lineToSwapWith = utils.consumeRandomElement(lines.items)
		local swapBasicName = lineToSwapWith:findFirst(function(card)
			return card.stage == evo_line_randomize_utils.evolutionStage.BASIC
		end).name:toString()
		-- Neither the proxy nor the selected card will be in the list of candidates
		-- so we don't need to update any of the lists/groups/tables
		evo_line_randomize_utils.swapEvoSlots(modifiedProxyName, swapBasicName, toModifyByName,
				toModifyByPrevEvo)
		logger.info("evo_line_playable_trainers restored " .. modifiedProxyName .. " onto shape "
				.. originalShape .. " by swapping with " .. swapBasicName)
	end)
end

---------------------------- REMOVE ---------------------------------

function module.addEvoNamesToLine(name, toModifyByPrevEvo, list)
	local evos = toModifyByPrevEvo:get(name)
	if evos == nil then
		return
	end
	-- Evos can have multiple versions of the cards so group them for convinience
	evos:groupBy("name:toString"):each(function(_, evoGroup)
		table.insert(list, evoGroup)
	end)
end

-- Demote every card that has trainerName in its prevEvo chain by one stage.
function module.removeFromLines(modifiedProxyNames, toModifyByPrevEvo)
	modifiedProxyNames:each(function(modifiedProxyName)
		local toDemoteEvosOf = {}
		module.addEvoNamesToLine(modifiedProxyName, toModifyByPrevEvo, toDemoteEvosOf)
		-- While we have prev evos to process, process the next one
		while #toDemoteEvosOf > 0 do
			-- Pop the first entry and add its evos
			local evoGroup = table.remove(toDemoteEvosOf, 1)
			local evoName = evoGroup:get(1).name:toString()
			module.addEvoNamesToLine(evoName, toModifyByPrevEvo, toDemoteEvosOf)

			local newStage = evoGroup:get(1).stage:getPreviousStage()
			evo_line_randomize_utils.applyStageToCards(evoGroup, newStage)
			-- If its now a basic, clear the prev evo as well
			if newStage == evo_line_randomize_utils.evolutionStage.BASIC then
				evo_line_randomize_utils.applyPrevEvoToCards(evoGroup, "")
			end
			logger.debug("evo_line_playable_trainers demoted " .. evoName
					.. " off proxy " .. modifiedProxyName)
		end
	end)
end

---------------------------- REPLACE --------------------------------

-- Swap each trainer that still anchors evolutions with a random lone basic.
-- Trainer ends up as non-evolving basics and the chosen basic takes the
-- trainer's old evo slot.
function module.replaceWithBasic(modified, toModifyByName, toModifyByPrevEvo, modifiedProxyNames)
	local basicValue = evo_line_randomize_utils.evolutionStage.BASIC:getValue()
	-- Get all basic/basic cards that aren't proxies as an Array
	-- TODO: Use mutatable list in the future?
	local nonEvolvingNonProxyNamesArray = modified:filter(function(card)
		return not card.isTrainerProxy
			and card.stage:getValue() == basicValue
			and card.evoLineMaxStage:getValue() == basicValue
	end):select("name:toString"):removeDuplicates():toTable()

	modifiedProxyNames:each(function(modifiedProxyName)
		local proxiesEvos = toModifyByPrevEvo:get(modifiedProxyName)
		-- Already lone? Nothing to replace
		if proxiesEvos == nil or proxiesEvos:isEmpty() then
			logger.debug("evo_line_playable_trainers skip replace " .. modifiedProxyName
					.. " (no evolutions)")
			return
		end
		if #nonEvolvingNonProxyNamesArray == 0 then
			logger.warn("evo_line_playable_trainers: no lone basics left for "
					.. modifiedProxyName)
			return
		end

		-- Choose a random basic/basic non-proxy and swap
		local swapName = utils.consumeRandomElement(nonEvolvingNonProxyNamesArray)
		-- Neither the proxy nor the selected card will be in the list of candidates
		-- so we don't need to update any of the lists/groups/tables
		evo_line_randomize_utils.swapEvoSlots(modifiedProxyName, swapName, toModifyByName, toModifyByPrevEvo)
		logger.info("evo_line_playable_trainers replaced " .. modifiedProxyName
				.. " with lone basic " .. swapName)
	end)
end

return module
