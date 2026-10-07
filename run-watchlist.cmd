@echo off
rem Runs the whole watchlist in auto tier. Meant for Windows Task Scheduler (see README).
rem Needs ANTHROPIC_API_KEY (and ANTHROPIC_WORKSPACE_ID, if your key needs one) set as user environment variables.
cd /d "%~dp0"
java -jar target\stock-agent-1.0.0.jar --watchlist --config src\main\resources\config.yaml %*
