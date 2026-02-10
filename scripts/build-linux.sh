#!/bin/bash
# Build script for Linux
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build/linux"

# Build type
BUILD_TYPE="${1:-Release}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SecureChat Linux Build ===${NC}"
echo "Build Type: $BUILD_TYPE"

# Check for required dependencies
echo -e "${YELLOW}Checking dependencies...${NC}"

MISSING_DEPS=()

if ! pkg-config --exists libsodium; then
    MISSING_DEPS+=("libsodium-dev")
fi

if [ ${#MISSING_DEPS[@]} -ne 0 ]; then
    echo -e "${RED}Missing dependencies:${NC}"
    printf '%s\n' "${MISSING_DEPS[@]}"
    echo ""
    echo "Install with: sudo apt-get install ${MISSING_DEPS[*]}"
    exit 1
fi

echo -e "${GREEN}All dependencies found${NC}"

# Create build directory
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

# Configure
echo -e "${YELLOW}Configuring...${NC}"
cmake "$PROJECT_ROOT" \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DBUILD_TESTING=ON \
    -DCMAKE_INSTALL_PREFIX="$BUILD_DIR/install"

# Build
echo -e "${YELLOW}Building...${NC}"
cmake --build . --parallel

# Install
echo -e "${YELLOW}Installing...${NC}"
cmake --install .

echo -e "${GREEN}=== Linux Build Complete ===${NC}"
echo "Install directory: $BUILD_DIR/install"
echo "Binaries: $BUILD_DIR/install/bin"
echo "Libraries: $BUILD_DIR/install/lib"
echo ""
echo "To run the server:"
echo "  $BUILD_DIR/install/bin/securechat-server"
