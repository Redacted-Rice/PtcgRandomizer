-- Light tests for PTCG change detector move summary layout
local randomizer = require("randomizer")
local changedetector = randomizer.changedetector

local setupChunk, setupError = loadfile(MODULES_DIR .. "/prescripts/changedetector_setup.lua")
if not setupChunk then
	error(setupError)
end
local setup = setupChunk()

local function listContains(list, value)
	for _, entry in ipairs(list) do
		if entry == value then
			return true
		end
	end
	return false
end

local function assertContains(list, value, message)
	if not listContains(list, value) then
		error(message or ("expected '" .. tostring(value) .. "' in list"))
	end
end

local function findSummary(overrides, fieldKey)
	for _, summarySpec in ipairs(overrides.summary) do
		if summarySpec.field == fieldKey then
			return summarySpec
		end
	end
	return nil
end

-- name-only layout rolls other move suffixes into summary groups
local nameOnlyOverrides =
	setup.buildMoveSlotSummarySettings(setup.buildMoveSlotConfigs({ "name" }))

assertContains(nameOnlyOverrides.detail, "move1_name", "move1 name should be detail")
assertContains(nameOnlyOverrides.detail, "move2_name", "move2 name should be detail")
if listContains(nameOnlyOverrides.detail, "move1_damage") then
	error("move1 damage should not be detail for name-only layout")
end

local move1DamageSummary = findSummary(nameOnlyOverrides, "move1_damage")
if not move1DamageSummary or move1DamageSummary.group ~= "move1" then
	error("move1 damage should be summarized under move1")
end

if #nameOnlyOverrides.summaryGroups ~= 2 then
	error("expected summary groups for both move slots")
end

-- attack-style layout keeps name, cost, and damage as detail columns
local attackOverrides = setup.buildMoveSlotSummarySettings(
	setup.buildMoveSlotConfigs({ "name", "energyCost", "damage" })
)

assertContains(attackOverrides.detail, "move1_energyCost", "move1 cost should be detail")
assertContains(attackOverrides.detail, "move1_damage", "move1 damage should be detail")
if not findSummary(attackOverrides, "move1_category") then
	error("move1 category should be summarized for attack layout")
end

local function makeMove(fields)
	fields = fields or {}
	return {
		name = { toString = function()
			return fields.name or ""
		end },
		damage = fields.damage or 0,
		category = fields.category or "NORMAL",
		description = { toString = function()
			return fields.description or ""
		end },
		getEnergyCostString = function()
			return fields.energyCost or "None"
		end,
		getEffectSortKey = function()
			return fields.effect or "NONE"
		end,
		getEffect1Byte = function()
			return fields.effect1 or 0
		end,
		getEffect2Byte = function()
			return fields.effect2 or 0
		end,
		getEffect3Byte = function()
			return fields.effect3 or 0
		end,
		getAnimation = function()
			return fields.animation or 0
		end,
		getUnknownByte = function()
			return fields.unknownByte or 0
		end,
	}
end

local moveState = {
	name = "Tackle",
	damage = 20,
	category = "NORMAL",
	description = "old",
}

local card = {
	type = "GRASS",
	gfx = 1,
	rarity = "COMMON",
	set = "BASE",
	pack = "A",
	stage = "BASIC",
	prevEvoName = { toString = function()
		return ""
	end },
	retreatCost = 1,
	weakness = "FIRE",
	resistance = "NONE",
	monsterCategory = { toString = function()
		return "Seed"
	end },
	dexNumber = 1,
	unknownByte1 = 0,
	level = 10,
	lengthFt = 2,
	lengthIn = 4,
	weight = 100,
	description = { toString = function()
		return "desc"
	end },
	unknownByte2 = 0,
	getHp = function()
		return 50
	end,
	getNumMoves = function()
		return 1
	end,
	getIdValue = function()
		return 1
	end,
	name = { toString = function()
		return "Bulbasaur"
	end },
	getMove = function(self, index)
		if index == 0 then
			return makeMove(moveState)
		end
		if index == 1 then
			return makeMove({ name = "" })
		end
		return nil
	end,
}

changedetector.stopMonitoringAll()
changedetector.configure(true)
changedetector.monitor(setup.monsterCardsEntry, { card }, {
	title = "Monster Cards",
	primaryKey = {
		header = "ID",
		numeric = true,
		getter = function(obj)
			return obj:getIdValue()
		end,
	},
	description = {
		header = "Name",
		getter = function(obj)
			return obj.name:toString()
		end,
	},
	fields = setup.buildMonsterCardFields(),
})

changedetector.pushDisplaySettings(
	setup.monsterCardsEntry,
	setup.buildMoveSlotSummarySettings(setup.buildMoveSlotConfigs({ "name" }))
)

changedetector.takeSnapshots()

moveState.name = "Slam"
moveState.damage = 30
moveState.category = "POWER"

local changes = changedetector.detectChanges()
if not changedetector.hasChanges(changes) then
	error("expected move changes to be detected")
end

local row = changes[setup.monsterCardsEntry]["1"]
if not row or row.move1_name.new ~= "Slam" then
	error("expected move1 name to change to Slam")
end

if not row.move1_additional or row.move1_additional.new == "-" then
	error("expected move1 additional summary column")
end

if not string.find(row.move1_additional.new, "damage", 1, true) then
	error("expected damage in move1 additional summary")
end

local formatted = changedetector.formatChangesTable(changes)
if not string.find(formatted, "Move 1 Additional", 1, true) then
	error("expected formatted table to include Move 1 Additional column")
end

changedetector.popDisplaySettings(setup.monsterCardsEntry)
changedetector.stopMonitoringAll()

return true
