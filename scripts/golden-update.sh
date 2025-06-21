#!/bin/bash

# Golden Test Kit - Update Golden Images Script
# This script helps manage golden image updates for the TalkToBook application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
GOLDEN_DIR="app/src/androidTest/goldens"
DEVICE_GOLDEN_DIR="app/build/outputs/androidTest-results/golden_images"
LOG_FILE="golden-update-$(date +%Y%m%d_%H%M%S).log"

# Help function
show_help() {
    echo "Golden Test Kit - Update Golden Images"
    echo ""
    echo "Usage: $0 [OPTIONS] [TEST_NAME_PATTERN]"
    echo ""
    echo "OPTIONS:"
    echo "  -h, --help              Show this help message"
    echo "  -l, --list              List all available golden images"
    echo "  -u, --update            Update golden images from latest test run"
    echo "  -t, --test PATTERN      Update only golden images matching pattern"
    echo "  -d, --device DEVICE     Update only for specific device configuration"
    echo "  -c, --confirm           Skip confirmation prompts"
    echo "  -b, --backup            Create backup before updating"
    echo "  --dry-run               Show what would be updated without making changes"
    echo ""
    echo "EXAMPLES:"
    echo "  $0 --list                                    # List all golden images"
    echo "  $0 --update --confirm                        # Update all golden images"
    echo "  $0 --test main_screen --device phone_normal  # Update main screen for phone"
    echo "  $0 --backup --update                         # Create backup and update all"
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

# Check if git LFS is installed and initialized
check_git_lfs() {
    if ! command -v git-lfs &> /dev/null; then
        error "Git LFS is not installed. Please install it first."
    fi
    
    if ! git lfs ls-files &> /dev/null; then
        warning "Git LFS not initialized in this repository"
        info "Initializing Git LFS..."
        git lfs install
    fi
}

# List all golden images
list_golden_images() {
    info "Listing all golden images..."
    
    if [ -d "$GOLDEN_DIR" ]; then
        find "$GOLDEN_DIR" -name "*.png" | sort
    else
        warning "Golden images directory not found: $GOLDEN_DIR"
    fi
    
    if [ -d "$DEVICE_GOLDEN_DIR" ]; then
        echo ""
        info "Device golden images from last test run:"
        find "$DEVICE_GOLDEN_DIR" -name "*.png" | sort
    else
        warning "Device golden images directory not found: $DEVICE_GOLDEN_DIR"
    fi
}

# Create backup of golden images
create_backup() {
    if [ ! -d "$GOLDEN_DIR" ]; then
        warning "No golden images to backup"
        return 0
    fi
    
    local backup_dir="goldens-backup-$(date +%Y%m%d_%H%M%S)"
    info "Creating backup: $backup_dir"
    
    cp -r "$GOLDEN_DIR" "$backup_dir"
    success "Backup created: $backup_dir"
}

# Update golden images
update_golden_images() {
    local test_pattern="$1"
    local device_pattern="$2"
    local dry_run="$3"
    local confirm="$4"
    
    if [ ! -d "$DEVICE_GOLDEN_DIR" ]; then
        error "Device golden images not found. Run golden tests first."
    fi
    
    # Find matching images
    local find_cmd="find $DEVICE_GOLDEN_DIR -name '*.png'"
    
    if [ -n "$test_pattern" ]; then
        find_cmd="$find_cmd | grep '$test_pattern'"
    fi
    
    if [ -n "$device_pattern" ]; then
        find_cmd="$find_cmd | grep '$device_pattern'"
    fi
    
    local images=$(eval "$find_cmd")
    
    if [ -z "$images" ]; then
        warning "No golden images found matching criteria"
        return 0
    fi
    
    echo ""
    info "Images to update:"
    echo "$images"
    echo ""
    
    if [ "$confirm" != "true" ] && [ "$dry_run" != "true" ]; then
        read -p "Continue with update? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            info "Update cancelled"
            return 0
        fi
    fi
    
    if [ "$dry_run" = "true" ]; then
        info "DRY RUN - Would update $(echo "$images" | wc -l) images"
        return 0
    fi
    
    # Ensure golden directory exists
    mkdir -p "$GOLDEN_DIR"
    
    local count=0
    while IFS= read -r image_path; do
        if [ -n "$image_path" ]; then
            # Extract relative path from device golden dir
            local rel_path=${image_path#$DEVICE_GOLDEN_DIR/}
            local dest_path="$GOLDEN_DIR/$rel_path"
            
            # Create destination directory
            mkdir -p "$(dirname "$dest_path")"
            
            # Copy image
            cp "$image_path" "$dest_path"
            info "Updated: $rel_path"
            ((count++))
        fi
    done <<< "$images"
    
    success "Updated $count golden images"
}

# Run golden tests
run_golden_tests() {
    local test_pattern="$1"
    
    info "Running golden tests..."
    
    local gradle_cmd="./gradlew connectedAndroidTest"
    
    if [ -n "$test_pattern" ]; then
        gradle_cmd="$gradle_cmd -Pandroid.testInstrumentationRunnerArguments.class=com.example.talktobook.golden.screens.*$test_pattern*"
    fi
    
    # Run tests with golden test classes
    gradle_cmd="$gradle_cmd -Pandroid.testInstrumentationRunnerArguments.package=com.example.talktobook.golden"
    
    eval "$gradle_cmd" || warning "Some golden tests failed, but continuing with update process"
}

# Main function
main() {
    local action=""
    local test_pattern=""
    local device_pattern=""
    local dry_run="false"
    local confirm="false"
    local backup="false"
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -l|--list)
                action="list"
                shift
                ;;
            -u|--update)
                action="update"
                shift
                ;;
            -t|--test)
                test_pattern="$2"
                shift 2
                ;;
            -d|--device)
                device_pattern="$2"
                shift 2
                ;;
            -c|--confirm)
                confirm="true"
                shift
                ;;
            -b|--backup)
                backup="true"
                shift
                ;;
            --dry-run)
                dry_run="true"
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
    
    # Default action
    if [ -z "$action" ]; then
        action="update"
    fi
    
    # Start logging
    log "Golden Test Kit Update Script Started"
    log "Action: $action, Test: $test_pattern, Device: $device_pattern"
    
    # Check prerequisites
    check_git_lfs
    
    case $action in
        list)
            list_golden_images
            ;;
        update)
            if [ "$backup" = "true" ]; then
                create_backup
            fi
            
            # Run tests first if pattern specified
            if [ -n "$test_pattern" ]; then
                run_golden_tests "$test_pattern"
            fi
            
            update_golden_images "$test_pattern" "$device_pattern" "$dry_run" "$confirm"
            
            # Show git status
            if [ "$dry_run" != "true" ]; then
                echo ""
                info "Git status after update:"
                git status --porcelain | grep -E "\.(png|jpg|jpeg)$" || true
                
                echo ""
                info "To commit changes, run:"
                echo "git add $GOLDEN_DIR"
                echo "git commit -m \"Update golden images for $test_pattern\""
            fi
            ;;
        *)
            error "Unknown action: $action"
            ;;
    esac
    
    log "Golden Test Kit Update Script Completed"
}

# Run main function with all arguments
main "$@"