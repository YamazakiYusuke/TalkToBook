#!/bin/bash

# TalkToBook Test Execution Script
# Based on test documentation in docs/testing/
# Usage: ./scripts/run-tests.sh [options]

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
REPORTS_DIR="$PROJECT_ROOT/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Default options
RUN_UNIT_TESTS=true
RUN_LINT=true
RUN_CLEAN=false
GENERATE_REPORTS=true
VERBOSE=false
SPECIFIC_TEST=""

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

print_header() {
    echo -e "\n${BLUE}==== $1 ====${NC}"
}

# Function to display usage
show_usage() {
    cat << EOF
TalkToBook Test Execution Script

Usage: $0 [OPTIONS]

OPTIONS:
    -u, --unit-tests     Run unit tests (default: true)
    -l, --lint          Run lint checks (default: true)
    -c, --clean         Run clean before tests (default: false)
    -r, --reports       Generate reports (default: true)
    -v, --verbose       Verbose output (default: false)
    -t, --test CLASS    Run specific test class
    -h, --help          Show this help message
    --no-unit-tests     Skip unit tests
    --no-lint          Skip lint checks
    --no-reports       Skip report generation

Examples:
    $0                                      # Run all tests with default options
    $0 -c -v                               # Clean build with verbose output
    $0 -t "ChapterTest"                    # Run specific test class
    $0 --no-lint                          # Run only unit tests, skip lint
    $0 -u -l -r                           # Explicitly run tests, lint, and generate reports

EOF
}

# Function to setup Android environment
setup_android_environment() {
    print_header "Setting up Android Environment"
    
    # Set environment variables for Docker DevContainer
    export ANDROID_HOME="/opt/android-sdk"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
    
    print_info "Environment variables set:"
    print_info "ANDROID_HOME: $ANDROID_HOME"
    print_info "JAVA_HOME: $JAVA_HOME"
    
    # Verify environment
    print_info "Verifying environment..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java not found. Please install OpenJDK 17."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1)
    print_info "Java: $java_version"
    
    if ! command -v adb &> /dev/null; then
        print_error "ADB not found. Please check Android SDK installation."
        exit 1
    fi
    
    adb_version=$(adb --version | head -n 1)
    print_info "ADB: $adb_version"
    
    if ! command -v sdkmanager &> /dev/null; then
        print_error "SDK Manager not found. Please check Android SDK installation."
        exit 1
    fi
    
    sdk_version=$(sdkmanager --version)
    print_info "SDK Manager: $sdk_version"
    
    print_success "Environment setup completed"
}

# Function to run clean build
run_clean() {
    if [ "$RUN_CLEAN" = true ]; then
        print_header "Running Clean Build"
        print_info "Executing: ./gradlew clean"
        
        if [ "$VERBOSE" = true ]; then
            ./gradlew clean --info
        else
            ./gradlew clean
        fi
        
        print_success "Clean build completed"
    fi
}

# Function to run unit tests
run_unit_tests() {
    if [ "$RUN_UNIT_TESTS" = true ]; then
        print_header "Running Unit Tests"
        
        if [ -n "$SPECIFIC_TEST" ]; then
            print_info "Running specific test: $SPECIFIC_TEST"
            test_command="./gradlew testDebugUnitTest --tests \"*$SPECIFIC_TEST*\""
        else
            print_info "Running all unit tests"
            test_command="./gradlew test"
        fi
        
        print_info "Executing: $test_command"
        
        # Run tests and capture output
        if [ "$VERBOSE" = true ]; then
            eval "$test_command --info" | tee "$REPORTS_DIR/test-output-$TIMESTAMP.log"
        else
            eval "$test_command" | tee "$REPORTS_DIR/test-output-$TIMESTAMP.log"
        fi
        
        # Check test results
        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            print_success "Unit tests completed successfully"
        else
            print_error "Unit tests failed"
            return 1
        fi
    fi
}

# Function to run lint checks
run_lint() {
    if [ "$RUN_LINT" = true ]; then
        print_header "Running Lint Checks"
        print_info "Executing: ./gradlew lint"
        
        if [ "$VERBOSE" = true ]; then
            ./gradlew lint --info | tee "$REPORTS_DIR/lint-output-$TIMESTAMP.log"
        else
            ./gradlew lint | tee "$REPORTS_DIR/lint-output-$TIMESTAMP.log"
        fi
        
        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            print_success "Lint checks completed successfully"
        else
            print_warning "Lint checks completed with warnings/errors"
        fi
    fi
}

# Function to generate and collect reports
generate_reports() {
    if [ "$GENERATE_REPORTS" = true ]; then
        print_header "Generating Test Reports"
        
        # Create reports directory
        mkdir -p "$REPORTS_DIR/$TIMESTAMP"
        
        # Copy test reports if they exist
        if [ -d "app/build/reports/tests" ]; then
            print_info "Copying test reports..."
            cp -r app/build/reports/tests/* "$REPORTS_DIR/$TIMESTAMP/" 2>/dev/null || print_warning "No test reports found"
        fi
        
        # Copy test results if they exist
        if [ -d "app/build/test-results" ]; then
            print_info "Copying test results..."
            mkdir -p "$REPORTS_DIR/$TIMESTAMP/test-results"
            cp -r app/build/test-results/* "$REPORTS_DIR/$TIMESTAMP/test-results/" 2>/dev/null || print_warning "No test results found"
        fi
        
        # Copy lint reports if they exist
        if ls app/build/reports/lint-results-*.* 1> /dev/null 2>&1; then
            print_info "Copying lint reports..."
            cp app/build/reports/lint-results-*.* "$REPORTS_DIR/$TIMESTAMP/" 2>/dev/null || print_warning "No lint reports found"
        fi
        
        # Generate summary report
        generate_summary_report
        
        print_success "Reports generated in: $REPORTS_DIR/$TIMESTAMP"
    fi
}

# Function to generate summary report
generate_summary_report() {
    local summary_file="$REPORTS_DIR/$TIMESTAMP/test-summary.md"
    
    print_info "Generating test summary report..."
    
    cat > "$summary_file" << EOF
# TalkToBook Test Execution Summary

**Execution Date:** $(date)
**Script Version:** 1.0
**Environment:** WSL Ubuntu + Android SDK

## Test Configuration
- Unit Tests: $RUN_UNIT_TESTS
- Lint Checks: $RUN_LINT
- Clean Build: $RUN_CLEAN
- Specific Test: ${SPECIFIC_TEST:-"All tests"}

## Environment Information
EOF
    
    # Add environment info
    echo "- Java Version: $(java -version 2>&1 | head -n 1)" >> "$summary_file"
    echo "- ADB Version: $(adb --version | head -n 1)" >> "$summary_file"
    echo "- Gradle Version: $(./gradlew --version | grep "Gradle" | head -n 1)" >> "$summary_file"
    
    # Add test results if available
    if [ -f "app/build/test-results/testDebugUnitTest/TEST-*.xml" ]; then
        echo -e "\n## Test Results" >> "$summary_file"
        
        local total_tests=0
        local failed_tests=0
        local test_files=(app/build/test-results/testDebugUnitTest/TEST-*.xml)
        
        for test_file in "${test_files[@]}"; do
            if [ -f "$test_file" ]; then
                local tests=$(grep -o 'tests="[0-9]*"' "$test_file" | grep -o '[0-9]*' || echo "0")
                local failures=$(grep -o 'failures="[0-9]*"' "$test_file" | grep -o '[0-9]*' || echo "0")
                total_tests=$((total_tests + tests))
                failed_tests=$((failed_tests + failures))
            fi
        done
        
        echo "- Total Tests: $total_tests" >> "$summary_file"
        echo "- Failed Tests: $failed_tests" >> "$summary_file"
        echo "- Success Rate: $(( (total_tests - failed_tests) * 100 / total_tests ))%" >> "$summary_file"
    fi
    
    # Add file locations
    echo -e "\n## Generated Files" >> "$summary_file"
    echo "- Test Reports: \`app/build/reports/tests/\`" >> "$summary_file"
    echo "- Test Results: \`app/build/test-results/\`" >> "$summary_file"
    echo "- Lint Reports: \`app/build/reports/lint-results-*.*\`" >> "$summary_file"
    echo "- Execution Logs: \`$REPORTS_DIR/\`" >> "$summary_file"
    
    print_info "Summary report created: $summary_file"
}

# Function to display final results
display_results() {
    print_header "Test Execution Results"
    
    # Display test summary if HTML report exists
    if [ -f "app/build/reports/tests/testDebugUnitTest/index.html" ]; then
        print_info "HTML Test Report: app/build/reports/tests/testDebugUnitTest/index.html"
        
        # Extract test counts from HTML report
        if command -v grep &> /dev/null; then
            local test_count=$(grep -o '<div class="counter">[0-9]*</div>' app/build/reports/tests/testDebugUnitTest/index.html | head -n 1 | grep -o '[0-9]*' || echo "N/A")
            local failure_count=$(grep -o '<div class="counter">[0-9]*</div>' app/build/reports/tests/testDebugUnitTest/index.html | sed -n '2p' | grep -o '[0-9]*' || echo "N/A")
            
            if [ "$test_count" != "N/A" ] && [ "$failure_count" != "N/A" ]; then
                print_info "Tests Executed: $test_count"
                print_info "Failures: $failure_count"
                
                if [ "$failure_count" = "0" ]; then
                    print_success "All tests passed! ✅"
                else
                    print_warning "Some tests failed ⚠️"
                fi
            fi
        fi
    fi
    
    # Display lint results if available
    if [ -f "app/build/reports/lint-results-debug.txt" ]; then
        print_info "Lint Report: app/build/reports/lint-results-debug.txt"
        
        local lint_issues=$(grep -c "Warning\|Error" app/build/reports/lint-results-debug.txt 2>/dev/null || echo "0")
        print_info "Lint Issues Found: $lint_issues"
    fi
    
    # Display reports location
    if [ "$GENERATE_REPORTS" = true ]; then
        print_info "All reports collected in: $REPORTS_DIR/$TIMESTAMP"
        print_info "Summary report: $REPORTS_DIR/$TIMESTAMP/test-summary.md"
    fi
    
    print_success "Test execution completed!"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--unit-tests)
            RUN_UNIT_TESTS=true
            shift
            ;;
        --no-unit-tests)
            RUN_UNIT_TESTS=false
            shift
            ;;
        -l|--lint)
            RUN_LINT=true
            shift
            ;;
        --no-lint)
            RUN_LINT=false
            shift
            ;;
        -c|--clean)
            RUN_CLEAN=true
            shift
            ;;
        -r|--reports)
            GENERATE_REPORTS=true
            shift
            ;;
        --no-reports)
            GENERATE_REPORTS=false
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -t|--test)
            SPECIFIC_TEST="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main execution
main() {
    print_header "TalkToBook Test Execution Script"
    print_info "Timestamp: $TIMESTAMP"
    
    # Change to project root directory
    cd "$PROJECT_ROOT" || {
        print_error "Failed to change to project root directory: $PROJECT_ROOT"
        exit 1
    }
    
    print_info "Working directory: $(pwd)"
    
    # Create reports directory
    mkdir -p "$REPORTS_DIR"
    
    # Execute test phases
    setup_android_environment
    run_clean
    run_unit_tests
    run_lint
    generate_reports
    display_results
    
    print_success "Script execution completed successfully!"
}

# Error handling
trap 'print_error "Script failed at line $LINENO"' ERR

# Run main function
main "$@"