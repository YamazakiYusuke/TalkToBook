#!/bin/bash

# TalkToBook Quick Test Script
# A simplified version for rapid testing
# Usage: ./scripts/quick-test.sh [test-type]

set -e

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Setup environment
setup_env() {
    export ANDROID_HOME="$HOME/android"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
}

# Quick test functions
quick_unit_test() {
    print_info "Running quick unit tests..."
    ./gradlew testDebugUnitTest
    print_success "Unit tests completed"
}

quick_lint() {
    print_info "Running quick lint check..."
    ./gradlew lintDebug
    print_success "Lint check completed"
}

quick_all() {
    print_info "Running all quick tests..."
    ./gradlew test lint
    print_success "All tests completed"
}

# Main execution
case "${1:-all}" in
    "unit"|"u")
        setup_env && quick_unit_test
        ;;
    "lint"|"l")
        setup_env && quick_lint
        ;;
    "all"|"a")
        setup_env && quick_all
        ;;
    *)
        echo "Usage: $0 [unit|lint|all]"
        echo "  unit: Run unit tests only"
        echo "  lint: Run lint checks only"
        echo "  all:  Run both (default)"
        exit 1
        ;;
esac