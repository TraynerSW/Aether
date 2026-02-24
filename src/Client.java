import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {									// statique : variable globale modifiable depuis les threads
	static volatile boolean isServerON = false;			// volatile : force les threads à utiliser la valeur de la mémoire centrale (pas de leur cache)
	static boolean isSetUsername = false;

	public static void main(String[] args) {
		System.out.println("\r" + "-".repeat(51));
		System.out.println(" ============== DÉMARRAGE DU CLIENT ==============");

		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois
		Thread attente = null;

		mainLoop: while (true) {
			// Try with resources, pour fermer le socket si on sort de la boucle : pas de fuite de mémoire
			try (Socket serverSocket = new Socket("localhost", 5000)) {                // Si cette connexion passe, tu as le handshake.
				boucle = false;			// Pour afficher à nouveau le "Tu es déconnecté" si la connection se coupe.
				isServerON = true;

				if (attente != null) {				// Si le thread n'est pas encore lancé
					attente.interrupt();
				}

				String servAdr = serverSocket.getInetAddress().getHostAddress();		// Inet/Local = Son/Mon adresse
				OutputStream toServ = serverSocket.getOutputStream();	// Entrée du serveur (sortie du client)
				PrintWriter sendServ = new PrintWriter(toServ, true);

				System.out.println("\r" + "-".repeat(51));
				System.out.println("Tu es bien connecté au serveur " + servAdr + ".");

				// Gestion du pseudo à la connexion \\
				Scanner msgUser = new Scanner(System.in);
				Scanner msgFromServ = new Scanner(serverSocket.getInputStream());		// Nécessaire pour écouter le serveur

				// Réception des messages du serveur \\
				Thread receiveMsgFromServer = new Thread(() -> {
					while (true) {
						try {
							if (!msgFromServ.hasNextLine()) {        // Si le serveur s'arrête ou si le client se déconnecte
								System.out.println("\r[Client] Tu t'es déconnecté du serveur.");
								Thread.sleep(1000);
								isServerON = false;
								isSetUsername = false;
								return;
							}
							String msgServ = msgFromServ.nextLine();
							System.out.println("[Serveur] " + msgServ);
							Thread.sleep(50);
						} catch (Exception eReceiveMsg) {
							System.out.println("[Client] Erreur lors de la réception d'un message du serveur :\n");
							eReceiveMsg.printStackTrace();
							isServerON = false;
							return;
						}
					}
				});
				receiveMsgFromServer.start();

				System.out.print("Quel est ton pseudo ? ");

				// Boucle d'envoi / réception de messages (tant que le client est connecté) \\
				while (true) {
					if (isServerON) {
						try {
							// System.in.available regarde à la milliseconde près si l'utilisateur a déjà appuyé sur Entrée.
							// > 0 : l'utilisateur a écrit un message et a appuyé sur Entrée.
							// = 0 : le programme tourne sans être bloqué par nextLine().
							if (System.in.available() > 0) {
								String msgClient = msgUser.nextLine().strip();		// Message écrit de l'utilisateur (sans espaces inutiles)

								if (msgClient.equals("/exit")) {
									System.out.print("[Client] Déconnexion et arrêt du client...");
									System.exit(0);					// Si l'utilisateur décide de se déconnecter, arrête le programme
								}
								else if (msgClient.equals("/restart")) {
									System.out.print("[Client] Redémarrage du client...");
									try {
										Thread.sleep(1000);					// "fausse" pause pour ne pas perdre l'utilisateur
										serverSocket.close();
										receiveMsgFromServer.join();		// Pour être sûr que le thread se mette en pause
										continue mainLoop;
									} catch (InterruptedException e) {
										return;
									}
								}
								// Si le message/pseudo est vide
								else if (msgClient.isBlank() && !isSetUsername) {
									System.out.println("\r" + "-".repeat(51));
									System.out.println("Tu ne peux pas envoyer de pseudo vide.");
									Thread.sleep(1000);
									System.out.print("Quel est ton pseudo ? ");
								}
								// Si le message est ni vide ni une tentative de commande
								else if (!msgClient.isBlank() && !msgClient.startsWith("/")) {
									if (!sendServ.checkError()) {			// Si aucune erreur avec le serveur
										sendServ.println(msgClient);
										if (!isSetUsername) {
											isSetUsername = true;
											System.out.println("-".repeat(51));
										}
									}
									else {
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
					System.out.println("\r" + "-".repeat(51));
					System.out.println("Tu es hors-ligne.");
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
					}
				}
			}
		}
	}
}
