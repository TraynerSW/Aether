import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	public static void main (String[] args) {
		try {
			ServerSocket clientSocket = new ServerSocket(5000);

			Thread attente = new Thread(() -> {
				while(true) {
					for (int i=0; i<4; i++) {
						if (i==0) {
							System.out.print("\rServeur en attente");
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
			while (true) {
				Socket client = clientSocket.accept();		// Attend un new client ; crée un nouveau socket et le renvoie
				attente.interrupt();						// Dès qu'un client est trouvé, on stoppe le processus

				String clientAdr = client.getLocalAddress().getHostAddress(); 	// Récupère l'adresse du client sans le "/"
				int clientPort = client.getLocalPort();							// Récupère le port que le client a renseigné

				System.out.println("\nUn nouveau client s'est connecté : " + clientAdr + ":" + clientPort);
				OutputStream toClient = client.getOutputStream();

				// Envoi de message à tous les clients connectés
				PrintWriter msgToAllClients = new PrintWriter(toClient, true);		// True permet d'envoyer le msg direct au client connecté
				msgToAllClients.println("Bienvenue " + client.getLocalAddress().getHostAddress() + ":" + client.getLocalPort() + " !");

				Scanner msgServ = new Scanner(System.in);

				Thread sendMsgToClient = new Thread(() -> {
					while (true) {
						try {
							if (System.in.available() > 0) {
								String msg = msgServ.nextLine();
								if (msg.equals("/stop")) {
									System.out.println("[Système] : Arrêt total du serveur en cours...");
									System.exit(0);
								} else if (!msg.isBlank()){			// Si que des espaces ou rien d'écrit
									msgToAllClients.println(msg);
								}
							} else {
								Thread.sleep(50);			// Met en pause le thread principal pour ne pas vérifier le clavier trop souvent
							}
						} catch (Exception e) {
							System.out.println("[Système] Erreur du thread sendMsgToClient :\n");
							e.printStackTrace();
						}
					}
				});
				sendMsgToClient.start();

				// Messages du client reçus par le serveur
				Scanner msgFromClient = new Scanner(client.getInputStream()); // Écoute le client

				while (true) {
					try {
						if (!msgFromClient.hasNextLine()) {
							System.out.println("Le client " + clientAdr + " s'est déconnecté.");
							break;
						} else {
							String msgClient = msgFromClient.nextLine();
							System.out.println("[Client "+ clientAdr + "] " + msgClient);
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
}

