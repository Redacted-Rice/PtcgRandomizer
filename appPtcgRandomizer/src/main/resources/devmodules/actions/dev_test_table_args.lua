-- Dev only module used to manually verify the module config UI renders and saves TABLE arguments
-- (simple tables, nested list values, and enum keys).
local module
module = {
	id = "dev_test_table_args",
	name = "[Dev] Table Argument Test",
	description = "Exercises table argument types in the config UI",
	seeded = true,
	groups = { "dev" },
	modifies = {},
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	arguments = {
		{
			-- Table<String, Integer> with ranged values
			name = "caps",
			definition = {
				type = "table",
				keyDefinition = {
					type = "string",
				},
				valueDefinition = {
					type = "int",
					constraint = { type = "range", min = 1, max = 999 },
				},
			},
			default = { hp = 120, damage = 80 },
		},
		{
			-- Table<String, List<Integer>> — former group-style pools
			name = "poolsByType",
			definition = {
				type = "table",
				keyDefinition = {
					type = "string",
				},
				valueDefinition = {
					type = "list",
					elementDefinition = {
						type = "int",
					},
				},
			},
			default = {
				fire = { 10, 20, 30 },
				water = { 5, 15 },
			},
		},
		{
			-- Table<CardType, Integer> with Java-registered enum keys
			name = "typeWeights",
			definition = {
				type = "table",
				keyDefinition = {
					type = "enum",
					constraint = "CardType",
				},
				valueDefinition = {
					type = "int",
					constraint = { type = "range", min = 0, max = 10 },
				},
			},
			default = {
				MONSTER_FIRE = 6,
				MONSTER_WATER = 4,
			},
		},
		{
			-- Table<String, Table<String, List<Integer>>> — three levels deep
			name = "nestedGroups",
			definition = {
				type = "table",
				keyDefinition = {
					type = "string",
				},
				valueDefinition = {
					type = "table",
					keyDefinition = {
						type = "string",
					},
					valueDefinition = {
						type = "list",
						elementDefinition = {
							type = "int",
						},
					},
				},
			},
			default = {
				fire = {
					common = { 1, 2, 3 },
					rare = { 10, 20 },
				},
				water = {
					common = { 4, 5 },
				},
			},
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
		parts[#parts + 1] = tostring(value)
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
	logger.info(string.format(
		"dev_test_table_args received caps=%s poolsByType=%s typeWeights=%s nestedGroups=%s",
		module.formatTable(args.caps),
		module.formatTable(args.poolsByType),
		module.formatTable(args.typeWeights),
		module.formatTable(args.nestedGroups)))
end

return module
