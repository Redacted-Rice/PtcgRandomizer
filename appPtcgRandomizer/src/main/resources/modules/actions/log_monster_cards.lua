local datatable = require("randomizer").datatable

local function moveName(moveIndex)
	return function(card)
		local move = card:getMove(moveIndex)
		if move:isEmpty() then
			return ""
		end
		return move.name:toString()
	end
end

return {
	name = "log_monster_cards",
	description = "Log monster card data as an ASCII table",
	groups = { "pokemon cards" },
	modifies = {},
	author = "Redacted Rice",
	version = "0.1",
	requires = {
		UniversalRandomizerJava = "0.5.0",
		PtcgRandomizer = "0.2.0",
	},

	execute = function(context)
		if not context.modified then
			return
		end

		local monsterCards = context.modified:getMonsterCards()
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
				{
					header = "Lvl",
					align = "right",
					getter = function(card)
						return card:getLevel()
					end,
				},
				{
					header = "Type",
					getter = function(card)
						return card.type:convertToEnergyType():toString()
					end,
				},
				{
					header = "HP",
					align = "right",
					getter = function(card)
						return card:getHp()
					end,
				},
				{
					header = "Move 1",
					getter = moveName(0),
				},
				{
					header = "Move 2",
					getter = moveName(1),
				},
			},
		}, {
			leadingNewline = true,
		})

		if tableOutput ~= "" then
			logger.info(tableOutput)
		end
	end,
}
