import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;


public class User {
	private String userName;
	private String hash;
	private Key publicKey;
	private ArrayList<Mail> inbox = new ArrayList<Mail>();
	private ArrayList<Mail> sent = new ArrayList<Mail>();
	private ArrayList<String> aesKeys = new ArrayList<String>();
	private FileWriter keyWriter;
	
	public User (String userName, String hash, Key publicKey) throws IOException {
		keyWriter = new FileWriter(userName + "Aes.txt", true);
		this.publicKey = publicKey;
		this.userName = userName;
		this.hash = hash;
	}
	
	public void addNewAes (String name, String aesKey) {
		try {
			keyWriter = new FileWriter(userName + "Aes.txt", true);
			keyWriter.write(name + "\n" + aesKey + "\n");
			aesKeys.add(name);
			aesKeys.add(aesKey);
			keyWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initAesKeys () throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(userName + "Aes.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			aesKeys.add(line);
		}
		reader.close();
	}
	
	public ArrayList<String> getAesList () {
		return aesKeys;
	}
	
	public String getUserName () {
		return userName;
	}
	
	public String getHash () {
		return hash;
	}
	
	public Key getPublicKey () {
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
