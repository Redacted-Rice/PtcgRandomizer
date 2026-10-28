-- Covers dev_deck_read_write_test (deck accessors in modified context).
-- decks and expectDecks rows use id = DeckId name and cards = { CARD_ID = count }.
return {
	{
		name = "add_one",
		module = "dev_deck_read_write_test",
		args = {
			deck = "FIRE_STARTER_DECK",
			cardId = "ENERGY_FIRE",
			delta = 1,
		},
		decks = {
			{
				id = "FIRE_STARTER_DECK",
				cards = {
					ENERGY_FIRE = 10,
				},
			},
		},
		expectDecks = {
			{
				id = "FIRE_STARTER_DECK",
				cards = {
					ENERGY_FIRE = 11,
				},
			},
		},
	},
	{
		name = "clamp_at_zero",
		module = "dev_deck_read_write_test",
		args = {
			deck = "FIRE_STARTER_DECK",
			cardId = "ENERGY_FIRE",
			delta = -100,
		},
		decks = {
			{
				id = "FIRE_STARTER_DECK",
				cards = {
					ENERGY_FIRE = 5,
				},
			},
		},
		expectDecks = {
			{
				id = "FIRE_STARTER_DECK",
				cards = {
					ENERGY_FIRE = 0,
				},
			},
		},
	},
}
