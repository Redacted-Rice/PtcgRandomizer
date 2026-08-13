local datatable = require("randomizer").datatable

local module
module = {
	id = "log_monster_cards",
	name = "Log Monster Cards",
	description = "Log monster card data as an ASCII table",
	seeded = false,
	groups = { "Support" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "fields",
			displayName = "Fields",
			definition = {
				type = "list",
				elementDefinition = {
					type = "string",
					constraint = {
						type = "enum",
						values = {
                            "Lvl", "Type", "HP", "Stage", "Prev Evo", "Num Moves",
							"Retreat", "Weakness", "Resistance", "Category", "Dex #", "Evo Line",
							"Max Stage", "Move 1", "Move 1 Cost", "Move 1 Dmg", "Move 2",
							"Move 2 Cost", "Move 2 Dmg",
						},
					},
				},
			},
			default = {
				"Lvl", "Type", "HP", "Stage", "Num Moves",
				"Move 1", "Move 1 Cost", "Move 2", "Move 2 Cost",
			},
		},
	},
	execute = function(context, args)
		return module.logMonsterCards(context, args)
	end,
}

local function moveName(card, slot)
	return card:getMove(slot).name:toString()
end

local function moveCost(card, slot)
	return card:getMove(slot):getEnergyCostString(true, "/")
end

local function moveDamage(card, slot)
	return card:getMove(slot):getDamageString()
end

local FIELD_SPECS = {
	["Lvl"] = {
		header = "Lvl",
		align = "right",
		getter = function(card) return card:getLevel() end,
	},
	["Type"] = {
		header = "Type",
		getter = function(card) return card.type:convertToEnergyType():toString() end,
	},
	["HP"] = {
		header = "HP",
		align = "right",
		getter = function(card) return card:getHp() end,
	},
	["Stage"] = {
		header = "Stage",
		getter = function(card) return tostring(card.stage) end,
	},
	["Prev Evo"] = {
		header = "Prev Evo",
		getter = function(card) return card.prevEvoName:toString() end,
	},
	["Num Moves"] = {
		header = "Moves",
		align = "right",
		getter = function(card) return card:getNumMoves() end,
	},
	["Retreat"] = {
		header = "Retreat",
		align = "right",
		getter = function(card) return card.retreatCost end,
	},
	["Weakness"] = {
		header = "Weak",
		getter = function(card) return tostring(card.weakness) end,
	},
	["Resistance"] = {
		header = "Resist",
		getter = function(card) return tostring(card.resistance) end,
	},
	["Category"] = {
		header = "Category",
		getter = function(card) return card.monsterCategory:toString() end,
	},
	["Dex #"] = {
		header = "Dex #",
		align = "right",
		getter = function(card) return card.dexNumber end,
	},
	["Evo Line"] = {
		header = "Evo Line",
		align = "right",
		getter = function(card) return card.evoLineId end,
	},
	["Max Stage"] = {
		header = "Max Stage",
		getter = function(card)
			return card.evoLineMaxStage ~= nil and tostring(card.evoLineMaxStage) or ""
		end,
	},
	["Move 1"] = {
		header = "Move 1",
		getter = function(card) return moveName(card, 0) end,
	},
	["Move 1 Cost"] = {
		header = "M1 Cost",
		getter = function(card) return moveCost(card, 0) end,
	},
	["Move 1 Dmg"] = {
		header = "M1 Dmg",
		align = "right",
		getter = function(card) return moveDamage(card, 0) end,
	},
	["Move 2"] = {
		header = "Move 2",
		getter = function(card) return moveName(card, 1) end,
	},
	["Move 2 Cost"] = {
		header = "M2 Cost",
		getter = function(card) return moveCost(card, 1) end,
	},
	["Move 2 Dmg"] = {
		header = "M2 Dmg",
		align = "right",
		getter = function(card) return moveDamage(card, 1) end,
	},
}

function module.buildFields(fieldNames)
	local fields = {}
	for _, name in ipairs(fieldNames or {}) do
		local spec = FIELD_SPECS[name]
		if spec then
			fields[#fields + 1] = spec
		else
			logger.warn(string.format("log_monster_cards ignoring unknown field '%s'", tostring(name)))
		end
	end
	return fields
end

function module.logMonsterCards(context, args)
	if not context.modified then
		return
	end

	local monsterCards = context.modified:getRandomizableMonsterCards()
	local tableOutput = datatable.format(monsterCards, {
		title = "Monster Cards",
		headerEvery = 30,
		trailingHeader = true,
		primaryKey = {
			header = "ID",
			align = "right",
			numeric = true,
			getter = function(card)
				return card:getIdValue()
			end,
		},
		description = {
			header = "Name",
			getter = function(card)
				return card.name:toString()
			end,
		},
		fields = module.buildFields(args.fields),
	}, {
		leadingNewline = true,
	})

	if tableOutput ~= "" then
		logger.info(tableOutput)
	end
end

return module
