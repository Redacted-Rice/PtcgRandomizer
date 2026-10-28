local module
module = {
	id = "dev_deck_read_write_test",
	name = "[Dev] Deck Read Write Test",
	description = "Quick test for added deck accessors",
	seeded = false,
	groups = { "dev" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "deck",
			displayName = "Deck",
			description = "Which deck to tweak",
			definition = {
				type = "enum",
				constraint = "DeckId",
			},
			default = "FIRE_STARTER_DECK",
		},
		{
			name = "cardId",
			displayName = "Card",
			description = "Card id entry to adjust in that deck",
			definition = {
				type = "string",
			},
			default = "ENERGY_FIRE",
		},
		{
			name = "delta",
			displayName = "Delta",
			description = "Amount to add or subract to that card's count",
			definition = {
				type = "number",
			},
			default = 1,
		},
	},
	execute = function(context, args)
		return module.adjustDeckEntry(context, args)
	end,
}

function module.adjustDeckEntry(context, args)
	local deckValue = args.deck
	local cardId = context.CardId[args.cardId]
	if not cardId then
		error("Unknown card id: " .. tostring(args.cardId))
	end

	local originalDeck = context.original.decks:getDeck(deckValue)
	local modifiedDeck = context.modified.decks:getDeck(deckValue)

	local before = originalDeck:getCountOfCard(cardId)
	local after = before + args.delta
	if after < 0 then
		after = 0
	end

	logger.info("Deck " .. tostring(deckValue) .. " " .. args.cardId .. " count " .. before .. " -> " .. after)
	modifiedDeck:setCountOfCard(cardId, after)
end

return module
