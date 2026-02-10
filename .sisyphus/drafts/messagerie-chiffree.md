# Brouillon: Application Mobile de Messagerie Centralisée Chiffrée

## Exigences Initiales (Source: Utilisateur)

### Fonctionnalités Demandées
- **Langage**: C (logique métier) + React Native/Expo (UI)
- **Type**: Application mobile + desktop
- **Architecture**: Centralisée avec éléments P2P (BitTorrent modifié)
- **Sécurité**: Chiffrement E2EE avec stockage serveur chiffré
- **Fonctionnalités**:
  - Messagerie texte
  - Messages vocaux (Opus)
  - Envoi de fichiers jusqu'à 100 Mo (BitTorrent modifié)
  - Appels vocaux et vidéo (WebRTC)
  - Groupes de discussion
  - Authentification 2FA (TOTP)

## Décisions Confirmées

### Architecture & Plateforme
- **Plateformes cibles**: Windows, Android, iOS
- **Architecture**: Hybride - Core en C, UI en React Native (Expo)
- **Communication Core↔UI**: Native Modules (JNA/JNI/JSI)
- **Protocole réseau**: QUIC/HTTP3
- **Notifications**: FCM (Firebase) pour Android

### Chiffrement & Sécurité
- **Type**: End-to-End Encryption (E2EE) avec stockage serveur chiffré
- **Gestion des clés**: Stockage sécurisé sur appareil (Keychain/Keystore)
- **Authentification**: 2FA avec TOTP

### Stockage
- **Serveur**: SQLite (base relationnelle embarquée)
- **Client**: Chiffrement local + stockage structuré

### Audio & Vidéo
- **Format audio**: Opus (codec moderne, optimisé voix)
- **Appels temps réel**: WebRTC (signalisation + médias)

### Transfert Fichiers
- **Protocole**: BitTorrent modifié (approche P2P)
- **Taille max**: 100 Mo
- **Chunking**: Oui, avec resume

### Tests
- **Stratégie**: Tests d'intégration uniquement
- **Validation**: Scénarios utilisateurs complets

## Stack Technique Proposée

### Client (C Core)
- **Réseau**: msquic (Microsoft QUIC) ou ngtcp2
- **Chiffrement**: libsodium (NaCl) - moderne, simple API
- **Audio**: libopus (encodage/décodage)
- **WebRTC**: libdatachannel ou gstreamer
- **BitTorrent**: libtorrent-rasterbar (modifié)
- **Base de données**: SQLite
- **Storage mobile**: Keychain (iOS), Keystore (Android), DPAPI (Windows)

### Client (React Native UI)
- **Framework**: Expo (managed workflow)
- **Navigation**: React Navigation
- **State**: Redux Toolkit ou Zustand
- **Storage local**: AsyncStorage + SecureStore
- **Audio recording**: expo-av
- **Permissions**: expo-permissions
- **FCM**: expo-notifications

### Serveur
- **Langage**: C (pour cohérence avec client)
- **Protocole**: QUIC/HTTP3 via msquic
- **Base de données**: SQLite
- **TOTP**: liboath ou implementation custom
- **Signaling WebRTC**: Serveur de signalisation custom
- **BitTorrent**: Tracker + DHT modifié

## Fonctionnalités IN (Scope)
- Messagerie 1-1 et groupes
- Messages texte chiffrés E2EE
- Messages vocaux (Opus)
- Fichiers jusqu'à 100 Mo (BitTorrent)
- Appels vocaux/vidéo (WebRTC)
- Authentification 2FA
- Notifications FCM (Android)
- Historique local chiffré
- Gestion des groupes (création, membres, droits)

## Fonctionnalités OUT (Hors scope)
- Appels de groupe (multiparty)
- Stories/status éphémères
- Stickers/personnalisation avancée
- Backup cloud des messages
- Web/desktop (hors Windows demandé)
- Messagerie hors connexion (P2P direct)
- Chiffrement parfait forward (perfect forward secrecy) - optionnel

## Risques Identifiés
1. **Complexité WebRTC**: Signalisation, ICE, codecs, NAT traversal
2. **BitTorrent modifié**: Adaptation pour usage centralisé + mobile
3. **Cross-platform C**: Compilation et linking sur 3 plateformes
4. **QUIC/HTTP3**: Maturité des bibliothèques C
5. **Performance mobile**: Consommation batterie avec WebSocket/QUIC persistant

## Questions Résolues
- ✅ Plateforme: Windows, Android, iOS
- ✅ Architecture: Hybride C + React Native
- ✅ Protocole: QUIC/HTTP3
- ✅ Chiffrement: E2EE avec libsodium
- ✅ Audio: Opus
- ✅ Fichiers: BitTorrent modifié
- ✅ Vidéo: WebRTC
- ✅ Tests: Intégration
- ✅ DB: SQLite

## Points de Vigilance (Guardrails)
- ⚠️ Ne pas implémenter de cryptographie maison - utiliser libsodium
- ⚠️ Gérer proprement les ressources natives (libérer la mémoire C)
- ⚠️ Valider tous les inputs utilisateur (sécurité)
- ⚠️ Gérer les déconnexions réseau gracieusement
- ⚠️ Limiter la consommation batterie sur mobile
- ⚠️ Assurer la compatibilité des formats entre plateformes
