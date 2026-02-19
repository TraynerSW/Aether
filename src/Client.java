import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	static boolean serverActif = false;			// Variable globale MODIFIABLE depuis les threads

	public static void main(String[] args) {
		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois
		Thread attente = null;

		while (true) {
			try {
				Socket serverSocket = new Socket("localhost", 5000);                // Si cette connexion passe, tu as le handshake.
				boucle = false;			// Pour afficher à nouveau le "Tu es déconnecté" si la connection se coupe.
				serverActif = true;

				if (attente != null) {				// Si le thread n'est pas encore lancé
					attente.interrupt();
					System.out.println("(Debug) Thread attente arrêté.");
				}
				String servAdr = serverSocket.getInetAddress().getHostAddress();		// Inet/Local = Son/Mon adresse
				System.out.println("\nTu es bien connecté au serveur " + servAdr + ".\n___________________________________________________\n");

				Scanner msgFromServ = new Scanner(serverSocket.getInputStream());		// Nécessaire pour écouter le serveur
				Scanner msgUser = new Scanner(System.in);

				// Boucle de réception des messages du serveur
				Thread receiveMsgFromServer = new Thread(() -> {
					while (true) {
						try {
							if (!msgFromServ.hasNextLine()) {        // Si le serveur s'arrête
								Thread.sleep(100);
								System.out.println("[Serveur] Le serveur vient de se déconnecter.\n_____________________________________________________\n");
								serverActif = false;
								return;
							}
							// Récupère le message du serveur
							String msgServ = msgFromServ.nextLine();
							System.out.println("[Serveur] " + msgServ);
							Thread.sleep(100);
						} catch (Exception eReceiveMsg) {
							System.out.println("Erreur lors de la réception d'un message du serveur :\n");
							eReceiveMsg.printStackTrace();
						}
					}
				});
				receiveMsgFromServer.start();

				// Boucle principale d'envoi et de réception de messages (tant que le client est connecté)
				OutputStream toServ = serverSocket.getOutputStream();	// Entrée du serveur (sortie du client)
				PrintWriter sendServ = new PrintWriter(toServ, true);

				while (true) {
					if (serverActif) {
						// Boucle principale d'envoi de messages au serveur
						try {
							// System.in.available regarde à la milliseconde près si l'utilisateur a déjà appuyé sur Entrée.
							// > 0 : l'utilisateur a écrit un message et a appuyé sur Entrée.
							// = 0 : le programme tourne sans être bloqué par nextLine().
							if (System.in.available() > 0) {
								// Message écrit de l'utilisateur
								String msgClient = msgUser.nextLine();

								// Si l'utilisateur décide de se déconnecter, arrête le programme complètement.
								if (msgClient.equals("/exit")) {
									System.out.println("[Client] Déconnexion en cours...");
									System.exit(0);
								} else {
									if (!sendServ.checkError()) {
										sendServ.println(msgClient);
									} else {
										System.out.println("Le serveur est injoignable.\n_____________________________________\n");
										receiveMsgFromServer.interrupt();
										//System.out.println("(Debug) Thread receiveMsgFromServer arrêté.");
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
				// Threads d'attente de reconnexion
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

				// Boucle de reconnexion
				if (!boucle) {
					System.out.println("Tu es déconnecté.");
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
