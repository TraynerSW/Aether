#!/bin/bash
# Build script for Android (using Android NDK)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build/android"

# Android NDK Configuration
ANDROID_NDK_VERSION="25.2.9519653"
ANDROID_MIN_SDK=24
ANDROID_TARGET_SDK=34
ANDROID_API_LEVEL=24

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SecureChat Android Build ===${NC}"

# Check for Android NDK
if [ -z "$ANDROID_NDK" ] && [ -z "$ANDROID_NDK_HOME" ]; then
    echo -e "${YELLOW}Warning: ANDROID_NDK or ANDROID_NDK_HOME not set${NC}"
    echo "Please set one of these environment variables to your Android NDK path"
    echo "Example: export ANDROID_NDK=$HOME/Android/Sdk/ndk/$ANDROID_NDK_VERSION"
    exit 1
fi

NDK_PATH="${ANDROID_NDK:-$ANDROID_NDK_HOME}"
echo "Using Android NDK: $NDK_PATH"

# Supported ABIs
ABIS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")

# Create build directory
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

# Build for each ABI
for ABI in "${ABIS[@]}"; do
    echo -e "${YELLOW}Building for ABI: $ABI${NC}"
    
    ABI_BUILD_DIR="$BUILD_DIR/$ABI"
    mkdir -p "$ABI_BUILD_DIR"
    cd "$ABI_BUILD_DIR"
    
    # Set up toolchain
    if [ -f "$NDK_PATH/build/cmake/android.toolchain.cmake" ]; then
        TOOLCHAIN="$NDK_PATH/build/cmake/android.toolchain.cmake"
    else
        echo -e "${RED}Error: Android toolchain not found${NC}"
        exit 1
    fi
    
    # Configure
    cmake "$PROJECT_ROOT/core" \
        -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_NATIVE_API_LEVEL=$ANDROID_API_LEVEL \
        -DCMAKE_BUILD_TYPE=Release \
        -DBUILD_TESTING=OFF \
        -DCMAKE_INSTALL_PREFIX="$BUILD_DIR/install/$ABI"
    
    # Build
    cmake --build . --parallel
    cmake --install .
    
    echo -e "${GREEN}Build successful for $ABI${NC}"
done

# Create combined library structure
echo -e "${YELLOW}Creating combined Android library...${NC}"
COMBINED_DIR="$BUILD_DIR/securechat-android"
mkdir -p "$COMBINED_DIR/libs"

for ABI in "${ABIS[@]}"; do
    mkdir -p "$COMBINED_DIR/libs/$ABI"
    if [ -f "$BUILD_DIR/install/$ABI/lib/libsecurechat_core.a" ]; then
        cp "$BUILD_DIR/install/$ABI/lib/libsecurechat_core.a" "$COMBINED_DIR/libs/$ABI/"
    fi
done

# Copy headers
mkdir -p "$COMBINED_DIR/include"
cp -r "$PROJECT_ROOT/core/include/"* "$COMBINED_DIR/include/"

echo -e "${GREEN}=== Android Build Complete ===${NC}"
echo "Output directory: $COMBINED_DIR"
