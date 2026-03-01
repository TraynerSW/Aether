import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Scanner;
import java.util.List;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

public class Server {
	private List<Userprofile> liClients = new CopyOnWriteArrayList<>();			// CopyOnWrite crée une copie pour la modifier (évite les crash de threads)
	public static Time time = new Time();										// static pour l'utiliser via ClientHandler
	// public static String lastUsername = "";

	// Envoie un message à tous les clients connectés \\
	public void sendToAll(String msg) {
		for (Userprofile user: liClients) {
			// Si le tunnel avec le client existe \\
			if (user.getTunnel() != null) {
				user.getTunnel().println("/SERVER_MESSAGE " + msg);
			}
		}
	}

	// Envoie un message à un client spécifique \\
	public void send(String IP, String msg) {
		for (Userprofile user: liClients) {
			if (user.getIP().equals(IP) && user.getTunnel() != null) {
				user.getTunnel().println("/SERVER_MESSAGE " + msg);
			}
		}
	}

	public void command(String command, ServerSocket clientSocket) {
		// Aide et explication des commandes \\
		if (command.equals("/help")) {
			System.out.println(
				"-".repeat(70) + "\n" +
				"  ==========================  COMMANDES  ===========================\n\n" +
				"/stop : Arrête le serveur\n" +
				"/restart : Redémarre le serveur\n" +
				"/client : Liste des pseudos connectés\n" +
				"/ip : Liste des IPs connectées\n" +
				"/kick [IP] : Déconnecte un client\n" +
				"/kickAll : Déconnecte tous les clients\n" +
				"/send [IP] [msg] : Envoie un message à un client\n" +
				"/sendAll [msg] : Envoie un message à tous les clients\n" +
				"-".repeat(70)
			);
		}
		// Commande d'arrêt du serveur \\
		else if (command.equals("/stop")) {
			System.out.println(time.full() + "Arrêt total du serveur...");
			System.exit(0);
		}
		// Commande de redémarrage du serveur \\
		else if (command.equals("/restart")) {
			System.out.println(time.full() + "Redémarrage du serveur...");

			for (Userprofile user : liClients) {
				user.getTunnel().close();
			}
			try {
				clientSocket.close();
				Thread.sleep(1000);
			} catch (Exception eClientSocket) {
				eClientSocket.printStackTrace();
			}
		}
		// Commande d'affichage des pseudos connectés au serveur \\
		else if (command.equals("/client")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucun client n'est connecté au serveur.");
			}
			else {
				if (liClients.size() == 1) {
					System.out.print(time.full() + "Connecté : ");
					System.out.print(liClients.getFirst().getUsername());
				}
				else {
					System.out.print(time.full() + "Connectés : ");
					System.out.print(liClients.getFirst().getUsername());

					for (int i=1; i<liClients.size(); i++) {
						Userprofile user = liClients.get(i);
						System.out.print(", " + user.getUsername());
					}
				}
				System.out.print("\n");
			}
		}
		// Commande d'affichage des IPs connectées au serveur \\
		else if (command.equals("/clientIP")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucune IP n'est connectée au serveur.");
			}
			else {
				System.out.print("¨Connectés : ");

				if (liClients.size() == 1) {
					Userprofile firstUser = liClients.getFirst();
					System.out.print(firstUser.getUsername() +" ("+ firstUser.getIP() +")");
				}
				else {
					System.out.print(liClients.getFirst().getIP());

					for (int i=1; i<liClients.size(); i++) {
						Userprofile user = liClients.get(i);
						System.out.print(", " + user.getUsername() +" ("+ user.getIP() +")");
					}
				}
				System.out.print("\n");
			}
		}
		// Commande pour déconnecter tous les clients \\
		else if (command.startsWith("/kickAll")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucun client n'est connecté.");
			}
			else {
				for (Userprofile user : liClients) {
					user.getTunnel().println("/KICK");
					user.getTunnel().close();
					liClients.remove(user);
				}
			}
		}
		// Commande pour déconnecter un client \\
		else if (command.startsWith("/kick")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucun client n'est connecté.");
			}
			else {
				if (command.contains(" ")) {
					String IP = command.split(" ")[1];

					boolean isFound = false;
					for (Userprofile user : liClients) {
						if (user.getIP().equals(IP)) {
							user.getTunnel().println("/KICK");
							user.getTunnel().close();
							liClients.remove(user);
							isFound = true;
						}
					}
					if (!isFound) {
						System.out.println("L'IP "+ IP +" n'est associée à aucun client connecté.");
					}
				}
				else {
					System.out.println("Tu n'as pas renseigné l'IP du client.");
				}
			}
		}
		// Commmande d'envoi de message à tous les clients \\
		else if (command.startsWith("/sendAll")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucun client n'est connecté.");
			}
			else {
				if (command.contains(" ")) {
					String msg = command.split(" ", 2)[1];				// Limite à 1 pour prendre tout le message après le premier espace

					if (msg.isBlank()) {
						System.out.println("Tu ne peux pas envoyer de message vide.");
					}
					else {
						sendToAll(msg);
					}
				}
				else {
					System.out.println("Tu ne peux pas envoyer de message vide.");
				}
			}
		}
		// Commmande d'envoi de message à un client \\
		else if (command.startsWith("/send")) {
			if (liClients.isEmpty()) {
				System.out.println("Aucun client n'est connecté.");
			}
			else {
				if (command.contains(" ")) {
					String IP = command.split(" ")[1];

					if (IP.isEmpty()) {
						System.out.println("Tu n'as pas renseigné l'IP du destinataire.");
					}
					else {
						boolean isFound = false;
						for (Userprofile user: liClients) {
							if (user.getIP().equals(IP)) {
								isFound = true;
							}
						}
						if (!isFound) {
							System.out.println("L'IP "+ IP +" n'est associée à aucun client connecté.");
						}
						else {
							if (command.split(" ",3).length < 3) {
								System.out.println("Tu ne peux pas envoyer de message vide.");
							} else {
								String msg = command.split(" ", 3)[2];

								if (msg.isBlank()) {
									System.out.println("Tu ne peux pas envoyer de message vide.");
								}
								// Envoie le message au client s'il est connecté \\
								else {
									boolean hasBeenSent = false;
									for (Userprofile user : liClients) {
										if (user.getIP().equals(IP)) {
											send(IP, msg);
											System.out.println(time.full() + msg + " -> " + user.getUsername() +" ("+ IP +")");
											hasBeenSent = true;
										}
									}
									if (!hasBeenSent) {
										System.out.println("L'IP " + IP + " n'est associée à aucun client connecté.");
									}
								}
							}
						}
					}
				}
				else {
					System.out.println("Tu n'as pas renseigné l'IP du destinataire.");
				}
			}
		}
		// Commande non enregistrée \\
		else {
			System.out.println(command + " n'est pas une commande.");
		}
	}

	public void startServer(int port) {
		try (ServerSocket clientSocket = new ServerSocket(port)) {
			System.out.println("-".repeat(70));
			System.out.println(time.full() +"Serveur ouvert sur le port "+ port +".");

			// Gère l'envoi de messages du serveur aux clients \\
			Scanner msgServ = new Scanner(System.in);

			Thread sendMsgToClient = new Thread(() -> {
				while (true) {
					try {
						if (System.in.available() > 0) {
							String msg = msgServ.nextLine().strip();				// Récupère le message tapé sans espaces inutiles

							if (msg.startsWith("/")) command(msg, clientSocket);
							else if (!msg.isBlank()) {
								sendToAll(msg);
							}
						} else {
							// Met en pause le thread principal pour ne pas vérifier le clavier trop souvent \\
							Thread.sleep(70);
						}
					} catch (InterruptedException e) {
						return;							// Si le thread s'arrête pendant un Thread.sleep, ignore l'affichage de l'erreur
					} catch (Exception e) {
						System.out.println(time.full() +"Erreur inattendue du thread sendMsgToClient :\n");
						e.printStackTrace();
						return;
					}
				}
			});
			sendMsgToClient.start();

			// Boucle d'acceptation des clients \\
			while (true) {
				// Crée le socket du client \\
				try {
					Socket tubeToClient = clientSocket.accept();

					// Crée un clientHandler pour chaque nouveau client qui se connecte \\
					ClientHandler clientHandler = new ClientHandler(tubeToClient, liClients);

					Thread runClientHandler = new Thread(clientHandler::run);			// = clientHandler.run()
					runClientHandler.start();
				}
				// Si redémarrage du serveur : on sort de startServer() \\
				catch (SocketException eSocket) {
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args) {
		while (true) {
			System.out.println("-".repeat(70));
			System.out.println("  =====================  DÉMARRAGE DU SERVEUR  =====================");
			Server server = new Server();
			server.startServer(5000);
		}

	}
}

