-- Dev only module used to manually verify the module config UI renders and saves LIST arguments
-- (plain lists, lists with constrained/enum elements, and a nested list of lists)
local module
module = {
	id = "dev_test_list_args",
	name = "[Dev] List Argument Test",
	description = "Exercises list argument types in the config UI",
	seeded = true,
	groups = { "dev" },
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			-- List<String> with ANY elements
			name = "anyStringList",
			definition = {
				type = "list",
				elementDefinition = {
					type = "string",
				},
			},
			default = { "common", "uncommon", "rare" },
		},
		{
			-- List<Integer> with ranged elements
			name = "rangeIntList",
			definition = {
				type = "list",
				elementDefinition = {
					type = "int",
					constraint = { type = "range", min = 1, max = 100 },
				},
			},
			default = { 10, 25, 50 },
		},
		{
			-- List<String> with inline enum constraint on each element
			name = "enumStringList",
			definition = {
				type = "list",
				elementDefinition = {
					type = "string",
					constraint = { type = "enum", values = { "red", "green", "blue" } },
				},
			},
			default = { "red", "blue" },
		},
		{
			-- List<List<Integer>> - a nested list, so each row's own value is itself another
			-- inline list editor
			name = "nestedIntLists",
			definition = {
				type = "list",
				elementDefinition = {
					type = "list",
					elementDefinition = {
						type = "int",
					},
				},
			},
			default = { { 1, 2, 3 }, { 4, 5 } },
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
			parts[#parts + 1] = string.format("[%d]=%s", index, module.formatList(value))
		else
			parts[#parts + 1] = string.format("[%d]=%s", index, tostring(value))
		end
	end
	return "{" .. table.concat(parts, ", ") .. "}"
end

function module.logArgs(context, args)
	logger.info(string.format(
		"dev_test_list_args received anyStringList=%s rangeIntList=%s enumStringList=%s nestedIntLists=%s",
		module.formatList(args.anyStringList),
		module.formatList(args.rangeIntList),
		module.formatList(args.enumStringList),
		module.formatList(args.nestedIntLists)))
end

return module
