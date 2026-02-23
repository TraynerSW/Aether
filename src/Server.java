import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.OutputStream;

public class Server {
	private List<Userprofile> liClients = new ArrayList<>();

	public void startServer(int port) {
		try (ServerSocket clientSocket = new ServerSocket(port)) {
			System.out.println("_".repeat(100));
			System.out.println("\n[Server] Serveur ouvert sur le port " + port + ".");

			// Boucle principale qui accepte la connexion du client \\
			while (true) {
				// Animation d'attente \\
				Thread attente = new Thread(() -> {
					while(true) {
						for (int i=0; i<4; i++) {
							if (i==0) {
								System.out.print("\r[Server] En attente de clients");
							} else {
								System.out.print(".");
							}
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				});
				attente.start();

				// Attend un new client ; crée un nouveau socket et le renvoie \\
				Socket client = clientSocket.accept();

				// Informations du client \\
				String clientAdr = client.getLocalAddress().getHostAddress(); 	// Récupère l'adresse du client sans le "/"
				int clientPort = client.getLocalPort();							// Récupère le port que le client a renseigné

				Scanner msgFromClient = new Scanner(client.getInputStream()); 	// Écoute le client (pseudo puis messages)

				if (!msgFromClient.hasNextLine()) {
					System.out.println("\r[Server]" + clientAdr + " s'est déconnecté.");
					attente.interrupt();
					continue;        // Si le client se déconnecte sans rien répondre, passe au prochain client/tour de boucle
				}
				attente.interrupt();

				// Ajout du client à la classe UserProfile \\
				String username = msgFromClient.nextLine().strip();
				Userprofile user = new Userprofile();

				if (!clientAdr.isBlank()) {
					user.setIP(clientAdr);
				} else {
					System.out.println("\n[Server] L'IP est vide.");
					System.out.println("[Server] Déconnexion du client...");
					continue;
				}

				if (!username.isBlank()) {
					user.setUsername(username);
				} else {
					System.out.println("\n[Server] Le pseudo est vide.");
					System.out.println("[Server] Déconnexion du client...");
					continue;
				}

				liClients.add(user);

				attente.interrupt();						// Dès qu'un client est trouvé, on stoppe le processus d'animation
				System.out.println("\r[Server] " + username + " (" + clientAdr + ":" + clientPort + ") s'est connecté.");
				OutputStream toClient = client.getOutputStream();

				// Envoi de message à tous les clients connectés
				PrintWriter msgToAllClients = new PrintWriter(toClient, true);		// True permet d'envoyer le msg direct au client connecté
				msgToAllClients.println("Bienvenue " + username + " !");

				Scanner msgServ = new Scanner(System.in);

				Thread sendMsgToClient = new Thread(() -> {
					while (true) {
						try {
							if (System.in.available() > 0) {
								String msg = msgServ.nextLine().strip();

								if (msg.equals("/stop")) {
									System.out.println("[Système] : Arrêt total du serveur en cours...");
									System.exit(0);
								} else if (!msg.isBlank() && !msg.startsWith("/")) {            // Si que des espaces ou rien d'écrit
									msgToAllClients.println(msg);
								}
							} else {
								Thread.sleep(50);			// Met en pause le thread principal pour ne pas vérifier le clavier trop souvent
							}
						} catch (InterruptedException e) {
							return;							// Si le thread s'arrête pendant un Thread.sleep, ignore l'affichage de l'erreur
						} catch (Exception e) {
							System.out.println("[Système] Erreur inattendue du thread sendMsgToClient :\n");
							e.printStackTrace();
							return;
						}
					}
				});
				sendMsgToClient.start();

				// Messages du client reçus par le serveur \\
				while (true) {
					try {
						if (!msgFromClient.hasNextLine()) {
							System.out.println(username + " s'est déconnecté.");
							sendMsgToClient.interrupt();
							break;
						} else {
							String msgClient = msgFromClient.nextLine();
							System.out.println(username + " : " + msgClient);
						}
					}
					catch (Exception eMsgClient) {
						System.out.println("[Système] Erreur lors de la réception du message du client " + clientAdr + " :\n");
						eMsgClient.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args) {
		Server server = new Server();
		server.startServer(5000);
	}
}

