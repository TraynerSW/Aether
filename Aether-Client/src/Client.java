import java.util.Scanner;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.OutputStream;

public class Client {
	static volatile boolean isServerON = false;				// volatile : force les threads à utiliser la valeur de la mémoire centrale (pas de leur cache)
	static volatile boolean isUsernameAllowed = true;		// statique : variable globale modifiable depuis les threads
	static boolean isSetUsername = false;

	public static void main(String[] args) {
		System.out.println("\r" + "-".repeat(71));
		System.out.println("  ======================  DÉMARRAGE DU CLIENT  ======================");
		System.out.println("\r" + "-".repeat(71));

		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois
		Thread attente = null;
		Time time = new Time();

		mainLoop: while (true) {
																						// Try with resources, pour fermer le socket si on sort de la boucle : pas de fuite de mémoire
			try (Socket serverSocket = new Socket("localhost", 5000)) {					// Si cette connexion passe, tu as le handshake.
				boucle = false;															// Pour afficher à nouveau le "Tu es déconnecté" si la connection se coupe.
				isServerON = true;

				if (attente != null) {				// Si le thread n'est pas encore lancé
					attente.interrupt();
				}

				String servAdr = serverSocket.getInetAddress().getHostAddress();		// Inet/Local = Son/Mon adresse
				OutputStream toServ = serverSocket.getOutputStream();					// Entrée du serveur (sortie du client)
				PrintWriter sendServ = new PrintWriter(toServ, true);

				System.out.println("\rTu es bien connecté au serveur "+servAdr+".");

				// Gestion du pseudo à la connexion \\
				Scanner msgUser = new Scanner(System.in);
				Scanner msgFromServ = new Scanner(serverSocket.getInputStream());		// Nécessaire pour écouter le serveur

				// Réception des messages du serveur \\
				Thread receiveMsgFromServer = new Thread(() -> {
					while (true) {
						try {
							if (!msgFromServ.hasNextLine()) {        					// Si le serveur s'arrête ou si le client se déconnecte
								System.out.println("\rTu es déconnecté du serveur.");
								System.out.println("\r" + "-".repeat(71));

								Thread.sleep(1000);
								isServerON = false;
								isSetUsername = false;
								isUsernameAllowed = true;
								return;
							}
							String msgServ = msgFromServ.nextLine();

							// Pour recevoir le message d'erreur
							if (msgServ.equals("/ERROR_USERNAME")) {
								isUsernameAllowed = false;
							}
							else if (msgServ.equals("/KICK")) {
								System.out.println("\rTu t'es fait kick.");
								Thread.sleep(1000);
							}
							else if (msgServ.startsWith("/SERVER_MESSAGE")){
								System.out.println("\r[Server] " + msgServ.split(" ",2)[1]);
							}
							else {
								System.out.println("\r" + msgServ);
							}

							Thread.sleep(50);
						} catch (Exception eReceiveMsg) {
							System.out.println("[Debug] Erreur lors de la réception d'un message du serveur :\n");
							eReceiveMsg.printStackTrace();
							isServerON = false;
							return;
						}
					}
				});
				receiveMsgFromServer.start();

				System.out.print("Quel est ton pseudo ? ");

				// Pour afficher les erreurs lors de la création du pseudo \\
				String retryMsg = "";

				// Boucle d'envoi / réception de messages (tant que le client est connecté) \\
				while (true) {
					if (isServerON) {
						try {
							// System.in.available regarde à la milliseconde près si l'utilisateur a déjà appuyé sur Entrée.
							// > 0 : l'utilisateur a écrit un message et a appuyé sur Entrée.
							// = 0 : le programme tourne sans être bloqué par nextLine().
							if (System.in.available() > 0) {
								String msg = msgUser.nextLine().strip();		// Message écrit de l'utilisateur (sans espaces inutiles)

								// Si erreur de communication avec le serveur \\
								if (sendServ.checkError()) {
									System.out.println("Le serveur est injoignable.");
									receiveMsgFromServer.interrupt();
									break;
								}
								// Commande de déconnexion \\
								else if (msg.equals("/exit")) {
									System.out.print("Arrêt du client...");
									System.exit(0);					// Si l'utilisateur décide de se déconnecter, arrête le programme
								}
								// Commande de redémarrage \\
								else if (msg.equals("/restart")) {
									System.out.println("Redémarrage du client...");
									try {
										Thread.sleep(1000);					// "fausse" pause pour ne pas perdre l'utilisateur
										serverSocket.close();
										receiveMsgFromServer.join();		// Pour être sûr que le thread se mette en pause
										continue mainLoop;
									} catch (InterruptedException e) {
										return;
									}
								}
								// Si c'est un pseudo \\
								else if (!isSetUsername) {
									if (msg.startsWith("/")) {
										retryMsg = msg +" n'est pas une commande valide.";
									}
									else if (msg.isBlank()) {
										retryMsg = "Tu dois renseigner un pseudo.";
									}
									else if (msg.startsWith("[")) {
										retryMsg = "Ton pseudo est incorrect.";
									}
									else if (msg.contains(" ")) {
										retryMsg = "Ton pseudo doit être en un seul mot.";
									}
									// Si le pseudo est au bon format \\
									else {
										isUsernameAllowed = true;
										sendServ.println(msg);
										Thread.sleep(200);					// Avec cette pause, si le serveur envoie "/ERROR_USERNAME, isUsernameAllowed est mis à false.
										if (!isUsernameAllowed) {
											retryMsg = "Le pseudo existe déjà.";
										}
										else {
											isSetUsername = true;
											isUsernameAllowed = true;
											System.out.println("Bienvenue sur Aether, "+ msg +" !\n"+ "-".repeat(71));
										}
									}
									// Si le client doit retenter la création du pseudo \\
									if (!retryMsg.isEmpty()) {
										System.out.println("\r" + "-".repeat(71) +"\n"+ retryMsg);
										Thread.sleep(1000);
										System.out.print("Quel est ton pseudo ? ");
										retryMsg = "";									// Réinitialisation de la variable
									}
								}
								// Si c'est un message \\
								else {
									// Aide et explication des commandes \\
									if (msg.equals("/help")) {
										System.out.println(
											"-".repeat(70) + "\n" +
											"  ==========================  COMMANDES  ===========================\n\n" +
											"/exit : Arrête le client\n" +
											"/restart : Redémarre le client\n" +
											"/msg [IP] [msg] : Envoie un message à un client\n" +
											"/msgAll [msg] : Envoie un message à tous les clients\n" +
											"/DM [IP] : Démarre une conversation avec un client\n" +
											"-".repeat(70)
										);
									}
									// Commande pour envoyer un message à tous les utilisateurs \\
									else if (msg.startsWith("/msgAll")) {
										if (msg.contains(" ")) {
											sendServ.println(msg);
										}
										else {
											System.out.println("Tu ne peux pas envoyer un message vide.");
										}
									}
									// Commande pour envoyer un message à un utilisateur \\
									else if (msg.startsWith("/msg")) {
										if (msg.contains(" ")) {
											if (msg.split(" ").length < 2) {
												System.out.println("Tu ne peux pas envoyer un message vide.");
											}
											else {
												sendServ.println(msg);
											}
										}
										else {
											System.out.println("Tu dois renseigner une IP.");
										}
									}
									// Si le message est une tentative de commande \\
									else if (msg.startsWith("/")) {
										System.out.println(msg +" n'est pas une commande.");
									}
									// Message classique \\
									else if (!msg.isBlank()) {
										sendServ.println(msg);
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
				// Thread d'animation d'attente \\
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
							System.out.print("\rConnexion au serveur échouée.");
						}
					}
				});

				// Boucle de reconnexion \\
				if (!boucle) {
					boucle = true;
				}

				for (int i=4; i>=0; i--) {
					if (i==0) {
						attente.start();
						try {
							Thread.sleep(4000);
						} catch (InterruptedException eAttente) {
							attente.interrupt();
							//System.out.println("(Debug) Thread attente arrêté.");
							e.printStackTrace();		// Debug
						}
					}
					else if (i<4) {
						System.out.print("\rTentative de connexion dans " + i + " sec");
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
