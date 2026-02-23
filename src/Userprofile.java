public class Userprofile {
	private String IP;
	private String username;

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
}
