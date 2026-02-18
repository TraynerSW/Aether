import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) {
		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois
		boolean isConnected = false;

		while (true) {
			if (isConnected == false);
			try {
				Socket socket = new Socket("localhost", 5000);                // Si cette connexion passe, tu as le handshake.
				isConnected = true;

				Scanner infoServ = new Scanner(socket.getInputStream());
				String servAdr = socket.getInetAddress().getHostAddress();        // Inet = SON adresse, Local = MON adresse
				System.out.println("Tu es bien connecté au serveur " + servAdr + ".\n");

				while (true) {			// Tant qu'il est connecté, attend les messages.
					String msgServ = infoServ.nextLine();
					System.out.println("[Serveur] " + msgServ);

					Scanner usr = new Scanner(System.in);
					String msgClient = usr.nextLine();

				}
			} catch (Exception e) {
				isConnected = false;
			}

			// Si l'utilisateur est déconnecté
			if (!isConnected) {
				if (!boucle) {
						System.out.println("Tu es déconnecté.");
						boucle = true;
					}

				// Threads d'attente de reconnexion
				Thread attente = new Thread(() -> {
					for (int i=0; i<3; i++) {
						for (int j=0; j<4; j++) {
							if (j==0) {
								System.out.print("\rTentative de reconnexion");
							} else {
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

				Thread reconnexion = new Thread(() -> {
					for (int i=6; i>=0; i--) {
						if (i==0) {
							attente.start();
							try {
								attente.join(0);
							} catch (InterruptedException exce) {
								return;
							}
						}
						else if (i<6) {
							System.out.print("\rTentative de reconnexion dans " + i + " sec");
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
							return;
						}
					}
				});

				// Lancement des threads
				reconnexion.start();
				try {
					reconnexion.join(0);				// Attend que le thread se termine (0 = infini)
				} catch (InterruptedException exp) {
					return;
				}
				//e.printStackTrace();

				// QUAND JE MET LA VERIF POUR QUE CA ARRETE L'AFFICHAGE QUAND L'UTILISATEUR SE RECONNECTE ?
			}
		}
	}
}
