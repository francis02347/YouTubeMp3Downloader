@echo off
title Actualizador Automatico YouTubeMp3Downloader
:: Ejecuta el script de PowerShell saltándose las restricciones de ejecución de Windows
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0actualizar.ps1"
pause
