@echo off

setlocal enabledelayedexpansion

set SERVICES=gateway user-service product-service order-service payment-service message-service

for %%s in (%SERVICES%) do (
    start "%%s" cmd /c "cd %%s && mvn spring-boot:run"
    timeout /t 5 >nul
)

pause