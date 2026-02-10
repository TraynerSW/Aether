#!/bin/bash
# Main build script - detects platform and runs appropriate build
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔══════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      SecureChat Build System         ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════╝${NC}"
echo ""

# Detect platform
PLATFORM="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    PLATFORM="linux"
    echo "Detected platform: Linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    PLATFORM="macos"
    echo "Detected platform: macOS"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
    PLATFORM="windows"
    echo "Detected platform: Windows"
else
    echo "Unknown platform: $OSTYPE"
fi

# Build target
TARGET="${1:-all}"
BUILD_TYPE="${2:-Release}"

echo "Target: $TARGET"
echo "Build Type: $BUILD_TYPE"
echo ""

# Function to show usage
show_usage() {
    echo "Usage: $0 [target] [build_type]"
    echo ""
    echo "Targets:"
    echo "  all       - Build everything (default)"
    echo "  core      - Build core library only"
    echo "  server    - Build server only"
    echo "  mobile    - Prepare mobile project"
    echo "  clean     - Clean build directories"
    echo ""
    echo "Build Types:"
    echo "  Release   - Optimized build (default)"
    echo "  Debug     - Debug build"
    echo ""
    echo "Platform-specific targets:"
    if [ "$PLATFORM" == "linux" ]; then
        echo "  linux     - Build for Linux"
    elif [ "$PLATFORM" == "macos" ]; then
        echo "  ios       - Build for iOS"
        echo "  macos     - Build for macOS"
    elif [ "$PLATFORM" == "windows" ]; then
        echo "  windows   - Build for Windows"
        echo "  mingw     - Build with MinGW"
    fi
    echo "  android   - Build for Android"
}

# Handle targets
case "$TARGET" in
    -h|--help|help)
        show_usage
        exit 0
        ;;
    
    clean)
        echo -e "${YELLOW}Cleaning build directories...${NC}"
        rm -rf "$SCRIPT_DIR/../build"
        echo -e "${GREEN}Clean complete${NC}"
        exit 0
        ;;
    
    core)
        echo -e "${YELLOW}Building core library...${NC}"
        if [ "$PLATFORM" == "linux" ]; then
            bash "$SCRIPT_DIR/build-linux.sh" "$BUILD_TYPE"
        elif [ "$PLATFORM" == "macos" ]; then
            bash "$SCRIPT_DIR/build-macos.sh" "$BUILD_TYPE"
        elif [ "$PLATFORM" == "windows" ]; then
            bash "$SCRIPT_DIR/build-windows.sh" "$BUILD_TYPE"
        fi
        ;;
    
    server)
        echo -e "${YELLOW}Building server...${NC}"
        if [ "$PLATFORM" == "linux" ]; then
            bash "$SCRIPT_DIR/build-linux.sh" "$BUILD_TYPE"
        elif [ "$PLATFORM" == "macos" ]; then
            bash "$SCRIPT_DIR/build-macos.sh" "$BUILD_TYPE"
        elif [ "$PLATFORM" == "windows" ]; then
            bash "$SCRIPT_DIR/build-windows.sh" "$BUILD_TYPE"
        fi
        ;;
    
    android)
        echo -e "${YELLOW}Building for Android...${NC}"
        bash "$SCRIPT_DIR/build-android.sh"
        ;;
    
    ios)
        if [ "$PLATFORM" != "macos" ]; then
            echo -e "${RED}Error: iOS builds must be performed on macOS${NC}"
            exit 1
        fi
        echo -e "${YELLOW}Building for iOS...${NC}"
        bash "$SCRIPT_DIR/build-ios.sh"
        ;;
    
    mobile)
        echo -e "${YELLOW}Preparing mobile project...${NC}"
        cd "$SCRIPT_DIR/../mobile"
        if [ ! -d "node_modules" ]; then
            echo "Installing dependencies..."
            npm install
        fi
        echo -e "${GREEN}Mobile project ready${NC}"
        echo "Run 'npm start' in the mobile/ directory to start the development server"
        ;;
    
    all|*)
        echo -e "${YELLOW}Building all components...${NC}"
        
        # Build native components
        if [ "$PLATFORM" == "linux" ]; then
            bash "$SCRIPT_DIR/build-linux.sh" "$BUILD_TYPE"
        elif [ "$PLATFORM" == "macos" ]; then
            if command -v bash "$SCRIPT_DIR/build-macos.sh" "$BUILD_TYPE" 2>/dev/null; then
                bash "$SCRIPT_DIR/build-macos.sh" "$BUILD_TYPE"
            else
                echo "macOS build script not found, skipping..."
            fi
        elif [ "$PLATFORM" == "windows" ]; then
            bash "$SCRIPT_DIR/build-windows.sh" "$BUILD_TYPE"
        fi
        
        echo -e "${GREEN}=== Build Complete ===${NC}"
        ;;
esac
