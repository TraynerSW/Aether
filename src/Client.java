import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	static boolean serverActif = false;			// Variable globale MODIFIABLE depuis les threads

	public static void main(String[] args) {
		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois
		boolean isSetUsername = false;
		Thread attente = null;

		mainLoop: while (true) {
			// Try with resources, pour fermer le socket si on sort de la boucle : pas de fuite de mémoire
			try (Socket serverSocket = new Socket("localhost", 5000)) {                // Si cette connexion passe, tu as le handshake.
				boucle = false;			// Pour afficher à nouveau le "Tu es déconnecté" si la connection se coupe.
				serverActif = true;
				isSetUsername = false;

				if (attente != null) {				// Si le thread n'est pas encore lancé
					attente.interrupt();
					//System.out.println("(Debug) Thread attente arrêté.");
				}
				String servAdr = serverSocket.getInetAddress().getHostAddress();		// Inet/Local = Son/Mon adresse
				OutputStream toServ = serverSocket.getOutputStream();	// Entrée du serveur (sortie du client)
				PrintWriter sendServ = new PrintWriter(toServ, true);

				System.out.print("\n");
				System.out.println("_".repeat(100));
				System.out.println("\nTu es bien connecté au serveur " + servAdr + ".");

				// Gestion du pseudo à la connexion \\
				Scanner msgUser = new Scanner(System.in);
				Scanner msgFromServ = new Scanner(serverSocket.getInputStream());		// Nécessaire pour écouter le serveur

				// Réception des messages du serveur \\
				Thread receiveMsgFromServer = new Thread(() -> {
					while (true) {
						try {
							if (!msgFromServ.hasNextLine()) {        // Si le serveur s'arrête ou si le client se déconnecte
								System.out.println("\n[Serveur] Tu t'es déconnecté du serveur.");
								Thread.sleep(400);
								serverActif = false;
								return;
							}
							String msgServ = msgFromServ.nextLine();
							System.out.println("[Serveur] " + msgServ);
							Thread.sleep(50);
						} catch (Exception eReceiveMsg) {
							System.out.println("[Client] Erreur lors de la réception d'un message du serveur :\n");
							eReceiveMsg.printStackTrace();
							return;
						}
					}
				});
				receiveMsgFromServer.start();

				if (!isSetUsername) {
					System.out.print("Quel est ton pseudo ? ");
				}
				// Boucle d'envoi / réception de messages (tant que le client est connecté) \\
				while (true) {
					if (serverActif) {
						try {
							// System.in.available regarde à la milliseconde près si l'utilisateur a déjà appuyé sur Entrée.
							// > 0 : l'utilisateur a écrit un message et a appuyé sur Entrée.
							// = 0 : le programme tourne sans être bloqué par nextLine().
							if (System.in.available() > 0) {
								String msgClient = msgUser.nextLine().strip();		// Message écrit de l'utilisateur (sans espaces inutiles)

								if (msgClient.equals("/exit")) {
									System.out.println("[Client] Déconnexion en cours...");
									System.exit(0);					// Si l'utilisateur décide de se déconnecter, arrête le programme
								}
								else if (msgClient.equals("/restart")) {
									System.out.print("[Client] Redémarrage du client en cours...");
									try {
										Thread.sleep(1000);			// Fait une "fausse" pause pour ne pas aller trop vite
										serverSocket.close();
										receiveMsgFromServer.join();		// Pour être sûr que le thread se mette en pause
										continue mainLoop;
									} catch (InterruptedException e) {
										return;
									}
								}
								else if (!isSetUsername && msgClient.isBlank()) {
									System.out.print("\rTu ne peux pas envoyer de pseudo vide.");
									Thread.sleep(1500);
									System.out.print("\rQuel est ton pseudo ? ");
								}
								else {
									if (!sendServ.checkError()) {			// Si pas d'erreur avec le serveur
										if (!isSetUsername) {
											isSetUsername = true;
											System.out.println("_".repeat(100) + "\n");
										}
										if (!msgClient.isBlank()) {			// Si le message est vide, ne l'envoie pas.
											sendServ.println(msgClient);
										}
									} else {
										System.out.println("Le serveur est injoignable.");
										receiveMsgFromServer.interrupt();
										break;
									}
								}
							} else {
								Thread.sleep(50);			// Met en pause le thread principal pour ne pas vérifier le clavier trop souvent
							}
						} catch (Exception eSendMsg) {
							System.out.println("Erreur lors de l'envoi du message :\n");
							eSendMsg.printStackTrace();
						}
					} else {
						break;
					}
				}
			} catch (Exception e) {
				// Thread d'attente de reconnexion
				attente = new Thread(() -> {
					for (int i=0; i<3; i++) {
						System.out.print("\rTentative de connexion au serveur");
						for (int j=0; j<4; j++) {
							if (j > 0) {
								System.out.print(".");
							}
							try {
								Thread.sleep(500);
							} catch (InterruptedException ex) {
								return;
							}
						}
						if (i == 2) {
							System.out.print("\rReconnexion échouée.");
						}
					}
				});

				// Boucle de reconnexion \\
				if (!boucle) {
					System.out.println("_".repeat(100));
					System.out.println("\nTu es offline.");
					boucle = true;
				}

				for (int i=4; i>=0; i--) {
					if (i==0) {
						attente.start();
						try {
							Thread.sleep(4000);
						} catch (InterruptedException eAttente) {
							attente.interrupt();
							System.out.println("(Debug) Thread attente arrêté.");
							e.printStackTrace();		// Debug
						}
					}
					else if (i<4) {
						System.out.print("\rTentative de reconnexion dans " + i + " sec");
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						attente.interrupt();
						//System.out.println("(Debug) Thread attente arrêté.");
					}
				}
			}
		}
	}
}
