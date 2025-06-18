#!/bin/bash

# run-tests.sh - Comprehensive test suite with reports
# This script runs all tests and generates comprehensive reports

echo "🧪 Running TalkToBook Comprehensive Test Suite"
echo "============================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the project root
if [ ! -f "gradlew" ]; then
    echo -e "${RED}Error: gradlew not found. Please run this script from the project root.${NC}"
    exit 1
fi

# Clean previous build artifacts
echo -e "\n${YELLOW}Cleaning previous build artifacts...${NC}"
./gradlew clean

# Run lint checks
echo -e "\n${YELLOW}Running lint checks...${NC}"
./gradlew lint
LINT_EXIT_CODE=$?

# Run unit tests with coverage
echo -e "\n${YELLOW}Running unit tests with coverage...${NC}"
./gradlew test jacocoTestReport
TEST_EXIT_CODE=$?

# Check if device/emulator is connected for instrumented tests
ADB_DEVICES=$(adb devices | grep -E "device$|emulator" | wc -l)
if [ $ADB_DEVICES -gt 0 ]; then
    echo -e "\n${YELLOW}Running instrumented tests...${NC}"
    ./gradlew connectedAndroidTest
    INSTRUMENTED_EXIT_CODE=$?
else
    echo -e "\n${YELLOW}Skipping instrumented tests - no device/emulator connected${NC}"
    INSTRUMENTED_EXIT_CODE=0
fi

# Generate test reports
echo -e "\n${YELLOW}Generating test reports...${NC}"

# Unit test report location
UNIT_TEST_REPORT="app/build/reports/tests/testDebugUnitTest/index.html"
COVERAGE_REPORT="app/build/reports/jacoco/jacocoTestReport/html/index.html"
LINT_REPORT="app/build/reports/lint-results-debug.html"

# Summary
echo -e "\n============================================="
echo -e "📊 TEST SUMMARY"
echo -e "============================================="

if [ $LINT_EXIT_CODE -eq 0 ]; then
    echo -e "✅ Lint checks: ${GREEN}PASSED${NC}"
else
    echo -e "❌ Lint checks: ${RED}FAILED${NC}"
fi

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "✅ Unit tests: ${GREEN}PASSED${NC}"
else
    echo -e "❌ Unit tests: ${RED}FAILED${NC}"
fi

if [ $ADB_DEVICES -gt 0 ]; then
    if [ $INSTRUMENTED_EXIT_CODE -eq 0 ]; then
        echo -e "✅ Instrumented tests: ${GREEN}PASSED${NC}"
    else
        echo -e "❌ Instrumented tests: ${RED}FAILED${NC}"
    fi
fi

echo -e "\n📁 REPORT LOCATIONS:"
echo -e "- Unit test report: $UNIT_TEST_REPORT"
echo -e "- Coverage report: $COVERAGE_REPORT"
echo -e "- Lint report: $LINT_REPORT"

# Calculate overall exit code
OVERALL_EXIT_CODE=$((LINT_EXIT_CODE + TEST_EXIT_CODE + INSTRUMENTED_EXIT_CODE))

# Open reports in browser if on macOS and all tests passed
if [ $OVERALL_EXIT_CODE -eq 0 ] && [ "$(uname)" == "Darwin" ]; then
    echo -e "\n${GREEN}All tests passed! Opening reports in browser...${NC}"
    open "$UNIT_TEST_REPORT" 2>/dev/null || true
    open "$COVERAGE_REPORT" 2>/dev/null || true
fi

echo -e "\n============================================="
echo -e "Test run completed at: $(date)"
echo -e "============================================="

exit $OVERALL_EXIT_CODE