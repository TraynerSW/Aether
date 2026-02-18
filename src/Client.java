import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) {
		boolean boucle = false;					// Pour afficher le "Tu es déconnecté" qu'une seule fois

		Thread attente = null;
		while (true) {
			try {
				Socket socket = new Socket("localhost", 5000);                // Si cette connexion passe, tu as le handshake.
				if (attente != null) {			// Si le thread n'est pas encore lancé
					attente.interrupt();
				}

				Scanner infoServ = new Scanner(socket.getInputStream());
				String servAdr = socket.getInetAddress().getHostAddress();        // Inet = SON adresse, Local = MON adresse
				System.out.println("\nTu es bien connecté au serveur " + servAdr + ".\n_______________________________________________________________________\n");

				while (true) {			// Tant qu'il est connecté, attend les messages.
					String msgServ = infoServ.nextLine();
					System.out.println("[Serveur] " + msgServ);

					Scanner usr = new Scanner(System.in);
					String msgClient = usr.nextLine();

				}
			} catch (Exception e) {
				if (!boucle) {
					System.out.println("Tu es déconnecté.");
					boucle = true;
				}

				// Threads d'attente de reconnexion
				attente = new Thread(() -> {
					for (int i=0; i<3; i++) {
						System.out.print("\rTentative de reconnexion");
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

				for (int i=6; i>=0; i--) {
					if (i==0) {
						attente.start();
						try {
							Thread.sleep(4000);
						} catch (InterruptedException ex) {
							attente.interrupt();
						}
					}
					else if (i<6) {
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
