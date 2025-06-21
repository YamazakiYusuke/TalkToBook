#!/bin/bash

# Golden Test Kit - Test Execution Script
# This script runs golden tests with various configurations and options

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REPORTS_DIR="test-reports/golden-tests"
LOG_FILE="$REPORTS_DIR/golden-test-$(date +%Y%m%d_%H%M%S).log"

# Help function
show_help() {
    echo "Golden Test Kit - Test Execution Script"
    echo ""
    echo "Usage: $0 [OPTIONS] [TEST_PATTERN]"
    echo ""
    echo "OPTIONS:"
    echo "  -h, --help              Show this help message"
    echo "  -a, --all               Run all golden tests"
    echo "  -s, --screen SCREEN     Run tests for specific screen (main, recording, document-list)"
    echo "  -d, --device DEVICE     Run tests for specific device (phone, tablet)"
    echo "  -t, --theme THEME       Run tests for specific theme (light, dark, elderly)"
    echo "  -c, --config CONFIG     Run tests for specific accessibility config (wcag, elderly, relaxed)"
    echo "  -f, --fast              Run only essential test combinations"
    echo "  -v, --verbose           Enable verbose output"
    echo "  --generate-report       Generate HTML test report"
    echo "  --update-goldens        Update golden images after test run"
    echo "  --fail-fast             Stop on first test failure"
    echo ""
    echo "EXAMPLES:"
    echo "  $0 --all                                     # Run all golden tests"
    echo "  $0 --screen main --theme elderly            # Run main screen elderly theme tests"
    echo "  $0 --device tablet --fast                   # Run essential tablet tests"
    echo "  $0 --config wcag --generate-report          # Run WCAG tests with report"
    echo "  $0 MainScreenGoldenTest                      # Run specific test class"
    echo ""
}

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Error handling
error() {
    echo -e "${RED}ERROR: $1${NC}" >&2
    log "ERROR: $1"
    exit 1
}

# Success message
success() {
    echo -e "${GREEN}$1${NC}"
    log "SUCCESS: $1"
}

# Warning message
warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
    log "WARNING: $1"
}

# Info message
info() {
    echo -e "${BLUE}INFO: $1${NC}"
    log "INFO: $1"
}

# Setup test environment
setup_test_environment() {
    # Create reports directory
    mkdir -p "$REPORTS_DIR"
    
    # Clear previous test results
    rm -rf app/build/outputs/androidTest-results/
    
    # Ensure device is connected
    if ! adb devices | grep -q "device$"; then
        error "No Android device connected. Please connect a device or start an emulator."
    fi
    
    info "Test environment setup complete"
}

# Build test arguments based on filters
build_test_args() {
    local screen="$1"
    local device="$2"
    local theme="$3"
    local config="$4"
    local fast="$5"
    local test_pattern="$6"
    
    local test_args=""
    
    # Build package filter
    local package_filter="com.example.talktobook.golden"
    
    if [ -n "$screen" ]; then
        case $screen in
            main)
                package_filter="$package_filter.screens.MainScreen*"
                ;;
            recording)
                package_filter="$package_filter.screens.RecordingScreen*"
                ;;
            document-list)
                package_filter="$package_filter.screens.DocumentListScreen*"
                ;;
            *)
                warning "Unknown screen: $screen"
                ;;
        esac
    fi
    
    # Add test pattern if specified
    if [ -n "$test_pattern" ]; then
        package_filter="$package_filter*$test_pattern*"
    fi
    
    test_args="-Pandroid.testInstrumentationRunnerArguments.package=$package_filter"
    
    # Add specific test configurations if needed
    if [ "$fast" = "true" ]; then
        test_args="$test_args -Pandroid.testInstrumentationRunnerArguments.notAnnotation=com.example.talktobook.golden.SlowTest"
    fi
    
    echo "$test_args"
}

# Run golden tests
run_golden_tests() {
    local screen="$1"
    local device="$2"
    local theme="$3"
    local config="$4"
    local fast="$5"
    local verbose="$6"
    local fail_fast="$7"
    local test_pattern="$8"
    
    info "Running golden tests..."
    log "Screen: $screen, Device: $device, Theme: $theme, Config: $config, Fast: $fast"
    
    # Build test arguments
    local test_args=$(build_test_args "$screen" "$device" "$theme" "$config" "$fast" "$test_pattern")
    
    # Build gradle command
    local gradle_cmd="./gradlew connectedAndroidTest"
    
    if [ -n "$test_args" ]; then
        gradle_cmd="$gradle_cmd $test_args"
    fi
    
    # Add verbose output if requested
    if [ "$verbose" = "true" ]; then
        gradle_cmd="$gradle_cmd --info"
    fi
    
    # Add fail fast if requested
    if [ "$fail_fast" = "true" ]; then
        gradle_cmd="$gradle_cmd --fail-fast"
    fi
    
    # Run tests
    info "Executing: $gradle_cmd"
    
    if eval "$gradle_cmd"; then
        success "Golden tests completed successfully"
        return 0
    else
        error "Golden tests failed"
        return 1
    fi
}

# Generate HTML test report
generate_html_report() {
    info "Generating HTML test report..."
    
    local report_template="$REPORTS_DIR/golden-test-report.html"
    
    cat > "$report_template" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Golden Test Kit Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f5f5f5; padding: 20px; border-radius: 5px; }
        .test-result { margin: 10px 0; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
        .success { background: #d4edda; border-color: #c3e6cb; }
        .failure { background: #f8d7da; border-color: #f5c6cb; }
        .image-comparison { display: flex; gap: 10px; margin: 10px 0; }
        .image-comparison img { max-width: 300px; border: 1px solid #ddd; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Golden Test Kit Report</h1>
        <p>Generated on: <span id="timestamp"></span></p>
        <p>TalkToBook Application - Visual Regression Test Results</p>
    </div>
    
    <div id="summary">
        <h2>Test Summary</h2>
        <p>Total Tests: <span id="total-tests">0</span></p>
        <p>Passed: <span id="passed-tests">0</span></p>
        <p>Failed: <span id="failed-tests">0</span></p>
    </div>
    
    <div id="test-results">
        <h2>Test Results</h2>
        <!-- Test results will be populated here -->
    </div>
    
    <script>
        document.getElementById('timestamp').textContent = new Date().toLocaleString();
        
        // Parse test results from Android test output
        // This would be populated by parsing the actual test results
        function populateResults() {
            // Implementation would parse XML/JSON test results
            // and populate the HTML with actual test data
        }
        
        populateResults();
    </script>
</body>
</html>
EOF
    
    success "HTML report template created: $report_template"
}

# Update golden images after test run
update_golden_images_after_test() {
    info "Updating golden images..."
    
    if [ -f "scripts/golden-update.sh" ]; then
        bash scripts/golden-update.sh --update --confirm
    else
        warning "Golden update script not found"
    fi
}

# Main function
main() {
    local action="run"
    local screen=""
    local device=""
    local theme=""
    local config=""
    local fast="false"
    local verbose="false"
    local generate_report="false"
    local update_goldens="false"
    local fail_fast="false"
    local test_pattern=""
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -a|--all)
                action="all"
                shift
                ;;
            -s|--screen)
                screen="$2"
                shift 2
                ;;
            -d|--device)
                device="$2"
                shift 2
                ;;
            -t|--theme)
                theme="$2"
                shift 2
                ;;
            -c|--config)
                config="$2"
                shift 2
                ;;
            -f|--fast)
                fast="true"
                shift
                ;;
            -v|--verbose)
                verbose="true"
                shift
                ;;
            --generate-report)
                generate_report="true"
                shift
                ;;
            --update-goldens)
                update_goldens="true"
                shift
                ;;
            --fail-fast)
                fail_fast="true"
                shift
                ;;
            *)
                if [ -z "$test_pattern" ]; then
                    test_pattern="$1"
                fi
                shift
                ;;
        esac
    done
    
    # Start logging
    log "Golden Test Kit Execution Started"
    log "Screen: $screen, Device: $device, Theme: $theme, Config: $config"
    
    # Setup environment
    setup_test_environment
    
    # Run tests
    if run_golden_tests "$screen" "$device" "$theme" "$config" "$fast" "$verbose" "$fail_fast" "$test_pattern"; then
        success "All tests completed successfully"
        
        # Generate report if requested
        if [ "$generate_report" = "true" ]; then
            generate_html_report
        fi
        
        # Update goldens if requested
        if [ "$update_goldens" = "true" ]; then
            update_golden_images_after_test
        fi
    else
        error "Tests failed"
    fi
    
    log "Golden Test Kit Execution Completed"
}

# Run main function with all arguments
main "$@"