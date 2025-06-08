@echo off

setlocal enabledelayedexpansion

set YOUR_DOCKER_SERVER_IP=39.108.112.191

set SERVICES=gateway user-service product-service order-service payment-service message-service

for %%s in (%SERVICES%) do (
    start "%%s" cmd /c "mvn -pl %%s spring-boot:run"
    timeout /t 5 >nul
)

pause