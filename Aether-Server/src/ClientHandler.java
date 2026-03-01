import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
	private Socket tubeToClient;
	private List<Userprofile> liClients;

	public ClientHandler(Socket tubeToClient, List<Userprofile> liClients) {
		this.tubeToClient = tubeToClient;
		this.liClients = liClients;
	}

	// Méthode de l'interface Runnable \\
	public void run() {
		Userprofile user;
		String username;
		String clientIP = "";
		Time time = Server.time;

		try {
			Scanner msgFromClient = new Scanner(tubeToClient.getInputStream());        // Écoute le client (pseudo/messages)

			while (true) {
				// Informations du client \\
				clientIP = tubeToClient.getLocalAddress().getHostAddress();            // Adresse du client sans le "/" au début

				if (!msgFromClient.hasNextLine()) {
					System.out.println(time.full() + clientIP + " n'a pas réussi à se connecter.");
					return;
				}

				// Ajout du client à la classe UserProfile \\
				username = msgFromClient.nextLine().strip();
				user = new Userprofile();

				if (!clientIP.isBlank()) {
					user.setIP(clientIP);
				} else {
					System.out.println("\n" + time.full() + "Erreur lors de l'assignation de l'IP d'un client.");
					return;
				}

				// Si le pseudo n'existe pas déjà \\
				if (!username.isBlank()) user.setUsername(username);
				// Si le pseudo est vide (bug ou autre) \\
				else {
					System.out.println("\n" + time.full() + "Erreur : le pseudo de " + clientIP + "est vide.");
					System.out.println("Déconnexion du client " + clientIP);

					// Déconnecte le client \\
					tubeToClient.close();
					return;
				}

				boolean isUsernameAllowed = true;

				for (Userprofile client : liClients) {
					// Si le pseudo est déjà pris par un autre utilisateur \\
					if (client.getUsername().equals(username)) {
						user.setTunnel(new PrintWriter(tubeToClient.getOutputStream(), true));
						user.getTunnel().println("/ERROR_USERNAME");
						liClients.remove(user);
						isUsernameAllowed = false;
					}
				}
				if (isUsernameAllowed) {
					break;
				}
			}
																						// getOutputStream sortie serveur : serveur -> client (0/1)
			user.setTunnel(new PrintWriter(tubeToClient.getOutputStream(), true));		// PrintWriter (traducteur) : serveur P -> client (str -> 0/1)
																						// setTunnel (pour SERVEUR) : "ce tuyau appartient à xUser"
			liClients.add(user);

			System.out.println("\r"+ time.full() + username +" ("+ clientIP +") s'est connecté.");

			// Messages du client reçus par le serveur \\
			while (true) {
				try {
					// Si le client se déconnecte \\
					if (!msgFromClient.hasNextLine()) {
						System.out.println(time.full() + username +" s'est déconnecté.");
						liClients.remove(user);
						break;
					}
					else {
						String msgClient = msgFromClient.nextLine().strip();

						if (msgClient.startsWith("/msgAll")) {
							String msg = msgClient.split(" ")[1];

							for (Userprofile connectedUser: liClients) {
								if (!connectedUser.getUsername().equals(user.getUsername())) {
									connectedUser.getTunnel().println(username + ": " + msg);
								}
							}
						}
						else if (msgClient.startsWith("/msg")) {
							String IP = msgClient.split(" ")[1];			// [/msg ip] -> [/msg] [ip] -> ip.

							int cpt = 0;
							for (Userprofile connectedUser: liClients) {
								if (connectedUser.getIP().equals(IP)) {
									String msg = msgClient.split(" ", 3)[2];
									connectedUser.getTunnel().println(username +": "+ msg);
								}
								else {
									cpt += 1;
								}
							}
							// Si aucun client n'est trouvé avec cette IP \\
							if (cpt == liClients.size()) {
								user.getTunnel().println("/SERVER_MESSAGE Aucun client n'est associé à l'IP "+ IP +".");
							}
						}
						else {
							System.out.println(time.full() + username + ": " + msgClient);

							/*
							// Si l'utilisateur a déjà parlé juste avant \\
							if (Server.lastUsername.equals(username)) {
								System.out.println(time.full() + msgClient);
							}
							else {
								// Affiche le message du client dans la console du serveur \\
								System.out.println(time.full() + username + ": " + msgClient);
								Server.lastUsername = username;
							}
							 */
						}
					}
				} catch (Exception eMsgClient) {
					System.out.println(time.full() +"Erreur lors de la réception du message du client "+ clientIP +" :\n");
					eMsgClient.printStackTrace();
				}
			}
		} catch (Exception eSetTunnel) {
			System.out.println(time.full() +"Erreur lors de l'assignation du tunnel de "+ clientIP +" :\n");
			eSetTunnel.printStackTrace();
		}
	}
}