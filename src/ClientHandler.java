import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
	private Socket client;
	private List<Userprofile> liClients;

	public ClientHandler(Socket client, List<Userprofile> liClients) {
		this.client = client;
		this.liClients = liClients;
	}

	public List<Userprofile> getList() {
		return this.liClients;
	}

	public void run() {
		// Informations du client \\
		String clientAdr = client.getLocalAddress().getHostAddress();    // Récupère l'adresse du client sans le "/"
		int clientPort = client.getLocalPort();                            // Récupère le port que le client a renseigné

		try {
			Scanner msgFromClient = new Scanner(client.getInputStream());    // Écoute le client (pseudo puis messages)

			if (!msgFromClient.hasNextLine()) {
				System.out.println("[Server] " + clientAdr + " n'a pas réussi à se connecter.");
				return;
			}

			// Ajout du client à la classe UserProfile \\
			String username = msgFromClient.nextLine().strip();
			Userprofile user = new Userprofile();

			if (!clientAdr.isBlank()) {
				user.setIP(clientAdr);
			} else {
				System.out.println("\n[Server] L'IP est vide.");
				System.out.println("[Server] Déconnexion du client...");
				return;
			}

			if (!username.isBlank()) {
				user.setUsername(username);
			} else {
				System.out.println("\n[Server] Le pseudo est vide.");
				System.out.println("[Server] Déconnexion du client...");
				return;
			}

			// A COMPRENDRE
			user.setTunnel(new PrintWriter(client.getOutputStream(), true));

			user.getTunnel().println("Bienvenue " + username + " !");
			liClients.add(user);

			System.out.println("\r[Server] " + username + " ("+clientAdr+") s'est connecté.");

			// Messages du client reçus par le serveur \\
			while (true) {
				try {
					if (!msgFromClient.hasNextLine()) {
						System.out.println("[Server] " + username + " s'est déconnecté.");
						liClients.remove(user);
						break;
					}
					else {
						// Affiche le message du client \\
						String msgClient = msgFromClient.nextLine();
						System.out.println(username + ": " + msgClient);
					}
				} catch (Exception eMsgClient) {
					System.out.println("[Système] Erreur lors de la réception du message du client " + clientAdr + " :\n");
					eMsgClient.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}