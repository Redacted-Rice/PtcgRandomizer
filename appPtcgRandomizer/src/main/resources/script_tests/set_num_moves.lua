-- Cards start with 0, 1, and 2 moves. All get set to 2.
return {
	{
		name = "set_to_2",
		module = "set_num_moves",
		args = {
			numMoves = 2,
		},
		cards = {
			{ id = "MONSTER_001", numMoves = 0 },
			{ id = "MONSTER_002", numMoves = 1 },
			{ id = "MONSTER_003_1", numMoves = 2 },
		},
		expect = {
			{ id = "MONSTER_001", numMoves = 2 },
			{ id = "MONSTER_002", numMoves = 2 },
			{ id = "MONSTER_003_1", numMoves = 2 },
		},
	},
}
