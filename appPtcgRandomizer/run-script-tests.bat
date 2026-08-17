@echo off
setlocal
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
