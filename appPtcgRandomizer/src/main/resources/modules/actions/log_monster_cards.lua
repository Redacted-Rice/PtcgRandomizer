local datatable = require("randomizer").datatable

local module
module = {
	id = "log_monster_cards",
	name = "Log Monster Cards",
	description = "Log monster card data as an ASCII table",
	seeded = false,
	groups = { "Logging" },
	modifies = {},
	author = "Redacted Rice",
	version = "0.7",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.logMonsterCards(context, args)
	end,
}

-- TODO later: Make the fields logged configurable and split card logging into a
-- util for use in debugging scripts
function module.logMonsterCards(context)
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
		fields = {
			{ header = "Lvl", align = "right", getter = function(card) return card:getLevel() end, },
			{ header = "Type", getter = function(card) return card.type:convertToEnergyType():toString() end, },
			{ header = "HP", align = "right", getter = function(card) return card:getHp() end, },
			{ header = "Move 1", getter = function(card) return card:getMove(0).name:toString() end, },
			{ header = "Move 2", getter = function(card) return card:getMove(1).name:toString() end, },
		},
	}, {
		leadingNewline = true,
	})

	if tableOutput ~= "" then
		logger.info(tableOutput)
	end
end

return module
