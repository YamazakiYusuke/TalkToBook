#!/bin/bash

# TalkToBook Test Reports Cleanup Script
# Keeps the latest 3 test report files/directories and removes older ones

set -e

# Configuration
KEEP_COUNT=3
TEST_REPORTS_DIR="test-reports"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Parse command line arguments
DRY_RUN=false
VERBOSE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -k|--keep)
            KEEP_COUNT="$2"
            shift 2
            ;;
        -d|--directory)
            TEST_REPORTS_DIR="$2"
            shift 2
            ;;
        -n|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Cleanup old test reports, keeping only the latest ones."
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Change to project root
cd "$PROJECT_ROOT"

# Check if test reports directory exists
REPORTS_PATH="$PROJECT_ROOT/$TEST_REPORTS_DIR"
if [ ! -d "$REPORTS_PATH" ]; then
    log_warning "Test reports directory not found: $REPORTS_PATH"
    log_info "Nothing to cleanup"
    exit 0
fi

log_info "Cleaning up test reports in: $REPORTS_PATH"
log_info "Keeping latest $KEEP_COUNT report(s)"

if [ "$DRY_RUN" = true ]; then
    log_info "DRY RUN MODE - No files will be deleted"
fi

# Find and cleanup old test report files
log_files=$(find "$REPORTS_PATH" -name "test-output-*.log" -type f -printf '%T@ %p\n' 2>/dev/null | sort -rn | cut -d' ' -f2- | tail -n +$((KEEP_COUNT + 1)))

if [ -n "$log_files" ]; then
    log_info "Deleting old test output log files:"
    while IFS= read -r file; do
        if [ -n "$file" ]; then
            filename=$(basename "$file")
            if [ "$DRY_RUN" = true ]; then
                echo "  [DRY RUN] Would delete: $filename"
            else
                echo "  Deleting: $filename"
                rm -f "$file"
            fi
        fi
    done <<< "$log_files"
else
    if [ "$VERBOSE" = true ]; then
        log_info "No old test report files to cleanup"
    fi
fi

if [ "$DRY_RUN" = true ]; then
    log_success "Dry run completed - no files were actually deleted"
else
    log_success "Test reports cleanup completed"
fi
exit 0