import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Mail {
	String to, from, topic, text, date;
	
	public Mail (String to, String from, String topic, String text, String date) {
		this.date = date;
		this.to = to;
		this.from = from;
		this.topic = topic;
		this.text = text;
	}
}
