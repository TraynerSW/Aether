# Notepad: Application Messagerie Chiffrée

## Session Info
- Started: 2026-02-10T15:14:20Z
- Plan: messagerie-chiffree
- Session ID: ses_3b803c38affemhBt9goX42FwHJ
- Agent: Atlas

## Progression

### Wave 1 - Foundation
- [ ] Task 1: Setup Architecture Projet
- [ ] Task 2: Core Chiffrement libsodium
- [ ] Task 3: SQLite Schema Serveur
- [ ] Task 4: React Native Base Project

### Wave 2 - Protocol Core
- [ ] Task 5: WebSocket + TLS 1.3 Client/Server
- [ ] Task 6: Protocole Binaire Messages
- [ ] Task 7: Gestion Sessions/Connexions
- [ ] Task 8: Bridge C ↔ JS

### Wave 3 - Features
- [ ] Task 9: Messages Texte + Groupes
- [ ] Task 10: Auth 2FA (TOTP)
- [ ] Task 11: Messages Vocaux (Opus)
- [ ] Task 12: Transfert Fichiers (HTTP Range Requests)

### Wave 4 - Temps réel
- [ ] Task 13: WebRTC Signalisation
- [ ] Task 14: Appels Vocaux
- [ ] Task 15: Appels Vidéo (OPTIONNEL)

### Wave 5 - Polish
- [ ] Task 16: Notifications FCM
- [ ] Task 17: UI/UX Polish
- [ ] Task 18: Tests Integration Complets
- [ ] Task 19: Documentation + Déploiement

## Learnings & Conventions

### Architecture
- Core C dans `/core/`
- Serveur dans `/server/`
- UI React Native dans `/mobile/`
- UI Windows dans `/desktop/`

### Technologies
- WebSocket + TLS 1.3 (pas QUIC)
- HTTP Range Requests pour fichiers (pas BitTorrent)
- libsodium pour E2EE
- libopus pour audio
- WebRTC pour appels voix (vidéo optionnel)

### Guardrails
- Max 256 membres par groupe
- Max 5 devices par compte
- Max 5 min pour messages vocaux
- Max 100 Mo pour fichiers
- Pas de crypto maison
- Pas de forward messages

## Issues & Blockers

*Aucun pour le moment*

## Decisions

- Protocole: WebSocket+TLS1.3 (recommandé par Momus)
- Fichiers: HTTP Range (recommandé par Momus)
- Vidéo: Optionnel post-MVP (recommandé par Momus)

## Evidence Paths

- Structure: .sisyphus/evidence/task1-structure.txt
- CMake: .sisyphus/evidence/task1-cmake.log
- Crypto: .sisyphus/evidence/task2-crypto-test.log
- DB: .sisyphus/evidence/task3-schema.log
- Expo: .sisyphus/evidence/task4-expo-init.log
- WebSocket: .sisyphus/evidence/task5-websocket-connect.log
- Protocol: .sisyphus/evidence/task6-protocol.log
- Sessions: .sisyphus/evidence/task7-session.log
- Bridge: .sisyphus/evidence/task8-bridge-load.log
- Chat: Screenshots .sisyphus/evidence/task9-chat-*.png
- 2FA: .sisyphus/evidence/task10-2fa.log
- Voice: .sisyphus/evidence/task11-voice.log
- Files: .sisyphus/evidence/task12-file-transfer.log
- WebRTC: .sisyphus/evidence/task13-signaling.log
- Voice calls: .sisyphus/evidence/task14-voice-call.log
- Video calls: .sisyphus/evidence/task15-video-call.log
- FCM: .sisyphus/evidence/task16-fcm.log
- Design: Screenshots .sisyphus/evidence/task17-design-*.png
- E2E: .sisyphus/evidence/task18-e2e.log
- Builds: .sisyphus/evidence/task19-builds.log
- Docs: .sisyphus/evidence/task19-docs.log
