#!/bin/bash
# Build script for iOS (iPhone/iPad)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build/ios"

# iOS Deployment Target
IOS_DEPLOYMENT_TARGET="13.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SecureChat iOS Build ===${NC}"

# Check for Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo -e "${RED}Error: xcodebuild not found. Please install Xcode.${NC}"
    exit 1
fi

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}Error: iOS builds must be performed on macOS${NC}"
    exit 1
fi

# Create build directory
mkdir -p "$BUILD_DIR"

# Build for device (arm64)
echo -e "${YELLOW}Building for iOS Device (arm64)...${NC}"
DEVICE_BUILD_DIR="$BUILD_DIR/device"
mkdir -p "$DEVICE_BUILD_DIR"
cd "$DEVICE_BUILD_DIR"

cmake "$PROJECT_ROOT/core" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_DEPLOYMENT_TARGET=$IOS_DEPLOYMENT_TARGET \
    -DCMAKE_OSX_ARCHITECTURES=arm64 \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_TESTING=OFF \
    -DCMAKE_INSTALL_PREFIX="$DEVICE_BUILD_DIR/install"

cmake --build . --parallel
cmake --install .

# Build for simulator (x86_64 and arm64)
echo -e "${YELLOW}Building for iOS Simulator...${NC}"
SIM_BUILD_DIR="$BUILD_DIR/simulator"
mkdir -p "$SIM_BUILD_DIR"
cd "$SIM_BUILD_DIR"

cmake "$PROJECT_ROOT/core" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_DEPLOYMENT_TARGET=$IOS_DEPLOYMENT_TARGET \
    -DCMAKE_OSX_ARCHITECTURES="x86_64;arm64" \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_TESTING=OFF \
    -DCMAKE_INSTALL_PREFIX="$SIM_BUILD_DIR/install"

cmake --build . --parallel
cmake --install .

# Create XCFramework
echo -e "${YELLOW}Creating XCFramework...${NC}"
XCFRAMEWORK_DIR="$BUILD_DIR/SecureChatCore.xcframework"

rm -rf "$XCFRAMEWORK_DIR"

xcodebuild -create-xcframework \
    -library "$DEVICE_BUILD_DIR/install/lib/libsecurechat_core.a" \
    -headers "$DEVICE_BUILD_DIR/install/include" \
    -library "$SIM_BUILD_DIR/install/lib/libsecurechat_core.a" \
    -headers "$SIM_BUILD_DIR/install/include" \
    -output "$XCFRAMEWORK_DIR"

echo -e "${GREEN}=== iOS Build Complete ===${NC}"
echo "XCFramework: $XCFRAMEWORK_DIR"
