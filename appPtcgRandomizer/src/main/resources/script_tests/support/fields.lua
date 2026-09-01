-- Field spec helpers for script tests. Not a test file. require("support.fields")
local fields = {}

function fields.moves(entries)
	return {
		accessType = "item",
		values = entries or {},
	}
end

function fields.costs(entries)
	local spec = {
		accessType = "item",
		pre = "clearCosts",
	}
	if entries then
		for key, value in pairs(entries) do
			spec[key] = value
		end
	end
	return spec
end

return fields
