@echo off
REM StyleSync - Build and Deploy Script for Windows
REM This script builds the project and provides deployment options

echo =========================================
echo   StyleSync - Build and Deploy Script
echo =========================================
echo.

REM Check Java version
echo Checking Java version...
java -version 2>&1 | findstr /C:"version" > nul
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    exit /b 1
)
echo [OK] Java found
echo.

REM Check Maven
echo Checking Maven...
where mvn > nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    exit /b 1
)
echo [OK] Maven found
echo.

REM Check environment variables
if "%MONGODB_URI%"=="" (
    echo Warning: MONGODB_URI environment variable not set
    echo Please set it before running the application:
    echo set MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/
    echo.
)

if "%MONGODB_DATABASE%"=="" (
    echo Warning: MONGODB_DATABASE not set. Using default: stylesync
    set MONGODB_DATABASE=stylesync
)

REM Clean and build
echo Building project...
call mvn clean package

if errorlevel 1 (
    echo [FAILED] Build failed!
    exit /b 1
)

echo [OK] Build successful!
echo.

REM Check WAR file
if not exist "target\stylesync.war" (
    echo Error: WAR file not found in target\stylesync.war
    exit /b 1
)

echo WAR file created: target\stylesync.war
echo.

REM Deployment options
echo =========================================
echo   Deployment Options
echo =========================================
echo 1. Deploy to local Tomcat
echo 2. Run with Docker
echo 3. Exit (manual deployment)
echo.
set /p choice="Choose an option (1-3): "

if "%choice%"=="1" goto tomcat
if "%choice%"=="2" goto docker
if "%choice%"=="3" goto manual
echo Invalid option
exit /b 1

:tomcat
echo.
set /p tomcat_path="Enter Tomcat webapps directory path: "

if exist "%tomcat_path%" (
    echo Copying WAR file to %tomcat_path%...
    copy target\stylesync.war "%tomcat_path%\ROOT.war"
    echo [OK] Deployed to Tomcat!
    echo.
    echo Start Tomcat and access: http://localhost:8080/
) else (
    echo Error: Directory not found: %tomcat_path%
)
goto end

:docker
echo.
echo Building Docker image...
docker build -t stylesync:latest .

if errorlevel 1 (
    echo [FAILED] Docker build failed!
    goto end
)

echo [OK] Docker image built!
echo.

if "%MONGODB_URI%"=="" (
    set /p mongo_uri="Enter MongoDB URI: "
    set MONGODB_URI=%mongo_uri%
)

echo Starting Docker container...
docker run -d -p 8080:8080 ^
    -e MONGODB_URI=%MONGODB_URI% ^
    -e MONGODB_DATABASE=%MONGODB_DATABASE% ^
    --name stylesync ^
    stylesync:latest

echo [OK] Container started!
echo.
echo Application URL: http://localhost:8080/
echo.
echo View logs: docker logs -f stylesync
echo Stop container: docker stop stylesync
echo Remove container: docker rm stylesync
goto end

:manual
echo.
echo =========================================
echo   Manual Deployment Instructions
echo =========================================
echo.
echo Local Tomcat:
echo   1. Copy target\stylesync.war to Tomcat webapps\
echo   2. Set environment variables
echo   3. Start Tomcat
echo   4. Access http://localhost:8080/stylesync
echo.
echo Docker:
echo   docker build -t stylesync .
echo   docker run -p 8080:8080 ^
echo     -e MONGODB_URI="your_uri" ^
echo     stylesync
echo.
echo Cloud (Render/Railway):
echo   1. Push to GitHub
echo   2. Connect repository to platform
echo   3. Add environment variables
echo   4. Deploy
echo.
goto end

:end
echo.
echo =========================================
echo   Environment Variables Required
echo =========================================
echo MONGODB_URI: MongoDB Atlas connection string
echo MONGODB_DATABASE: Database name (default: stylesync)
echo.
echo Set them with:
echo set MONGODB_URI=your_connection_string
echo set MONGODB_DATABASE=stylesync
echo.
echo =========================================
echo   Build Complete!
echo =========================================
pause
