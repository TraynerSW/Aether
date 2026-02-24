import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
	private List<Userprofile> liClients = new CopyOnWriteArrayList<>();			// CopyOnWrite crée une copie pour la modifier (évite les crash de threads)

	public void startServer(int port) {
		try (ServerSocket clientSocket = new ServerSocket(port)) {
			System.out.println("-".repeat(50));
			System.out.println("[Server] Serveur ouvert sur le port " + port + ".");

			// Gère l'envoi de messages du serveur aux clients \\
			Scanner msgServ = new Scanner(System.in);

			Thread sendMsgToClient = new Thread(() -> {
				while (true) {
					try {
						if (System.in.available() > 0) {
							String msg = msgServ.nextLine().strip();

							if (msg.equalsIgnoreCase("/stop")) {
								System.out.println("[Server] Arrêt total du serveur...");
								System.exit(0);
							}
							else if (msg.equalsIgnoreCase("/restart")) {
								System.out.println("[Server] Redémarrage du serveur...");

								for (Userprofile user: liClients) {
									user.getTunnel().close();
								}

								clientSocket.close();
								Thread.sleep(1000);
								return;
							}
							// Affiche la liste des clients connectés au serveur (pseudos) \\
							else if (msg.equalsIgnoreCase("/client")) {
								if (liClients.isEmpty()) {
									System.out.println("Aucune IP n'est connectée au serveur.");
								}
								else {
									if (liClients.size() == 1) {
										System.out.print("[Server] Connecté : ");
										System.out.print(liClients.getFirst().getUsername());
									} else {
										System.out.print("[Server] Connectés : ");
										System.out.print(liClients.getFirst().getUsername());
										for (Userprofile e : liClients) {
											System.out.print(", " + e.getUsername());
										}
									}
									System.out.print("\n");
								}
							}
							// Affiche la liste des clients connectés au serveur (IPs) \\
							else if (msg.equalsIgnoreCase("/ip")) {							// IgnoreCase pour maj et min
								if (liClients.isEmpty()) {
									System.out.println("Aucune IP n'est connectée au serveur.");
								}
								else {
									System.out.print("[Server] IPs connectées : ");

									if (liClients.size() == 1) {
										System.out.print(liClients.getFirst().getIP());
									}
									else {
										for (Userprofile e : liClients) {
											System.out.print(e.getIP() + ", ");
										}
									}
									System.out.print("\n");
								}
							// Envoie un message à tous les clients connectés
							} else if (!msg.isBlank() && !msg.startsWith("/")) {            // Si que des espaces ou rien d'écrit
								for (Userprofile user: liClients) {
									if (user.getTunnel() != null) {		// Si le tunnel existe bien
										user.getTunnel().println(msg);
									}
								}
							}
						} else {
							Thread.sleep(50);			// Met en pause le thread principal pour ne pas vérifier le clavier trop souvent
						}
					} catch (InterruptedException e) {
						return;							// Si le thread s'arrête pendant un Thread.sleep, ignore l'affichage de l'erreur
					} catch (Exception e) {
						System.out.println("[Server] Erreur inattendue du thread sendMsgToClient :\n");
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
					Socket client = clientSocket.accept();

					// Crée un clientHandler pour chaque nouveau client qui se connecte \\
					ClientHandler clientHandler = new ClientHandler(client, liClients);

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
			System.out.println("-".repeat(50));
			System.out.println(" ============= DÉMARRAGE DU SERVEUR =============");
			Server server = new Server();
			server.startServer(5000);
		}

	}
}

