import java.util.ArrayList;


public class User {
	private String userName;
	private String password;
	private ArrayList<Mail> newMail = new ArrayList<Mail>();
	private ArrayList<Mail> archive = new ArrayList<Mail>();
	
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
	
	public void addMail (Mail mail) {
		newMail.add(mail);
	}
	
	public ArrayList<Mail> getNewMail () {
		archive.addAll(newMail);
		ArrayList<Mail> temp = newMail;
		newMail.clear();
		return temp;
	}
	
	public ArrayList<Mail> getAllMail () {
		archive.addAll(newMail);
		newMail.clear();
		return archive;
	}
}
