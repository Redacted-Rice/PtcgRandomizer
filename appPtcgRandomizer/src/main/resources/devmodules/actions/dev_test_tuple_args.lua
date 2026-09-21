-- Dev only module used to manually verify the module config UI renders and saves TUPLE arguments
-- (standalone tuples, list elements, and table values). Tuple fields must be scalar or enum types.
local module
module = {
	id = "dev_test_tuple_args",
	name = "[Dev] Tuple Argument Test",
	description = "Exercises tuple argument types in the config UI",
	seeded = true,
	groups = { "dev" },
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			-- List<Tuple<String, Integer>> - flat tuple list (pair row grid)
			name = "labelCountList",
			definition = {
				type = "list",
				elementDefinition = {
					type = "tuple",
					fields = {
						{ name = "label", definition = { type = "string" } },
						{
							name = "count",
							definition = {
								type = "int",
								constraint = { type = "range", min = 0, max = 99 },
							},
						},
					},
				},
			},
			default = {
				{ label = "alpha", count = 3 },
				{ label = "beta", count = 7 },
			},
		},
		{
			-- Table<String, Tuple<Integer, Integer>> - tuple as table value
			name = "rangeCaps",
			definition = {
				type = "table",
				keyDefinition = {
					type = "string",
				},
				valueDefinition = {
					type = "tuple",
					fields = {
						{
							name = "min",
							definition = {
								type = "int",
								constraint = { type = "range", min = 0, max = 999 },
							},
						},
						{
							name = "max",
							definition = {
								type = "int",
								constraint = { type = "range", min = 0, max = 999 },
							},
						},
					},
				},
			},
			default = {
				hp = { min = 30, max = 120 },
				damage = { min = 10, max = 80 },
			},
		},
		{
			-- Table<String, List<Tuple<String, Integer>>> - list of tuples nested under table values
			name = "groupedLabelCounts",
			definition = {
				type = "table",
				keyDefinition = {
					type = "string",
				},
				valueDefinition = {
					type = "list",
					elementDefinition = {
						type = "tuple",
						fields = {
							{ name = "label", definition = { type = "string" } },
							{
								name = "count",
								definition = {
									type = "int",
									constraint = { type = "range", min = 0, max = 99 },
								},
							},
						},
					},
				},
			},
			default = {
				fire = {
					{ label = "ember", count = 2 },
					{ label = "flare", count = 5 },
				},
				water = {
					{ label = "splash", count = 1 },
				},
			},
		},
		{
			-- Tuple<String, Integer> - standalone top level tuple arg
			name = "standaloneLabelCount",
			definition = {
				type = "tuple",
				fields = {
					{ name = "label", definition = { type = "string" } },
					{
						name = "count",
						definition = {
							type = "int",
							constraint = { type = "range", min = 0, max = 99 },
						},
					},
				},
			},
			default = { label = "solo", count = 1 },
		},
	},
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

function module.formatList(list)
	if list == nil then
		return "nil"
	end
	local parts = {}
	for index, value in ipairs(list) do
		if type(value) == "table" then
			parts[#parts + 1] = string.format("[%d]=%s", index, module.formatValue(value))
		else
			parts[#parts + 1] = string.format("[%d]=%s", index, tostring(value))
		end
	end
	return "{" .. table.concat(parts, ", ") .. "}"
end

function module.isList(value)
	if type(value) ~= "table" then
		return false
	end
	if next(value) == nil then
		return true
	end
	local maxIndex = 0
	for key in pairs(value) do
		if type(key) ~= "number" or key < 1 or math.floor(key) ~= key then
			return false
		end
		maxIndex = math.max(maxIndex, key)
	end
	for index = 1, maxIndex do
		if value[index] == nil then
			return false
		end
	end
	return true
end

function module.formatValue(value)
	if type(value) ~= "table" then
		return tostring(value)
	end
	if module.isList(value) then
		return module.formatList(value)
	end
	return module.formatTable(value)
end

function module.formatTable(map)
	if map == nil then
		return "nil"
	end
	local parts = {}
	for key, value in pairs(map) do
		parts[#parts + 1] = string.format("%s=%s", tostring(key), module.formatValue(value))
	end
	return "{" .. table.concat(parts, ", ") .. "}"
end

function module.logArgs(context, args)
	logger.info(
		string.format(
			"dev_test_tuple_args received labelCountList=%s rangeCaps=%s groupedLabelCounts=%s standaloneLabelCount=%s",
			module.formatList(args.labelCountList),
			module.formatTable(args.rangeCaps),
			module.formatTable(args.groupedLabelCounts),
			module.formatTable(args.standaloneLabelCount)
		)
	)
end

return module
