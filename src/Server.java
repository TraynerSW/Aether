import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main (String[] args) {
		try {
			ServerSocket newClient = new ServerSocket(5000);

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
				Socket client = newClient.accept();			// Attend un new client ; crée un nouveau socket et le renvoie
				attente.interrupt();						// Dès qu'un client est trouvé, on stoppe le processus

				String clientAdr = client.getLocalAddress().getHostAddress(); 	// Récupère l'adresse du client sans le "/"
				int clientPort = client.getLocalPort();							// Récupère le port que le client a renseigné

				System.out.print("\nUn nouveau client s'est connecté : " + clientAdr + ":" + clientPort);

				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();

				PrintWriter msgServ = new PrintWriter(out, true);		// True permet d'envoyer le msg direct au client connecté
				msgServ.println("Bienvenue " + client.getLocalAddress().getHostAddress() + ":" + client.getLocalPort() + " !");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

