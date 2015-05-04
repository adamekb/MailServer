import java.util.ArrayList;


public class User {
	private String userName;
	private String password;
	private ArrayList<Mail> inbox = new ArrayList<Mail>();
	private ArrayList<Mail> sent = new ArrayList<Mail>();
	
	public User (String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public String getUserName () {
		return userName;
	}
	
	public String getPassword () {
		return password;
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
