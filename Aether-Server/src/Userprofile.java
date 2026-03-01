import java.io.PrintWriter;

public class Userprofile {
	private String IP;
	private String username;
	private PrintWriter tunnel;

	public void setIP(String IP) {
		if (!IP.isBlank()) {
			this.IP = IP;
		} else {
			System.out.println("[UserProfile] L'IP est vide.");
		}
	}

	public void setUsername(String username) {
		if (!username.isBlank()) {
			this.username = username;
		} else {
			System.out.println("[UserProfile] Le pseudo est vide.");
		}
	}

	public void setTunnel(PrintWriter tunnel) {
		this.tunnel = tunnel;
	}

	public String getIP() {
		if (!this.IP.isBlank()) {
			return IP;
		} else {
			return "IP ERROR";
		}
	}

	public String getUsername() {
		if (!this.username.isBlank()) {
			return username;
		}
		return "USERNAME ERROR";
	}

	public PrintWriter getTunnel() {
		return tunnel;
	}
}
