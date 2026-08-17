-- cards is the input deck. expect is matched by name after the module runs.
return {
	name = "set_to_two",
	module = "set_num_moves",
	args = {
		numMoves = 2,
	},
	cards = {
		{ name = "CardA", numMoves = 0 },
		{ name = "CardB", numMoves = 1 },
	},
	expect = {
		{ name = "CardA", numMoves = 2 },
		{ name = "CardB", numMoves = 2 },
	},
}
