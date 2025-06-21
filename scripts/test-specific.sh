#!/bin/bash

# test-specific.sh - Run specific test classes
# Usage: ./scripts/test-specific.sh [TEST_CLASS_PATTERN]
# Example: ./scripts/test-specific.sh DocumentRepository
# Example: ./scripts/test-specific.sh "*ViewModel*"

echo "🎯 Running TalkToBook Specific Tests"
echo "===================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if we're in the project root
if [ ! -f "gradlew" ]; then
    echo -e "${RED}Error: gradlew not found. Please run this script from the project root.${NC}"
    exit 1
fi

# Check if test pattern is provided
if [ -z "$1" ]; then
    echo -e "${YELLOW}Usage: $0 [TEST_CLASS_PATTERN]${NC}"
    echo -e "${BLUE}Examples:${NC}"
    echo -e "  $0 DocumentRepository"
    echo -e "  $0 \"*ViewModel*\""
    echo -e "  $0 TranscriptionUseCase"
    echo ""
    echo -e "${BLUE}Available test classes:${NC}"
    find app/src/test -name "*.kt" -exec basename {} .kt \; | sort | sed 's/^/  /'
    exit 1
fi

TEST_PATTERN="$1"

echo -e "${YELLOW}Running tests matching pattern: ${BLUE}$TEST_PATTERN${NC}"

# Run specific tests
./gradlew test --tests "*$TEST_PATTERN*" --no-build-cache
TEST_EXIT_CODE=$?

# Show summary
echo -e "\n===================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "✅ Tests matching '$TEST_PATTERN': ${GREEN}PASSED${NC}"
else
    echo -e "❌ Tests matching '$TEST_PATTERN': ${RED}FAILED${NC}"
fi
echo -e "===================================="

exit $TEST_EXIT_CODE