
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	private static ArrayList<User> users = new ArrayList<User>();
	private static FileWriter writer;
	

	public static void main(String[] args) throws IOException {

		User adam = new User("adam", "pass");
		User runar = new User("runar", "pass");
		users.add(adam);
		users.add(runar);

		final ServerSocket listener = new ServerSocket(9090);
		writer = new FileWriter("mail.txt", true);
		System.out.println("Starting server");
		loadMails();
		connect(listener);
	}

	private static void connect(final ServerSocket listener) throws IOException {

		Socket socket = listener.accept();
		System.out.println("Connection accepted");
		
		Thread thread = new Thread(){
			public void run(){
				try {
					connect(listener);
				} catch (IOException e) {
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

	private static void parseInput (BufferedReader input, Socket socket) throws IOException {

		String word0 = input.readLine();
		switch (word0) {
		case "login": 
			validateLogin(input, socket);
			break;
		case "sendMail": 
			handleMail(input, socket);
			getWriter(socket).println("mailSent\n");
			break;
		}
	}

	private static void handleMail(BufferedReader input, Socket socket) throws IOException {
		String to = input.readLine();
		String from = input.readLine();
		String header = input.readLine();
		String text = input.readLine();
		String date = input.readLine();
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
				accessDenied("user does not exist", socket);
			}
		}
	}

	private static void storeMail(Mail mail) throws IOException {
		writer.write(mail.to + "\n" + mail.from + "\n" + mail.topic + "\n" + mail.text + "\n" + mail.date + "\n");
		writer.close();
	}
	
	private static void loadMails() throws IOException {
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

	private static void validateLogin(BufferedReader input, Socket socket) throws IOException {

		String userName = input.readLine();
		String password = input.readLine();

		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(userName)) {
				if (users.get(i).getPassword().equals(password)) {
					grantAccess(socket, userName);
					break;
				} else {
					accessDenied("wrong password", socket);
					break;
				}
			}
			i++;
			if (i == users.size()) {
				accessDenied("user does not exist", socket);
			}
		}
	}

	private static void accessDenied(String string, Socket socket) {

		switch (string) {
		case "wrong password": 
			getWriter(socket).println("wrongPwd\n");
			break;
		case "user does not exist": 
			getWriter(socket).println("notExist\n");
			break;
		}
	}

	private static void grantAccess(Socket socket, String userName) {
		getWriter(socket).println("success\n" + userName);
		int index = getUserIndex(userName);
		ArrayList<Mail> sent = users.get(index).getSent();
		ArrayList<Mail> inbox = users.get(index).getInbox();
		
		for (Mail i : sent) {
			getWriter(socket).println("sentMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
		}
		
		for (Mail i : inbox) {
			getWriter(socket).println("inboxMail\n" + i.to + "\n" + i.from + "\n" + i.topic + "\n" + i.text + "\n" + i.date + "\n");
		}
		
		getWriter(socket).println("done");
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