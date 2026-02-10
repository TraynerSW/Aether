# SecureChat Architecture

## Overview

SecureChat is an end-to-end encrypted messaging application with support for text messages, voice calls, and file sharing. The project is organized as a monorepo with cross-platform support.

## Project Structure

```
securechat/
├── core/                   # Core C library (crypto, network, protocol)
│   ├── include/securechat/ # Public headers
│   └── src/                # Implementation
├── server/                 # Server C executable
│   ├── src/                # Server source
│   └── data/               # Server data directory
├── mobile/                 # React Native Expo app
│   ├── src/                # Mobile app source
│   ├── assets/             # Static assets
│   └── package.json        # Node dependencies
├── desktop/                # Windows desktop app (future)
├── tests/                  # Integration tests
├── docs/                   # Documentation
├── scripts/                # Build scripts
│   ├── build-android.sh
│   ├── build-ios.sh
│   ├── build-windows.sh
│   └── build-linux.sh
├── CMakeLists.txt          # Root CMake configuration
└── .gitignore
```

## Core Library (C)

The core library provides foundational cryptographic and networking functionality.

### Modules

1. **Crypto** (`crypto.c/h`)
   - libsodium integration
   - X25519 key exchange
   - Ed25519 signatures
   - XChaCha20-Poly1305 encryption
   - Argon2id key derivation
   - BLAKE2b hashing

2. **Network** (`network.c/h`)
   - WebSocket connection management
   - Event-driven architecture
   - Cross-platform socket handling

3. **Protocol** (`protocol.c/h`)
   - Binary message protocol
   - Packet serialization/deserialization
   - Version negotiation

4. **Voice** (`voice.c/h`)
   - Opus codec integration
   - Audio encoding/decoding
   - FEC and DTX support

5. **Utils** (`utils.c/h`)
   - Base64/Hex encoding
   - Timestamp handling
   - Secure memory operations

### Security Architecture

- **End-to-End Encryption**: All messages encrypted with XChaCha20-Poly1305
- **Perfect Forward Secrecy**: X25519 key exchange per session
- **Authenticated Signatures**: Ed25519 for message authentication
- **Zero Knowledge Server**: Server cannot read message content

## Server

The server acts as a message relay and presence manager.

### Components

- **Server Core**: Main event loop and connection management
- **Client Handler**: Per-client connection handling
- **Database**: SQLite for user/room persistence
- **Room Manager**: Multi-user room handling

### Protocol

1. Client connects via WebSocket
2. Handshake with version and public key exchange
3. Authentication with Ed25519 signature
4. Encrypted message relay

## Mobile App (React Native + Expo)

Cross-platform mobile application with native module integration.

### Technology Stack

- **Framework**: React Native with Expo (managed workflow)
- **Navigation**: React Navigation v6
- **State Management**: Redux Toolkit with Redux Persist
- **Networking**: Socket.IO client
- **Storage**: AsyncStorage with encryption
- **Audio**: react-native-audio-recorder-player
- **Permissions**: react-native-permissions

### Architecture Pattern

- **Presentation**: Functional components with hooks
- **State**: Redux store with slices
- **API**: REST + WebSocket hybrid
- **Storage**: Encrypted local storage

### Security Features

- Secure key storage using device hardware
- Biometric authentication
- Auto-lock on background
- Screenshot protection

## Build System

### CMake Configuration

- **Minimum Version**: 3.16
- **C Standard**: C11
- **Cross-compilation**: Android NDK, iOS, Windows (MinGW/MSVC)

### Scripts

| Script | Purpose |
|--------|---------|
| `build-android.sh` | Build Android libraries (4 ABIs) |
| `build-ios.sh` | Build iOS XCFramework |
| `build-windows.sh` | Build Windows executable |
| `build-linux.sh` | Build Linux executable |
| `build.sh` | Main build script with auto-detection |

## Dependencies

### Core C Library

- libsodium (crypto)
- libwebsockets (WebSocket)
- libopus (voice codec)
- SQLite3 (database)

### Mobile

- React Native 0.73
- Expo 50
- React Navigation 6
- Redux Toolkit 2
- Socket.IO Client 4

## Platform Support

| Platform | Core Library | Server | Mobile |
|----------|-------------|--------|--------|
| Linux | ✅ | ✅ | N/A |
| macOS | ✅ | ✅ | N/A |
| Windows | ✅ | ✅ | N/A |
| Android | ✅ | N/A | ✅ |
| iOS | ✅ | N/A | ✅ |

## Development Workflow

1. **Core Development**: Edit C code, rebuild with scripts
2. **Mobile Development**: Use Expo CLI for hot reloading
3. **Testing**: Unit tests in tests/, integration tests separately

## Security Considerations

- All keys generated client-side
- Server never sees plaintext
- Metadata minimized (timestamps only)
- No persistent logs with content

## Future Enhancements

- [ ] Desktop application (Electron/Tauri)
- [ ] Group chat with distributed keys
- [ ] File sharing with chunked encryption
- [ ] Voice/video calls with WebRTC
- [ ] Multi-device support

## License

TBD
