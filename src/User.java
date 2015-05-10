import java.security.Key;
import java.util.ArrayList;


public class User {
	private String userName;
	private String hash;
	private Key publicKey;
	private ArrayList<Mail> inbox = new ArrayList<Mail>();
	private ArrayList<Mail> sent = new ArrayList<Mail>();
	
	public User (String userName, String hash, Key publicKey) {
		this.publicKey = publicKey;
		this.userName = userName;
		this.hash = hash;
	}
	
	public String getUserName () {
		return userName;
	}
	
	public String getHash () {
		return hash;
	}
	
	public Key getKey () {
		return publicKey;
	}
	
	public void inboxAdd (Mail mail) {
		inbox.add(mail);
	}
	
	public void sentAdd (Mail mail) {
		sent.add(mail);
	}
	
	public ArrayList<Mail> getInbox () {
		return inbox;
	}
	
	public ArrayList<Mail> getSent () {
		return sent;
	}
}
