#!/bin/bash
# Build script for Windows (MinGW or MSVC)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build/windows"

# Build type
BUILD_TYPE="${1:-Release}"
COMPILER="${2:-mingw}"  # mingw or msvc

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SecureChat Windows Build ===${NC}"
echo "Build Type: $BUILD_TYPE"
echo "Compiler: $COMPILER"

# Create build directory
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

# Configure based on compiler
if [ "$COMPILER" == "msvc" ]; then
    echo -e "${YELLOW}Using MSVC compiler${NC}"
    
    # Check for Visual Studio
    if ! command -v cmake &> /dev/null; then
        echo -e "${RED}Error: cmake not found${NC}"
        exit 1
    fi
    
    # Generate Visual Studio project
    cmake "$PROJECT_ROOT" \
        -G "Visual Studio 17 2022" \
        -A x64 \
        -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
        -DBUILD_TESTING=ON
    
    # Build
    cmake --build . --config "$BUILD_TYPE" --parallel
    
elif [ "$COMPILER" == "mingw" ]; then
    echo -e "${YELLOW}Using MinGW compiler${NC}"
    
    # Check for MinGW
    if ! command -v x86_64-w64-mingw32-gcc &> /dev/null && ! command -v gcc &> /dev/null; then
        echo -e "${YELLOW}Warning: MinGW gcc not found in PATH${NC}"
        echo "Make sure MinGW is installed and in your PATH"
    fi
    
    # Configure with MinGW Makefiles
    cmake "$PROJECT_ROOT" \
        -G "MinGW Makefiles" \
        -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
        -DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
        -DBUILD_TESTING=ON
    
    # Build
    cmake --build . --parallel
    
else
    echo -e "${RED}Error: Unknown compiler '$COMPILER'${NC}"
    echo "Usage: $0 [Release|Debug] [mingw|msvc]"
    exit 1
fi

# Install/copy artifacts
INSTALL_DIR="$BUILD_DIR/install"
mkdir -p "$INSTALL_DIR/bin"
mkdir -p "$INSTALL_DIR/lib"
mkdir -p "$INSTALL_DIR/include"

echo -e "${YELLOW}Installing artifacts...${NC}"

# Copy executables
if [ -f "$BUILD_DIR/server/securechat-server.exe" ]; then
    cp "$BUILD_DIR/server/securechat-server.exe" "$INSTALL_DIR/bin/"
fi

# Copy libraries
if [ -f "$BUILD_DIR/core/libsecurechat_core.a" ]; then
    cp "$BUILD_DIR/core/libsecurechat_core.a" "$INSTALL_DIR/lib/"
elif [ -f "$BUILD_DIR/core/securechat_core.lib" ]; then
    cp "$BUILD_DIR/core/securechat_core.lib" "$INSTALL_DIR/lib/"
fi

# Copy headers
cp -r "$PROJECT_ROOT/core/include/"* "$INSTALL_DIR/include/"

echo -e "${GREEN}=== Windows Build Complete ===${NC}"
echo "Install directory: $INSTALL_DIR"
echo "Binaries: $INSTALL_DIR/bin"
echo "Libraries: $INSTALL_DIR/lib"
echo ""
echo "To run the server:"
echo "  $INSTALL_DIR/bin/securechat-server.exe"
