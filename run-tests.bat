@echo off
REM StyleSync API Test Runner Script for Windows
REM This script provides convenient commands to run different test suites

setlocal enabledelayedexpansion

set "COMMAND=%~1"
if "%COMMAND%"=="" set "COMMAND=all"

echo ================================================
echo      StyleSync API Test Runner
echo ================================================
echo.

REM Check prerequisites
call :check_prerequisites
if errorlevel 1 exit /b 1

REM Execute command
if "%COMMAND%"=="all" goto run_all_tests
if "%COMMAND%"=="unit" goto run_unit_tests
if "%COMMAND%"=="integration" goto run_integration_tests
if "%COMMAND%"=="specific" goto run_specific_test
if "%COMMAND%"=="coverage" goto run_with_coverage
if "%COMMAND%"=="parallel" goto run_parallel_tests
if "%COMMAND%"=="verbose" goto run_verbose_tests
if "%COMMAND%"=="auth" goto run_auth_tests
if "%COMMAND%"=="wardrobe" goto run_wardrobe_tests
if "%COMMAND%"=="outfit" goto run_outfit_tests
if "%COMMAND%"=="clean" goto clean_tests
if "%COMMAND%"=="report" goto show_report
if "%COMMAND%"=="check" goto end
if "%COMMAND%"=="help" goto show_help
if "%COMMAND%"=="--help" goto show_help
if "%COMMAND%"=="-h" goto show_help

echo [ERROR] Unknown command: %COMMAND%
echo.
goto show_help

:check_prerequisites
echo [INFO] Checking prerequisites...

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java is not installed. Please install Java 17 or higher.
    exit /b 1
)

REM Check Maven
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven is not installed. Please install Maven 3.6 or higher.
    exit /b 1
)

REM Check Docker
where docker >nul 2>nul
if errorlevel 1 (
    echo [WARNING] Docker is not installed. Testcontainers requires Docker.
    echo [WARNING] Some integration tests may fail.
) else (
    docker ps >nul 2>nul
    if errorlevel 1 (
        echo [WARNING] Docker is not running. Please start Docker.
        echo [WARNING] Some integration tests may fail.
    )
)

echo [SUCCESS] Prerequisites check completed
echo.
goto :eof

:run_all_tests
echo [INFO] Running all tests...
call mvn clean test
if errorlevel 1 (
    echo [ERROR] Tests failed
    exit /b 1
)
echo [SUCCESS] All tests completed
goto end

:run_unit_tests
echo [INFO] Running unit tests...
call mvn test -Dtest=**/*Test.java
if errorlevel 1 (
    echo [ERROR] Unit tests failed
    exit /b 1
)
echo [SUCCESS] Unit tests completed
goto end

:run_integration_tests
echo [INFO] Running integration tests...
call mvn test -Dtest=**/*IntegrationTest.java
if errorlevel 1 (
    echo [ERROR] Integration tests failed
    exit /b 1
)
echo [SUCCESS] Integration tests completed
goto end

:run_specific_test
if "%~2"=="" (
    echo [ERROR] Please provide a test class name
    echo Usage: %0 specific TestClassName
    exit /b 1
)
echo [INFO] Running test: %~2
call mvn test -Dtest=%~2
if errorlevel 1 (
    echo [ERROR] Test failed
    exit /b 1
)
echo [SUCCESS] Test completed
goto end

:run_with_coverage
echo [INFO] Running tests with coverage report...
call mvn clean test jacoco:report
if errorlevel 1 (
    echo [ERROR] Tests with coverage failed
    exit /b 1
)

if exist "target\site\jacoco\index.html" (
    echo [SUCCESS] Coverage report generated: target\site\jacoco\index.html
    start target\site\jacoco\index.html
) else (
    echo [WARNING] Coverage report not found
)
goto end

:run_parallel_tests
set "THREADS=%~2"
if "%THREADS%"=="" set "THREADS=4"
echo [INFO] Running tests in parallel (threads: %THREADS%)...
call mvn clean test -T %THREADS%
if errorlevel 1 (
    echo [ERROR] Parallel tests failed
    exit /b 1
)
echo [SUCCESS] Parallel tests completed
goto end

:run_verbose_tests
echo [INFO] Running tests with verbose output...
call mvn clean test -X
if errorlevel 1 (
    echo [ERROR] Verbose tests failed
    exit /b 1
)
echo [SUCCESS] Verbose tests completed
goto end

:run_auth_tests
echo [INFO] Running authentication tests...
call mvn test -Dtest=AuthenticationIntegrationTest
if errorlevel 1 (
    echo [ERROR] Authentication tests failed
    exit /b 1
)
echo [SUCCESS] Authentication tests completed
goto end

:run_wardrobe_tests
echo [INFO] Running wardrobe tests...
call mvn test -Dtest=WardrobeIntegrationTest
if errorlevel 1 (
    echo [ERROR] Wardrobe tests failed
    exit /b 1
)
echo [SUCCESS] Wardrobe tests completed
goto end

:run_outfit_tests
echo [INFO] Running outfit tests...
call mvn test -Dtest=OutfitIntegrationTest
if errorlevel 1 (
    echo [ERROR] Outfit tests failed
    exit /b 1
)
echo [SUCCESS] Outfit tests completed
goto end

:clean_tests
echo [INFO] Cleaning test artifacts...
call mvn clean
if errorlevel 1 (
    echo [ERROR] Clean failed
    exit /b 1
)
echo [SUCCESS] Clean completed
goto end

:show_report
if exist "target\surefire-reports\index.html" (
    echo [INFO] Opening test report...
    start target\surefire-reports\index.html
) else (
    echo [WARNING] Test report not found. Run tests first.
)
goto end

:show_help
echo StyleSync API Test Runner
echo.
echo Usage: %0 [command] [options]
echo.
echo Commands:
echo   all              Run all tests (default)
echo   unit             Run only unit tests
echo   integration      Run only integration tests
echo   specific ^<name^>  Run specific test class
echo   coverage         Run tests with coverage report
echo   parallel [n]     Run tests in parallel (default: 4 threads)
echo   verbose          Run tests with detailed output
echo   auth             Run authentication tests only
echo   wardrobe         Run wardrobe tests only
echo   outfit           Run outfit tests only
echo   clean            Clean test artifacts
echo   report           Show test report
echo   check            Check prerequisites
echo   help             Show this help message
echo.
echo Examples:
echo   %0 all
echo   %0 specific AuthenticationIntegrationTest
echo   %0 coverage
echo   %0 parallel 8
goto end

:end
echo.
echo ================================================
endlocal
