# Plan de Travail: Application Mobile de Messagerie Centralisée Chiffrée

## TL;DR

**Quick Summary**: Application de messagerie E2EE cross-platform (Windows, Android, iOS) avec core C et UI React Native/Expo. Inclut messages texte/vocaux (Opus), transfert fichiers 100 Mo via HTTP, et appels vocaux (WebRTC). **Note**: Vidéo optionnelle post-MVP.

**Deliverables**:
- Core C chiffré E2EE (libsodium)
- Serveur centralisé WebSocket + TLS 1.3
- Apps React Native (Android/iOS) + Windows
- WebRTC pour appels vocaux (vidéo post-MVP)
- HTTP Range Requests pour fichiers (chiffrés E2EE)
- Authentification 2FA (TOTP)

**Estimated Effort**: XL (Extra Large) - 10 semaines (reduit de 12 apres revue Momus)
**Parallel Execution**: YES - 5 vagues
**Critical Path**: Architecture → Core chiffrement → Protocole réseau → UI bridge → Intégration

---

## Context

### Original Request
Créer une application mobile de messagerie centralisée chiffrée en C, avec vocaux possibles et envoi de fichiers (photos, vidéos, etc) avec une taille maximale de 100 Mo.

### Interview Summary
**Architecture décidée**:
- Hybride C + React Native/Expo
- Core C: chiffrement, réseau, stockage
- UI: React Native (cross-platform)
- Protocole: WebSocket + TLS 1.3 (recommandé par Momus vs QUIC)
- Chiffrement: E2EE avec libsodium
- Fichiers: HTTP Range Requests (recommandé par Momus vs BitTorrent)
- Audio: Opus
- Appels: Voix WebRTC (vidéo optionnelle post-MVP)
- Auth: 2FA (TOTP)

**Plateformes**: Windows, Android, iOS

### Metis Review
**Identified Gaps** (addressed in guardrails):
- Limites de groupe définies (max 256 membres)
- Max devices par compte (5 devices)
- Durée max messages vocaux (5 minutes)
- Pas de P2P pour texte (centralisé uniquement)
- Pas de group calls (1:1 uniquement)
- Pas de stories/status
- Max messages texte (10 Ko)
- File retention (30 jours après téléchargement)

**Top Risques Identifiés**:
1. Complexité WebRTC (signalisation, ICE, NAT)
2. BitTorrent modifié (adaptation mobile)
3. Cross-platform C (compilation 3 plateformes)
4. Expo native modules (libsodium, WebRTC bindings)
5. iOS background execution (notifications)

---

## Work Objectives

### Core Objective
Développer une application de messagerie sécurisée E2EE fonctionnant sur Windows, Android et iOS, avec support des messages texte/vocaux, transfert de fichiers jusqu'à 100 Mo, et appels vocaux/vidéo en temps réel.

### Concrete Deliverables
1. **Core C** (`/core/`): Chiffrement, protocole QUIC, gestion messages/fichiers
2. **Serveur** (`/server/`): Centralisé, SQLite, signaling WebRTC, tracker BitTorrent
3. **UI React Native** (`/mobile/`): Expo app pour Android/iOS
4. **UI Windows** (`/desktop/`): React Native Windows ou Electron
5. **Tests d'intégration** (`/tests/`): Scénarios utilisateurs complets
6. **Documentation** (`/docs/`): Architecture, API, déploiement

### Definition of Done
- [ ] Compilation réussie sur Windows, Android, iOS
- [ ] Tests d'intégration passent (100% scénarios critiques)
- [ ] Audit de sécurité basique (pas de clés en clair, crypto valide)
- [ ] Performance: démarrage <2s, envoi message <500ms
- [ ] Documentation complète pour déploiement

### Must Have (Non-négociable)
- Messages texte E2EE (1-1 et groupes)
- Messages vocaux (Opus, max 5 min)
- Fichiers jusqu'à 100 Mo (HTTP + chiffrement E2EE)
- Appels vocaux 1:1 (WebRTC)
- Appels vidéo 1:1 (optionnel post-MVP)
- Authentification 2FA (TOTP)
- Chiffrement E2EE avec libsodium
- Applications Android + iOS
- Application Windows

### Modifications après Revue Momus
- Protocole: QUIC/HTTP3 → WebSocket + TLS 1.3 (simplification, -1 semaine)
- Fichiers: BitTorrent modifié → HTTP Range Requests (simplification majeure)
- Vidéo: Passée en optionnelle post-MVP (réduction scope Wave 4)
- Effort révisé: 10 semaines (vs 12 initialement)

### Must NOT Have (Guardrails - Verrouillés)
- Appels de groupe (multiparty) - EXCLU
- Stories/status éphémères - EXCLU
- P2P pour messages texte (fichiers uniquement) - EXCLU
- Groupes > 256 membres - LIMITE: 256 max
- Messages vocaux > 5 minutes - LIMITE: 5 min max
- Fichiers > 100 Mo - LIMITE: 100 Mo max
- Plus de 5 devices par compte - LIMITE: 5 devices
- Groupes publics (E2EE uniquement privé) - EXCLU
- Forward de messages - EXCLU
- Link previews - EXCLU
- Backup cloud des messages - EXCLU MVP
- Recherche full-text - EXCLU MVP
- Cryptographie maison - INTERDIT (libsodium uniquement)
- Messages éphémères (disappearing) - EXCLU MVP
- Multi-compte par app - EXCLU MVP
- Federation (inter-serveur) - EXCLU MVP

---

## Verification Strategy

**UNIVERSAL RULE: ZERO HUMAN INTERVENTION**

ALL tasks in this plan MUST be verifiable WITHOUT any human action.

### Test Decision
- **Infrastructure exists**: NO (a creer)
- **Automated tests**: Tests d'integration uniquement
- **Framework**: Bash scripts + curl/playwright pour scenarios utilisateurs

### Agent-Executed QA Scenarios (MANDATORY)

**Verification Tool by Deliverable Type:**

| Type | Tool | How Agent Verifies |
|------|------|-------------------|
| Frontend/UI | Playwright | Navigate, interact, assert DOM, screenshot |
| Core C/Library | Bash | Compile, link, run test scenarios |
| API/Backend | Bash (curl) | Send requests, parse responses, assert fields |
| CLI/Tools | Bash | Run commands, validate output, check exit codes |

---

## Execution Strategy

### Parallel Execution Waves

**Wave 1 - Foundation (Semaine 1-2):**
- Task 1: Setup architecture projet
- Task 2: Core chiffrement libsodium
- Task 3: SQLite schema serveur
- Task 4: React Native base project

**Wave 2 - Protocol Core (Semaine 3-4):**
- Task 5: WebSocket + TLS 1.3 client/server
- Task 6: Protocole binaire messages
- Task 7: Gestion sessions/connexions
- Task 8: Bridge C ↔ JS

**Wave 3 - Features (Semaine 5-7):**
- Task 9: Messages texte + groupes
- Task 10: Auth 2FA (TOTP)
- Task 11: Messages vocaux (Opus)
- Task 12: Transfert fichiers (HTTP Range Requests)

**Wave 4 - Temps reel (Semaine 8-9):**
- Task 13: WebRTC signalisation
- Task 14: Appels vocaux
- Task 15: Appels video (OPTIONNEL post-MVP)

**Wave 5 - Polish (Semaine 10-12):**
- Task 16: Notifications FCM
- Task 17: UI/UX polish
- Task 18: Tests integration complets
- Task 19: Documentation + deploiement

### Critical Path
Task 1 → Task 2 → Task 5 → Task 6 → Task 9 → Task 13 → Task 14 → Task 17 → Task 18 → Task 19

---

## TODOs Overview

### Wave 1: Foundation
1. Setup Architecture Projet
2. Core Chiffrement libsodium
3. SQLite Schema Serveur
4. React Native Base Project

### Wave 2: Protocol Core
5. QUIC/HTTP3 Client/Server
6. Protocole Binaire Messages
7. Gestion Sessions/Connexions
8. Bridge C ↔ JS

### Wave 3: Features
9. Messages Texte + Groupes
10. Auth 2FA (TOTP)
11. Messages Vocaux (Opus)
12. Transfert Fichiers (BitTorrent)

### Wave 4: Temps Reel
13. WebRTC Signalisation
14. Appels Vocaux
15. Appels Video

### Wave 5: Polish
16. Notifications FCM
17. UI/UX Polish
18. Tests Integration Complets
19. Documentation + Deploiement

---

## Commit Strategy Summary

| Task | Message | Files |
|------|---------|-------|
| 1 | chore(setup): initial project structure | All config |
| 2 | feat(crypto): libsodium wrapper | core/crypto/ |
| 3 | feat(db): SQLite schema | server/schema/ |
| 4 | feat(mobile): Expo base | mobile/ |
| 5 | feat(network): QUIC implementation | core/network/ |
| 6 | feat(protocol): binary protocol | core/protocol/ |
| 7 | feat(auth): session management | server/auth/ |
| 8 | feat(bridge): native modules | mobile/native/ |
| 9 | feat(chat): messaging and groups | mobile/screens/Chat* |
| 10 | feat(auth): TOTP 2FA | server/auth/totp* |
| 11 | feat(voice): Opus voice messages | core/audio/ |
| 12 | feat(files): BitTorrent file transfer | core/files/ |
| 13 | feat(webrtc): signaling server | server/signaling/ |
| 14 | feat(calls): voice calls | core/webrtc/audio* |
| 15 | feat(calls): video calls | core/webrtc/video* |
| 16 | feat(notifications): FCM | mobile/notifications/ |
| 17 | feat(ui): design system and polish | mobile/components/ |
| 18 | test(integration): e2e tests | tests/integration/ |
| 19 | docs: documentation and deployment | README.md, docs/ |

---

## Success Criteria

### Verification Commands

```bash
# Build verification
./scripts/build-all.sh
# Expected: All platforms build successfully

# Test verification
./scripts/test-all.sh
# Expected: 100% integration tests pass

# Security verification
./scripts/security-check.sh
# Expected: No critical vulnerabilities

# Performance verification
./scripts/perf-test.sh
# Expected: Latency < 500ms, Throughput > 100 msg/sec
```

### Final Checklist

- [ ] **Must Have**: Toutes les fonctionnalités IN implementées
- [ ] **Must NOT Have**: Aucune feature OUT presente
- [ ] **Security**: Pas de clés en clair, crypto libsodium uniquement
- [ ] **Performance**: Démarrage < 2s, envoi < 500ms
- [ ] **Tests**: 100% scenarios critiques passent
- [ ] **Documentation**: README, ARCHITECTURE, DEPLOY complets
- [ ] **Builds**: APK, IPA, EXE, Docker image generes
- [ ] **Guardrails**: Toutes les limites respectees (5 devices, 256 membres, 100 Mo, etc.)

---

## Detailed TODOs

### Task 1: Setup Architecture Projet

**What to do:**
- Creer structure repertoires (/core/, /server/, /mobile/, /desktop/, /tests/, /docs/)
- Setup CMakeLists.txt pour cross-compilation (Windows, Android NDK, iOS)
- Setup React Native project avec Expo
- Setup scripts de build pour chaque plateforme
- Documentation architecture initiale

**Must NOT do:**
- Ne PAS coder la logique metier (juste la structure)
- Ne PAS inclure de secrets/credentials dans le repo
- Ne PAS oublier .gitignore approprie

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: [git-master]

**Parallelization:**
- Can Run In Parallel: NO (fondation)
- Parallel Group: Wave 1
- Blocks: Tous les autres tasks
- Blocked By: None

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Structure de projet complete
  Tool: Bash
  Preconditions: Aucune
  Steps:
    1. Verifier existence: core/, server/, mobile/, desktop/, tests/, docs/
    2. Verifier existence: CMakeLists.txt dans core/ et server/
    3. Verifier existence: package.json dans mobile/
    4. Verifier .gitignore present
    5. Lister structure: find . -type d -maxdepth 2
    6. Assert: minimum 6 repertoires principaux
  Expected Result: Structure complete prete pour developpement
  Evidence: .sisyphus/evidence/task1-structure.txt

Scenario: Compilation CMake basique
  Tool: Bash
  Steps:
    1. mkdir -p build && cd build
    2. cmake ..
    3. Assert: cmake configure reussi (exit code 0)
  Expected Result: CMake configuration reussie
  Evidence: .sisyphus/evidence/task1-cmake.log

**Commit:** YES
- Message: chore(setup): initial project structure with CMake and Expo
- Files: Tous les fichiers de configuration

---

### Task 2: Core Chiffrement libsodium

**What to do:**
- Wrapper C autour de libsodium
- Generation et stockage securise des cles (Keypair X25519 pour E2EE)
- Chiffrement/dechiffrement E2EE (Box crypto_box)
- Stockage securise cles (Keychain iOS, Keystore Android, DPAPI Windows)
- Tests unitaires basiques du wrapper

**Must NOT do:**
- Ne PAS implementer de crypto maison
- Ne PAS stocker les cles privees en clair
- Ne PAS oublier de nettoyer la memoire des cles (memset_s)

**Recommended Agent Profile:**
- Category: deep
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 1
- Blocks: Task 5, Task 6
- Blocked By: Task 1

**References:**
- Documentation: https://libsodium.gitbook.io/doc/
- Pattern: libsodium examples: crypto_box_keypair(), crypto_box_easy()

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Generation et chiffrement de paire de cles
  Tool: Bash
  Steps:
    1. Compiler test: gcc -o test_crypto tests/test_crypto.c -lsodium
    2. Executer: ./test_crypto
    3. Assert: generation keypair reussie (pubkey 32 bytes, privkey 32 bytes)
    4. Assert: chiffrement message "Hello World" reussi
    5. Assert: dechiffrement retourne "Hello World" exact
    6. Assert: dechiffrement avec mauvaise cle echoue
  Expected Result: Chiffrement E2EE fonctionnel
  Evidence: .sisyphus/evidence/task2-crypto-test.log

Scenario: Stockage securise des cles
  Tool: Bash
  Steps:
    1. Executer test stockage: ./test_key_storage
    2. Assert: cles stockees sans erreur
    3. Verifier permissions: ls -la ~/.config/securechat/keys/
    4. Assert: permissions 0600
    5. Tenter lecture directe du fichier
    6. Assert: contenu chiffre (pas de cle en clair)
  Expected Result: Cles stockees de maniere securisee
  Evidence: .sisyphus/evidence/task2-keystore.log

**Commit:** YES
- Message: feat(crypto): libsodium wrapper with secure key storage
- Files: core/crypto/

---

### Task 3: SQLite Schema Serveur

**What to do:**
- Schema base de donnees SQLite pour serveur
- Tables: users, messages, groups, group_members, files, sessions, totp_secrets
- Migrations SQL versionnees
- Scripts d'initialisation DB

**Must NOT do:**
- Ne PAS stocker contenu messages en clair (metadata uniquement)
- Ne PAS oublier les index sur les cles etrangeres
- Ne PAS stocker les cles privees des utilisateurs

**Recommended Agent Profile:**
- Category: quick
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 1
- Blocks: Task 7
- Blocked By: Task 1

**Schema Propose:**
- users: id, username, public_key, created_at, last_seen
- messages: id, sender_id, recipient_id/group_id, encrypted_payload_ref, timestamp
- groups: id, name, creator_id, created_at
- group_members: group_id, user_id, joined_at
- files: id, uploader_id, torrent_info_hash, size, created_at
- sessions: id, user_id, device_id, auth_token, expires_at
- totp_secrets: user_id, secret_encrypted, enabled

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Creation et validation du schema
  Tool: Bash
  Steps:
    1. Creer DB: sqlite3 server/data/server.db < server/schema/init.sql
    2. Verifier tables: sqlite3 server/data/server.db ".tables"
    3. Assert: minimum 6 tables creees
    4. Verifier structure users: sqlite3 server/data/server.db ".schema users"
    5. Assert: colonnes id, username, public_key presentes
    6. Insert test et verification
  Expected Result: Schema DB fonctionnel
  Evidence: .sisyphus/evidence/task3-schema.log

**Commit:** YES
- Message: feat(db): SQLite schema for server with migrations
- Files: server/schema/, server/db/

---

### Task 4: React Native Base Project

**What to do:**
- Initialiser projet Expo (managed workflow)
- Setup React Navigation
- Setup Redux Toolkit (state management)
- Setup structure dossiers (screens/, components/, store/, api/, utils/)
- Setup Native Modules bridge pour core C
- Ecran Login basique (UI uniquement)

**Must NOT do:**
- Ne PAS implementer la logique metier (juste structure)
- Ne PAS oublier les permissions Android/iOS dans app.json

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: [frontend-ui-ux]

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 1
- Blocks: Task 8
- Blocked By: Task 1

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Build Expo reussi
  Tool: Bash
  Steps:
    1. cd mobile/ && npm install
    2. npx expo prebuild
    3. Assert: prebuild reussi sans erreur
    4. Verifier: dossiers ios/ et android/ crees
  Expected Result: Projet Expo initialise
  Evidence: .sisyphus/evidence/task4-expo-init.log

Scenario: Navigation setup
  Tool: Bash
  Steps:
    1. Verifier App.js: NavigationContainer present
    2. Lister ecrans: minimum 3 ecrans (Login, Chat, Settings)
    3. Verifier store: Redux store existe
  Expected Result: Structure React Navigation + Redux prete
  Evidence: .sisyphus/evidence/task4-structure.log

**Commit:** YES
- Message: feat(mobile): Expo base project with navigation and Redux
- Files: mobile/

---

### Task 5: WebSocket + TLS 1.3 Client/Server

**What to do:**
- Integrer libwebsockets ou uWebSockets
- Creer connexion client-serveur WebSocket securise (TLS 1.3)
- Gerer reconnexions automatiques
- Gerer certificats TLS
- Tests basiques connectivite
- **Note**: Changement par rapport au plan initial (QUIC → WebSocket recommandé par Momus pour simplification)

**Must NOT do:**
- Ne PAS utiliser WebSocket sans TLS (securite)
- Ne PAS oublier la gestion des timeouts
- Ne PAS oublier le heartbeat pour detecter deconnexions

**Recommended Agent Profile:**
- Category: deep
- Skills: []

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 2
- Blocks: Task 6, Task 7
- Blocked By: Task 1, Task 2

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Connexion WebSocket client-serveur
  Tool: Bash
  Steps:
    1. Demarrer serveur: ./server/build/chat_server --port 4433
    2. Demarrer client test: ./core/build/test_ws_client --server wss://localhost:4433
    3. Assert: connexion WebSocket etablie (handshake TLS OK)
    4. Envoyer message ping
    5. Assert: pong recu
  Expected Result: Connexion WebSocket TLS fonctionnelle
  Evidence: .sisyphus/evidence/task5-websocket-connect.log

**Commit:** YES
- Message: feat(network): WebSocket + TLS 1.3 client-server implementation
- Files: core/network/, server/network/

---

### Task 6: Protocole Binaire Messages

**What to do:**
- Definir format binaire des messages (protobuf ou custom binary)
- Types: MESSAGE_TEXT, MESSAGE_VOICE, MESSAGE_FILE, CALL_SIGNAL
- Structure: header + payload chiffre
- Serialisation/deserialisation C

**Must NOT do:**
- Ne PAS utiliser JSON (trop verbeux)
- Ne PAS oublier le versioning du protocole (v1)

**Recommended Agent Profile:**
- Category: deep
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 2
- Blocks: Task 9
- Blocked By: Task 1, Task 2

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Encode/decode message basique
  Tool: Bash
  Steps:
    1. Compiler test: gcc -o test_protocol tests/test_protocol.c
    2. Executer: ./test_protocol
    3. Assert: serialisation reussie
    4. Assert: taille message 100 chars < 500 bytes encode
  Expected Result: Protocole binaire fonctionnel
  Evidence: .sisyphus/evidence/task6-protocol.log

**Commit:** YES
- Message: feat(protocol): binary message protocol with encryption
- Files: core/protocol/

---

### Task 7: Gestion Sessions/Connexions

**What to do:**
- Gestion des sessions utilisateurs (login, logout)
- Auth tokens (JWT ou tokens aleatoires)
- Validation tokens cote serveur
- Multi-device (jusqu'a 5 devices par user)

**Must NOT do:**
- Ne PAS stocker tokens en clair dans la DB (hasher)
- Ne PAS autoriser plus de 5 devices par user

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 2
- Blocks: Task 9
- Blocked By: Task 1, Task 3, Task 5

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Login et creation session
  Tool: Bash
  Steps:
    1. Envoyer requete login
    2. Assert: reponse 200 OK avec token
    3. Verifier DB: session creee avec expiration
  Expected Result: Session creee avec token valide
  Evidence: .sisyphus/evidence/task7-session.log

Scenario: Limite 5 devices
  Tool: Bash
  Steps:
    1. Creer 5 sessions pour user test
    2. Tenter login device 6
    3. Assert: erreur "Max devices reached"
  Expected Result: Limite 5 devices respectee
  Evidence: .sisyphus/evidence/task7-device-limit.log

**Commit:** YES
- Message: feat(auth): session management with 5 device limit
- Files: server/auth/, core/auth/

---

### Task 8: Bridge C ↔ JS (Native Modules)

**What to do:**
- Creer bridge React Native pour appeler le core C
- Android: JNI bindings
- iOS: Objective-C++ bridge
- Windows: Node-API ou equivalent
- Exposer fonctions: init(), sendMessage(), encrypt(), decrypt()

**Must NOT do:**
- Ne PAS bloquer le thread JS
- Ne PAS oublier de liberer la memoire cote C

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: []

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 2
- Blocks: Task 9, Task 10, Task 11, Task 12
- Blocked By: Task 1, Task 4

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Bridge basique fonctionnel
  Tool: Bash
  Steps:
    1. Charger module natif
    2. Assert: pas d'erreur de chargement
    3. Verifier logs: "Bridge initialized"
  Expected Result: Bridge charge sans erreur
  Evidence: .sisyphus/evidence/task8-bridge-load.log

Scenario: Appel fonction C depuis JS
  Tool: Bash
  Steps:
    1. Implementer test_echo dans C
    2. Exposer via bridge
    3. Tester: echo("hello") retourne "hello"
  Expected Result: Communication bidirectionnelle fonctionnelle
  Evidence: .sisyphus/evidence/task8-echo.log

**Commit:** YES
- Message: feat(bridge): React Native to C native modules for all platforms
- Files: mobile/android/, mobile/ios/, mobile/windows/, mobile/native/

---

### Task 9: Messages Texte + Groupes

**What to do:**
- UI liste conversations
- UI conversation (messages bubbles)
- Envoi message texte
- Reception messages
- Creation groupes (max 256 membres)
- Liste membres groupe

**Must NOT do:**
- Ne PAS autoriser groupes > 256 membres
- Ne PAS implementer forward messages (EXCLU)

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: [frontend-ui-ux]

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 3
- Blocks: Task 13
- Blocked By: Task 6, Task 7, Task 8

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Envoi et reception message 1-1
  Tool: Playwright
  Steps:
    1. User A et User B connectes
    2. User A envoie "Hello B"
    3. User B recoit et voit le message
    4. User B repond "Hello A"
  Expected Result: Messagerie 1-1 fonctionnelle
  Evidence: Screenshots .sisyphus/evidence/task9-chat-*.png

Scenario: Creation groupe et envoi
  Tool: Playwright
  Steps:
    1. Creer groupe avec 3 membres
    2. Envoyer message dans groupe
    3. Tous les membres recoivent
  Expected Result: Groupes fonctionnels
  Evidence: .sisyphus/evidence/task9-group.log

**Commit:** YES
- Message: feat(chat): text messaging with groups up to 256 members
- Files: mobile/screens/ChatScreen.js, core/messaging/

---

### Task 10: Auth 2FA (TOTP)

**What to do:**
- Generation secret TOTP cote serveur
- QR code pour configuration (Google Authenticator)
- Verification TOTP lors du login
- Backup codes (recovery)
- UI setup 2FA

**Must NOT do:**
- Ne PAS implementer SMS 2FA (TOTP uniquement)
- Ne PAS stocker secrets TOTP en clair

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 3
- Blocked By: Task 1, Task 8

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Setup 2FA et login
  Tool: Bash + Playwright
  Steps:
    1. User active 2FA
    2. QR code affiche
    3. Validation code TOTP
    4. Login avec TOTP
  Expected Result: 2FA fonctionnel
  Evidence: .sisyphus/evidence/task10-2fa.log

**Commit:** YES
- Message: feat(auth): TOTP 2FA with QR setup and backup codes
- Files: server/auth/totp.c, mobile/screens/Setup2FAScreen.js

---

### Task 11: Messages Vocaux (Opus)

**What to do:**
- Recording audio (expo-av)
- Encodage Opus (libopus via C bridge)
- Envoi via protocole existant
- Playback audio
- Limite 5 minutes

**Must NOT do:**
- Ne PAS depasser 5 minutes
- Ne PAS compresser en MP3 (Opus uniquement)

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 3
- Blocked By: Task 1, Task 8

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Enregistrement et envoi vocal
  Tool: Playwright
  Steps:
    1. Appuyer bouton micro
    2. Enregistrer 10 secondes
    3. Envoyer
    4. Destinataire joue le message
  Expected Result: Messages vocaux fonctionnels
  Evidence: .sisyphus/evidence/task11-voice.log

**Commit:** YES
- Message: feat(voice): Opus-encoded voice messages with 5min limit
- Files: mobile/components/VoiceRecorder.js, core/audio/opus.c

---

### Task 12: Transfert Fichiers (HTTP Range Requests)

**What to do:**
- Serveur HTTP avec support Range Requests (libmicrohttpd ou nginx)
- Upload fichiers: POST multipart, chiffrement cote client avant upload
- Stockage fichiers chiffres sur serveur
- Download avec Range Requests pour resume/pause
- Progress bar cote client
- Limite 100 Mo
- Chiffrement fichiers (E2EE via libsodium)
- **Note**: Changement par rapport au plan initial (BitTorrent → HTTP recommandé par Momus pour simplification)

**Must NOT do:**
- Ne PAS depasser 100 Mo (verifier avant upload)
- Ne PAS stocker fichiers en clair sur serveur (toujours chiffrer)
- Ne PAS utiliser P2P (centralise uniquement)

**Recommended Agent Profile:**
- Category: deep
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 3
- Blocked By: Task 1, Task 8

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Upload et download fichier
  Tool: Bash + Playwright
  Steps:
    1. User A selectionne fichier 10 Mo
    2. Client chiffre le fichier avec cle E2EE
    3. Upload HTTP POST vers serveur avec progress
    4. Serveur stocke fichier chiffre
    5. User B telecharge avec HTTP Range Request
    6. Client dechiffre et verifie md5sum match
  Expected Result: Transfert HTTP fonctionnel avec E2EE
  Evidence: .sisyphus/evidence/task12-file-transfer.log

Scenario: Limite 100 Mo
  Tool: Bash
  Steps:
    1. Tenter envoyer fichier 101 Mo
    2. Assert: erreur "File too large (max 100 MB)" cote client
  Expected Result: Limite respectee
  Evidence: .sisyphus/evidence/task12-file-limit.log

**Commit:** YES
- Message: feat(files): HTTP Range file transfer with client-side E2EE encryption
- Files: core/files/, server/http/

---

### Task 13: WebRTC Signalisation

**What to do:**
- Serveur de signalisation (WebSocket securise sur TLS 1.3)
- Echange SDP (Session Description Protocol)
- Echange ICE candidates
- Gestion rooms d'appel (1:1)

**Must NOT do:**
- Ne PAS implementer group calls (1:1 uniquement)
- Ne PAS stocker metadonnees d'appel longtemps

**Recommended Agent Profile:**
- Category: deep
- Skills: []

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 4
- Blocks: Task 14, Task 15
- Blocked By: Task 1, Task 9

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Echange SDP via signalisation
  Tool: Bash
  Steps:
    1. User A initie appel
    2. SDP offer envoye a User B
    3. User B cree answer
    4. SDP answer envoye a User A
    5. ICE exchange demarre
  Expected Result: Signalisation WebRTC fonctionnelle
  Evidence: .sisyphus/evidence/task13-signaling.log

**Commit:** YES
- Message: feat(webrtc): signaling server for 1:1 calls
- Files: server/signaling/, core/webrtc/

---

### Task 14: Appels Vocaux

**What to do:**
- UI appel (ecran appel entrant/sortant)
- Integration WebRTC audio
- Codecs audio (Opus)
- Gestion mute/unmute
- Fin d'appel

**Must NOT do:**
- Ne PAS oublier de liberer ressources WebRTC apres appel

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 4
- Blocks: Task 17
- Blocked By: Task 1, Task 13

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Appel vocal complet
  Tool: Playwright
  Steps:
    1. User A appelle User B
    2. User B decroche
    3. Connexion audio etablie sous 3 secondes
    4. Audio bidirectionnel fonctionnel
    5. Raccrocher
  Expected Result: Appels vocaux fonctionnels
  Evidence: .sisyphus/evidence/task14-voice-call.log

**Commit:** YES
- Message: feat(calls): 1:1 voice calls with WebRTC
- Files: mobile/screens/CallScreen.js, core/webrtc/audio.c

---

### Task 15: Appels Video (OPTIONNEL post-MVP)

**Status**: Cette tache est OPTIONNELLE et peut etre reportee apres le MVP si les vagues 1-3 prennent plus de temps que prevu.

**What to do:**
- Capture camera (expo-camera)
- Encodage video (H.264 via WebRTC)
- Affichage local + distant
- Switch camera (avant/arriere)
- Mode audio-only fallback
- **Note**: Recommande par Momus comme optionnel pour reduire le scope et les risques

**Must NOT do:**
- Ne PAS oublier permission camera
- Ne PAS bloquer la release si non implemente (optionnel)

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 4 (ou Post-MVP)
- Blocks: Task 17 (si implemente)
- Blocked By: Task 1, Task 13

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Appel video complet
  Tool: Playwright
  Steps:
    1. User A appelle User B en video
    2. Video locale et distante affichees
    3. Switch camera fonctionnel
    4. Raccrocher
  Expected Result: Appels video fonctionnels
  Evidence: .sisyphus/evidence/task15-video-call.log

**Commit:** YES
- Message: feat(calls): 1:1 video calls with camera switching
- Files: mobile/screens/VideoCallScreen.js, core/webrtc/video.c

---

### Task 16: Notifications FCM

**What to do:**
- Integration Firebase Cloud Messaging
- Configuration Expo notifications
- Envoi notifications serveur
- Gestion token FCM
- Notifications en arriere-plan

**Must NOT do:**
- Ne PAS inclure contenu message dans notification

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: []

**Parallelization:**
- Can Run In Parallel: YES
- Parallel Group: Wave 5
- Blocks: Task 17
- Blocked By: Task 1, Task 9

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Notification push recue
  Tool: Bash
  Steps:
    1. User A envoie message a User B
    2. User B en arriere-plan
    3. Notification push recue sous 5 secondes
    4. Tap notification ouvre l'app
  Expected Result: Notifications push fonctionnelles
  Evidence: .sisyphus/evidence/task16-fcm.log

**Commit:** YES
- Message: feat(notifications): FCM push notifications for Android
- Files: mobile/notifications/, server/notifications/

---

### Task 17: UI/UX Polish

**What to do:**
- Design system (couleurs, typographie, composants)
- Animations transitions
- Dark mode
- Responsive (tablettes)
- Loading states
- Error handling UI
- Accessibility

**Must NOT do:**
- Ne PAS implementer de themes personnalisables

**Recommended Agent Profile:**
- Category: visual-engineering
- Skills: [frontend-ui-ux]

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 5
- Blocks: Task 18
- Blocked By: Task 1, Task 14, Task 15, Task 16

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Design system coherent
  Tool: Playwright
  Steps:
    1. Naviguer dans toutes les ecrans
    2. Meme palette de couleurs partout
    3. Meme typographie
    4. Dark mode supporte sur tous les ecrans
  Expected Result: Design system coherent
  Evidence: Screenshots .sisyphus/evidence/task17-design-*.png

**Commit:** YES
- Message: feat(ui): design system, dark mode, and polish
- Files: mobile/components/, mobile/theme/

---

### Task 18: Tests Integration Complets

**What to do:**
- Scenarios utilisateurs end-to-end
- Tests performance (latence, throughput)
- Tests securite (basique)
- Tests charge (simuler 100 users)

**Must NOT do:**
- Ne PAS negliger les tests negatifs

**Recommended Agent Profile:**
- Category: unspecified-high
- Skills: []

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 5
- Blocks: Task 19
- Blocked By: Task 1-17

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Test scenario utilisateur complet
  Tool: Bash
  Steps:
    1. Enregistrer User A et User B
    2. Setup 2FA pour User A
    3. User A envoie message texte a User B
    4. User B repond
    5. User A envoie message vocal
    6. User B ecoute
    7. User A envoie fichier 50 Mo
    8. User B telecharge
    9. User A appelle User B (video)
    10. Appel dure 2 minutes
    11. Raccrocher
    12. Historique complet preserve
  Expected Result: Flux complet fonctionnel
  Evidence: .sisyphus/evidence/task18-e2e.log

Scenario: Test charge
  Tool: Bash
  Steps:
    1. Simuler 100 users simultanes
    2. Chaque user envoie 10 messages
    3. Serveur repond < 200ms (p95)
    4. Pas de crash
    5. Pas de perte messages
  Expected Result: Performance acceptable
  Evidence: .sisyphus/evidence/task18-load.log

**Commit:** YES
- Message: test(integration): end-to-end test suite
- Files: tests/integration/

---

### Task 19: Documentation + Deploiement

**What to do:**
- README.md (installation, developpement)
- Documentation architecture (ARCHITECTURE.md)
- Guide deploiement serveur (DEPLOY.md)
- Guide contribution (CONTRIBUTING.md)
- Scripts Docker pour serveur
- Scripts build release (APK, IPA, EXE)

**Must NOT do:**
- Ne PAS oublier de documenter les variables d'environnement

**Recommended Agent Profile:**
- Category: writing
- Skills: []

**Parallelization:**
- Can Run In Parallel: NO
- Parallel Group: Wave 5
- Blocks: None
- Blocked By: Task 1-18

**Acceptance Criteria:**

**Agent-Executed QA Scenarios:**

Scenario: Build release reussi
  Tool: Bash
  Steps:
    1. Build Android: APK genere
    2. Build iOS: IPA genere
    3. Build Windows: EXE genere
    4. Build Docker: image cree
  Expected Result: Builds release fonctionnels
  Evidence: .sisyphus/evidence/task19-builds.log

Scenario: Documentation complete
  Tool: Bash
  Steps:
    1. README.md existe avec installation rapide
    2. ARCHITECTURE.md existe
    3. DEPLOY.md existe avec deploiement serveur
    4. Variables d'environnement documentees
  Expected Result: Documentation prete pour nouveaux devs
  Evidence: .sisyphus/evidence/task19-docs.log

**Commit:** YES (final)
- Message: docs: complete documentation and deployment guides
- Files: README.md, docs/, docker-compose.yml
