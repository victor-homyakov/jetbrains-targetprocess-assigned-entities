@echo off
rem Install ruby gem:
rem gem install github_changelog_generator

if "%1"=="" goto help
github_changelog_generator.bat --future-release %1
goto end

:help
echo Usage: %~nx0 future-release-name
echo Example: %~nx0 v1.2
goto end

:end
