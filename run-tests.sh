#!/bin/bash

# StyleSync API Test Runner Script
# This script provides convenient commands to run different test suites

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required. Found version: $JAVA_VERSION"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
    
    # Check Docker (for Testcontainers)
    if ! command -v docker &> /dev/null; then
        print_warning "Docker is not installed or not running. Testcontainers requires Docker."
        print_warning "Some integration tests may fail."
    else
        if ! docker ps &> /dev/null; then
            print_warning "Docker is not running. Please start Docker."
            print_warning "Some integration tests may fail."
        fi
    fi
    
    print_success "Prerequisites check completed"
}

# Function to run all tests
run_all_tests() {
    print_info "Running all tests..."
    mvn clean test
    print_success "All tests completed"
}

# Function to run only unit tests
run_unit_tests() {
    print_info "Running unit tests..."
    mvn test -Dtest=**/*Test.java
    print_success "Unit tests completed"
}

# Function to run only integration tests
run_integration_tests() {
    print_info "Running integration tests..."
    mvn test -Dtest=**/*IntegrationTest.java
    print_success "Integration tests completed"
}

# Function to run specific test class
run_specific_test() {
    if [ -z "$1" ]; then
        print_error "Please provide a test class name"
        echo "Usage: $0 specific <TestClassName>"
        exit 1
    fi
    
    print_info "Running test: $1"
    mvn test -Dtest="$1"
    print_success "Test completed"
}

# Function to run tests with coverage
run_with_coverage() {
    print_info "Running tests with coverage report..."
    mvn clean test jacoco:report
    
    if [ -f "target/site/jacoco/index.html" ]; then
        print_success "Coverage report generated: target/site/jacoco/index.html"
        
        # Try to open coverage report in browser
        if command -v xdg-open &> /dev/null; then
            xdg-open target/site/jacoco/index.html
        elif command -v open &> /dev/null; then
            open target/site/jacoco/index.html
        fi
    else
        print_warning "Coverage report not found"
    fi
}

# Function to run tests in parallel
run_parallel_tests() {
    THREADS=${1:-4}
    print_info "Running tests in parallel (threads: $THREADS)..."
    mvn clean test -T $THREADS
    print_success "Parallel tests completed"
}

# Function to run tests with detailed output
run_verbose_tests() {
    print_info "Running tests with verbose output..."
    mvn clean test -X
    print_success "Verbose tests completed"
}

# Function to run authentication tests only
run_auth_tests() {
    print_info "Running authentication tests..."
    mvn test -Dtest=AuthenticationIntegrationTest
    print_success "Authentication tests completed"
}

# Function to run wardrobe tests only
run_wardrobe_tests() {
    print_info "Running wardrobe tests..."
    mvn test -Dtest=WardrobeIntegrationTest
    print_success "Wardrobe tests completed"
}

# Function to run outfit tests only
run_outfit_tests() {
    print_info "Running outfit tests..."
    mvn test -Dtest=OutfitIntegrationTest
    print_success "Outfit tests completed"
}

# Function to clean test artifacts
clean_tests() {
    print_info "Cleaning test artifacts..."
    mvn clean
    print_success "Clean completed"
}

# Function to show test report
show_report() {
    if [ -f "target/surefire-reports/index.html" ]; then
        print_info "Opening test report..."
        
        if command -v xdg-open &> /dev/null; then
            xdg-open target/surefire-reports/index.html
        elif command -v open &> /dev/null; then
            open target/surefire-reports/index.html
        else
            print_info "Test report: target/surefire-reports/index.html"
        fi
    else
        print_warning "Test report not found. Run tests first."
    fi
}

# Function to show help
show_help() {
    echo "StyleSync API Test Runner"
    echo ""
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  all              Run all tests (default)"
    echo "  unit             Run only unit tests"
    echo "  integration      Run only integration tests"
    echo "  specific <name>  Run specific test class"
    echo "  coverage         Run tests with coverage report"
    echo "  parallel [n]     Run tests in parallel (default: 4 threads)"
    echo "  verbose          Run tests with detailed output"
    echo "  auth             Run authentication tests only"
    echo "  wardrobe         Run wardrobe tests only"
    echo "  outfit           Run outfit tests only"
    echo "  clean            Clean test artifacts"
    echo "  report           Show test report"
    echo "  check            Check prerequisites"
    echo "  help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all"
    echo "  $0 specific AuthenticationIntegrationTest"
    echo "  $0 coverage"
    echo "  $0 parallel 8"
}

# Main script logic
main() {
    echo "================================================"
    echo "     StyleSync API Test Runner"
    echo "================================================"
    echo ""
    
    COMMAND=${1:-all}
    
    case $COMMAND in
        all)
            check_prerequisites
            run_all_tests
            ;;
        unit)
            check_prerequisites
            run_unit_tests
            ;;
        integration)
            check_prerequisites
            run_integration_tests
            ;;
        specific)
            check_prerequisites
            run_specific_test "$2"
            ;;
        coverage)
            check_prerequisites
            run_with_coverage
            ;;
        parallel)
            check_prerequisites
            run_parallel_tests "$2"
            ;;
        verbose)
            check_prerequisites
            run_verbose_tests
            ;;
        auth)
            check_prerequisites
            run_auth_tests
            ;;
        wardrobe)
            check_prerequisites
            run_wardrobe_tests
            ;;
        outfit)
            check_prerequisites
            run_outfit_tests
            ;;
        clean)
            clean_tests
            ;;
        report)
            show_report
            ;;
        check)
            check_prerequisites
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $COMMAND"
            echo ""
            show_help
            exit 1
            ;;
    esac
    
    echo ""
    echo "================================================"
}

# Run main function
main "$@"
