import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Time {
	public String small() {
		LocalTime time = LocalTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
		return time.format(format);
	}

	public String big() {
		LocalTime time = LocalTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
		return time.format(format);
	}

	public String full() {
		LocalTime time = LocalTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
		return "["+ time.format(format) +"] ";
	}
}