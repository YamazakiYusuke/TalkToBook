#!/bin/bash

# quick-test.sh - Fast test execution for development
# This script runs only unit tests without reports for quick feedback

echo "⚡ Running TalkToBook Quick Tests"
echo "================================="

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

# Run unit tests only (no coverage, no reports)
echo -e "${YELLOW}Running unit tests...${NC}"
./gradlew test --no-build-cache --rerun-tasks
TEST_EXIT_CODE=$?

# Show summary
echo -e "\n================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "✅ Unit tests: ${GREEN}PASSED${NC}"
    echo -e "\n${GREEN}All tests passed!${NC}"
else
    echo -e "❌ Unit tests: ${RED}FAILED${NC}"
    echo -e "\n${RED}Tests failed! Check the output above for details.${NC}"
    echo -e "For detailed report, run: ${YELLOW}./scripts/run-tests.sh${NC}"
fi
echo -e "================================="

exit $TEST_EXIT_CODE