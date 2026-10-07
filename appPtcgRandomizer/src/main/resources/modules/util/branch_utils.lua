-- Helpers for reading evoBranchIds on monster cards / evo lines.
local branch_utils = {}

function branch_utils.hasBranchId(mc, branchId)
	if mc.evoBranchIds == nil then
		return false
	end
	for _, id in ipairs(mc.evoBranchIds) do
		if id == branchId then
			return true
		end
	end
	return false
end

-- Unique branch ids from the line, in first seen order
function branch_utils.branchIds(line)
	local seen = {}
	local ids = {}
	line:each(function(mc)
		if mc.evoBranchIds == nil then
			return
		end
		for _, id in ipairs(mc.evoBranchIds) do
			if not seen[id] then
				seen[id] = true
				table.insert(ids, id)
			end
		end
	end)
	return ids
end

-- Stable key for a card's full branch id set, e.g. {2,1} -> "1,2"
function branch_utils.branchIdsKey(mc)
	if mc.evoBranchIds == nil or #mc.evoBranchIds == 0 then
		return ""
	end
	local ids = {}
	for _, id in ipairs(mc.evoBranchIds) do
		table.insert(ids, id)
	end
	table.sort(ids)
	return table.concat(ids, ",")
end

return branch_utils

