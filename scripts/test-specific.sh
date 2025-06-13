#!/bin/bash

# TalkToBook Specific Test Execution Script
# Usage: ./scripts/test-specific.sh [test-class-name]

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Setup Android environment
setup_environment() {
    export ANDROID_HOME="$HOME/android"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
}

# Function to show available test classes
show_available_tests() {
    print_info "Available test classes:"
    find app/src/test -name "*Test.kt" | sed 's|app/src/test/java/||; s|/|.|g; s|.kt||' | sort
}

# Function to run specific test
run_specific_test() {
    local test_class="$1"
    
    if [ -z "$test_class" ]; then
        print_error "No test class specified"
        show_available_tests
        exit 1
    fi
    
    # Add package prefix if not provided
    if [[ "$test_class" != *"com.example.talktobook"* ]]; then
        test_class="com.example.talktobook.domain.model.$test_class"
    fi
    
    print_info "Running test class: $test_class"
    
    # Run the specific test
    if ./gradlew testDebugUnitTest --tests "$test_class" --info; then
        print_success "Test completed successfully"
        
        # Show test results if available
        if [ -f "app/build/reports/tests/testDebugUnitTest/index.html" ]; then
            print_info "Test report available at: app/build/reports/tests/testDebugUnitTest/index.html"
        fi
    else
        print_error "Test failed"
        exit 1
    fi
}

# Main execution
main() {
    print_info "TalkToBook Specific Test Runner"
    
    setup_environment
    
    case "${1:-help}" in
        "help"|"-h"|"--help")
            echo "Usage: $0 [test-class-name]"
            echo ""
            echo "Examples:"
            echo "  $0 ChapterTest"
            echo "  $0 DocumentTest"
            echo "  $0 RecordingTest"
            echo "  $0 com.example.talktobook.domain.model.ChapterTest"
            echo ""
            show_available_tests
            ;;
        *)
            run_specific_test "$1"
            ;;
    esac
}

main "$@"