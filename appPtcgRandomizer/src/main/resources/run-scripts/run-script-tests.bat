@echo off
setlocal
REM Thin wrapper around: java -jar PtcgRandomizer-*.jar --script-tests [test-file]
REM
REM Finds the app jar next to this script or in app\, then runs bundled Lua script
REM tests. The jar ships script_tests and extracts them when you pass --script-tests.
REM Run the app once to install bundled resources (including this wrapper).
REM
REM Usage:
REM   run-script-tests.bat                  run every test_*.lua case
REM   run-script-tests.bat test_set_num_moves    run one case file (.lua is optional)
REM   run-script-tests.bat --log-level INFO      run all with more logging
REM   run-script-tests.bat --log-level DEBUG test_set_num_moves
REM
REM Log level defaults to WARN. Use DEBUG, INFO, WARN, or ERROR.
REM
REM Exit code is 0 when all cases pass, 1 when any fail, 2 for bad args or setup.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "JAR="
for %%f in ("PtcgRandomizer-*.jar") do (
	if exist "%%~f" set "JAR=%%~f"
)

if not defined JAR (
	if exist "app\" (
		cd /d "%SCRIPT_DIR%app"
		for %%f in ("PtcgRandomizer-*.jar") do (
			if exist "%%~f" set "JAR=%%~f"
		)
	)
)

if not defined JAR (
	echo No PtcgRandomizer-*.jar found next to this script or in app\
	exit /b 1
)

java -jar "%JAR%" --script-tests %*
exit /b %ERRORLEVEL%
