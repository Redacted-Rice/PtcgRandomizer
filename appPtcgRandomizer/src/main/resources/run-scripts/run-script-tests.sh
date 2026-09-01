#!/usr/bin/env bash
set -euo pipefail

# Thin wrapper around: java -jar PtcgRandomizer-*.jar --script-tests [test-file]
#
# Finds the app jar next to this script or in app/, then runs bundled Lua script
# tests. The jar ships script_tests and extracts them when you pass --script-tests.
# Run the app once to install bundled resources (including this wrapper).
#
# Usage:
#   ./run-script-tests.sh                  run every test_*.lua case
#   ./run-script-tests.sh test_set_num_moves    run one case file (.lua is optional)
#
# Exit code is 0 when all cases pass, 1 when any fail, 2 for bad args or setup.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAR=""
for candidate in "$SCRIPT_DIR"/PtcgRandomizer-*.jar; do
	if [[ -f "$candidate" ]]; then
		JAR="$candidate"
		break
	fi
done

if [[ -z "$JAR" && -d "$SCRIPT_DIR/app" ]]; then
	for candidate in "$SCRIPT_DIR/app"/PtcgRandomizer-*.jar; do
		if [[ -f "$candidate" ]]; then
			JAR="$candidate"
			break
		fi
	done
fi

if [[ -z "$JAR" ]]; then
	echo "No PtcgRandomizer-*.jar found next to this script or in app/" >&2
	exit 1
fi

exec java -jar "$JAR" --script-tests "$@"
