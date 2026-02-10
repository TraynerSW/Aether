cmake_minimum_required(VERSION 3.16)
project(SecureChatServer VERSION 1.0.0 LANGUAGES C)

# C11 standard
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Compiler warnings
if(CMAKE_C_COMPILER_ID MATCHES "GNU|Clang")
    add_compile_options(-Wall -Wextra -Wpedantic)
endif()

# Find SecureChatCore
find_package(SecureChatCore REQUIRED HINTS ${CMAKE_CURRENT_SOURCE_DIR}/../core/build)

# Find SQLite3 for database
find_package(SQLite3)
if(NOT SQLite3_FOUND)
    message(WARNING "SQLite3 not found. Install with: apt-get install libsqlite3-dev (Linux), brew install sqlite3 (macOS), or vcpkg install sqlite3 (Windows)")
endif()

# Platform detection
if(ANDROID)
    message(FATAL_ERROR "Server cannot be built for Android")
elseif(IOS)
    message(FATAL_ERROR "Server cannot be built for iOS")
elseif(WIN32)
    if(MINGW)
        message(STATUS "Building server for Windows (MinGW)")
    else()
        message(STATUS "Building server for Windows (MSVC)")
    endif()
    add_compile_definitions(_CRT_SECURE_NO_WARNINGS)
elseif(UNIX AND NOT APPLE)
    message(STATUS "Building server for Linux/Unix")
elseif(APPLE AND NOT IOS)
    message(STATUS "Building server for macOS")
endif()

# Server source files
set(SERVER_SOURCES
    src/main.c
    src/server.c
    src/client_handler.c
    src/database.c
    src/room_manager.c
    src/config.c
)

set(SERVER_HEADERS
    src/server.h
    src/client_handler.h
    src/database.h
    src/room_manager.h
    src/config.h
)

# Create server executable
add_executable(securechat-server ${SERVER_SOURCES} ${SERVER_HEADERS})

# Include directories
target_include_directories(securechat-server
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/src
        ${CMAKE_CURRENT_SOURCE_DIR}/../core/include
)

# Link libraries
target_link_libraries(securechat-server
    PRIVATE
        SecureChat::securechat_core
        $<$<BOOL:${SQLite3_FOUND}>:SQLite::SQLite3>
)

# Platform-specific libraries
if(UNIX AND NOT APPLE)
    find_library(MATH_LIBRARY m)
    find_library(PTHREAD_LIBRARY pthread)
    target_link_libraries(securechat-server
        PRIVATE
            ${MATH_LIBRARY}
            ${PTHREAD_LIBRARY}
    )
elseif(WIN32)
    target_link_libraries(securechat-server
        PRIVATE
            ws2_32
            winmm
    )
elseif(APPLE)
    find_library(PTHREAD_LIBRARY pthread)
    target_link_libraries(securechat-server PRIVATE ${PTHREAD_LIBRARY})
endif()

# Installation
install(TARGETS securechat-server
    RUNTIME DESTINATION bin
)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/data/
    DESTINATION share/securechat-server
)

# Create data directory in build
add_custom_command(TARGET securechat-server POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory
        ${CMAKE_CURRENT_BINARY_DIR}/data
)

# Testing
enable_testing()
if(BUILD_TESTING)
    add_subdirectory(tests)
endif()
