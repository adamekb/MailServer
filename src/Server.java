import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {

	private static ArrayList<User> users = new ArrayList<User>();
	private static FileWriter mailWriter, serverKeyWriter;
	private static Key privateKey, publicKey;

	public static void main(String[] args) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		User adam = new User("adam", "1000:884b127712dea5253b266c854df04dd4013e52ea88345d12:1b192b707049362ac7121af7f7e97d9c7787de57d2d0177a", Rsa.stringToPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2TjV1rF5Yzg/3YwGD4ChQitfN3smAMdqWtxGmrzYKFZhuAQvwXv0Pvh912KAzBAE4FUPYhNaTsrnoZE6kZJJpdseVbO4SxuwzkLOzkHlw94+Q144sGUoreai5q7nW4Jj52kKOsFXkQi/5aN+MfHTE9cwlz+U6q6Iv2NvalPRQZwIDAQAB"));
		User runar = new User("runar", "1000:2efe51f772416e504d2bac519ad0fbc0f9b1392b1fc86c9a:c88b3dab8b50e16c802c32ad56145ab5c7338142a0980168", Rsa.stringToPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKXSAuXzY9CjB6wUPQY8hEB08azgP01+QYjXlO/EA/RB4PEK40mtWIunN0aamR1Y/urrbiQ01fqv+y5PDlz52Ivx2cyRoVIw+F18okmleVrjA7lxcWnPK+lf6pu85gW6Ccde5vgZT9w9FYbESh6kIV+GTXEw8FijyuBIB6PU0t6wIDAQAB"));
		users.add(adam);
		users.add(runar);

		final ServerSocket listener = new ServerSocket(9090);
		mailWriter = new FileWriter("mail.txt", true);
		System.out.println("Starting server...");
		loadAesKeys();
		loadServerKeys();
		loadMails();
		connect(listener);
	}

	private static void connect(final ServerSocket listener) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		Socket socket = listener.accept();

		Thread thread = new Thread(){
			public void run(){
				try {
					connect(listener);
				} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();

		InputStreamReader reader = new InputStreamReader(socket.getInputStream());
		BufferedReader input = new BufferedReader(reader);
		
		parseInput(input, socket);

		socket.close();
	}

	private static void parseInput (BufferedReader input, Socket socket) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		String word0 = input.readLine();
		printRec(word0);
		switch (word0) {
		case "requestServerKey":
			getWriter(socket).println("serverKey\n" + Rsa.keyToString(publicKey));
			printSent("serverKey\n" + Rsa.keyToString(publicKey));
			break;
		case "login": 
			validateLogin(input, socket);
			break;
		case "sendMail": 
			handleMail(input, socket);
			getWriter(socket).println("mailSent\n");
			printSent("mailSent\n");
			break;
		case "addContact":
			addContact(input, socket);
			break;
		case "giveAesKey":
			addAesKey(input, socket);
			break;
		}
	}

	private static void addAesKey(BufferedReader input, Socket socket) throws IOException {
		String contact = input.readLine();
		
		String name = input.readLine();
		String signature = input.readLine();
		String aesKey1 = input.readLine();
		String aesKey2 = input.readLine();
		printRec(contact + "\n" + name + "\n" + signature + "\n" + aesKey1 + "\n" + aesKey2);
		
		if (validateSignature(name, signature)) {
			int i = 0;
			while(i < users.size()) {
				if (users.get(i).getUserName().equals(contact)) {
					users.get(i).addNewAes(name, aesKey1);
					users.get(getUserIndex(name)).addNewAes(contact, aesKey2);
					getWriter(socket).println("aesAdded\n");
					printSent("aesAdded\n");
					break;
				}
				i++;
				if (i == users.size()) {
					getWriter(socket).println("notExist\n");
					printSent("notExist\n");
				}
			}
		}
	}

	private static boolean validateSignature(String name, String signature) {
		String decryptedSignature = Rsa.decrypt(signature, users.get(getUserIndex(name)).getPublicKey());
		return name.equals(decryptedSignature);
	}

	private static void addContact(BufferedReader input, Socket socket) throws IOException {
		String contact = input.readLine();
		printRec(contact);
		
		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(contact)) {
				getWriter(socket).println("publicKey\n" + contact + "\n" + Rsa.encrypt(contact, privateKey) + "\n" + Rsa.keyToString(users.get(i).getPublicKey()));
				printSent("publicKey\n" + users.get(i).getUserName() + "\n" + Rsa.keyToString(users.get(i).getPublicKey()));
				break;
			}
			i++;
			if (i == users.size()) {
				getWriter(socket).println("notExist\n");
				printSent("notExist\n");
			}
		}
	}

	private static void handleMail(BufferedReader input, Socket socket) 
			throws IOException {
		String to = input.readLine();
		String from = input.readLine();
		String header = input.readLine();
		String text = input.readLine();
		String date = input.readLine();
		printRec(to + "\n" + from + "\n" + header + "\n" + text + "\n" + date);
		Mail mail = new Mail(to, from, header, text, date);
		storeMail(mail);
		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(to)) {
				users.get(i).inboxAdd(mail);
				users.get(getUserIndex(from)).sentAdd(mail);
				break;
			}
			i++;
			if (i == users.size()) {
				getWriter(socket).println("notExist\n");
				printSent("notExist\n");
			}
		}
	}

	private static void storeMail(Mail mail) 
			throws IOException {
		mailWriter = new FileWriter("mail.txt", true);
		mailWriter.write(mail.to + "\n" + mail.from + "\n" + mail.topic + "\n" + mail.text + "\n" + mail.date + "\n");
		mailWriter.close();
	}

	private static void loadMails() 
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("mail.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String to = line;
			String from = reader.readLine();
			String header = reader.readLine();
			String text = reader.readLine();
			String date = reader.readLine();
			Mail mail = new Mail(to, from, header, text, date);

			users.get(getUserIndex(to)).inboxAdd(mail);
			users.get(getUserIndex(from)).sentAdd(mail);
		}
		reader.close();
	}
	
	private static void loadAesKeys() throws IOException {
		for (User i : users) {
			i.initAesKeys();
		}
	}

	private static void loadServerKeys() 
			throws IOException {
		serverKeyWriter = new FileWriter("serverKeys.txt", true);
		serverKeyWriter.close();
		BufferedReader reader = new BufferedReader(new FileReader("serverKeys.txt"));
		String line = null;
		if ((line = reader.readLine()) != null) {
			String privKey = line;
			String pubKey = reader.readLine();
			privateKey = Rsa.stringToPrivateKey(privKey);
			publicKey = Rsa.stringToPublicKey(pubKey);
		} else {
			createKeys();
		}
		reader.close();
	}

	private static void createKeys() 
			throws IOException {
		KeyPair newKeys = Rsa.generateNewKeys();
		privateKey = newKeys.getPrivate();
		publicKey = newKeys.getPublic();
		serverKeyWriter = new FileWriter("serverKeys.txt", true);
		serverKeyWriter.write(Rsa.keyToString(privateKey) + "\n" + Rsa.keyToString(publicKey) + "\n");
		serverKeyWriter.close();
	}

	private static void validateLogin(BufferedReader input, Socket socket) 
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		String encryptedUserName = input.readLine();
		String encryptedPassword = input.readLine();
		printRec(encryptedUserName);
		printRec(encryptedPassword);
		String userName = Rsa.decrypt(encryptedUserName, privateKey);
		char[] password = Rsa.decrypt(encryptedPassword, privateKey).toCharArray();

		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(userName)) {
				if (Hash.validatePassword(password, users.get(i).getHash())) {
					grantAccess(socket, userName);
					break;
				} else {
					getWriter(socket).println("loginError\n");
					printSent("loginError\n");
					break;
				}
			}
			i++;
			if (i == users.size()) {
				getWriter(socket).println("loginError\n");
				printSent("loginError\n");
			}
		}
		Arrays.fill(password, '\u0000');
	}

	private static void grantAccess(Socket socket, String userName) {

		getWriter(socket).println("success\n");
		printSent("success\n");
		int index = getUserIndex(userName);
		ArrayList<Mail> sent = users.get(index).getSent();
		ArrayList<Mail> inbox = users.get(index).getInbox();
		ArrayList<String> newAesKeys = users.get(index).getAesList();
		
		for (int i = 0; i < newAesKeys.size(); i = i + 2) {
			String name = newAesKeys.get(i);
			String encryptedAesKey = newAesKeys.get(i+1);
			Key publicKey = users.get(getUserIndex(name)).getPublicKey();
			getWriter(socket).println("newContact\n" + name + " " + encryptedAesKey + " " + Rsa.keyToString(publicKey));
			printSent("newContact\n" + name + " " + encryptedAesKey + " " + Rsa.keyToString(publicKey));
		}
		
		for (Mail i : sent) {
			getWriter(socket).println("sentMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
			printSent("sentMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
		}

		for (Mail i : inbox) {
			getWriter(socket).println("inboxMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
			printSent("inboxMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
		}

		getWriter(socket).println("done");
		printSent("done");
	}

	private static int getUserIndex(String userName) {
		int result = -1;
		int i = 0;
		while (i < users.size()) {
			if (users.get(i).getUserName().equals(userName)) {
				result = i;
				break;
			}
			i++;
		}
		return result;
	}
	
	private static void printSent (String msg) {
		System.out.println("--------------------------------");
		System.out.println("SERVER SENT");
		System.out.println("--------------------------------");
		System.out.println(msg + "\n\n\n");
	}
	
	private static void printRec (String msg) {
		System.out.println("--------------------------------");
		System.out.println("SERVER RECEIVED");
		System.out.println("--------------------------------");
		System.out.println(msg + "\n\n\n");
	}

	private static PrintWriter getWriter(Socket socket) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}
}