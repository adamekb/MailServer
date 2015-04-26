
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	private static ArrayList<User> users = new ArrayList<User>();

	public static void main(String[] args) throws IOException {

		User adam = new User("adam", "pass");
		User runar = new User("runar", "pass");
		users.add(adam);
		users.add(runar);

		final ServerSocket listener = new ServerSocket(9090);
		System.out.println("Starting server");

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
		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(to)) {
				Mail mail = new Mail("FIXA", "!!!", input.readLine(), input.readLine());
				users.get(i).addMail(mail);
				break;
			}
			i++;
			if (i == users.size()) {
				accessDenied("user does not exist", socket);
			}
		}
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

	public static void closeServer (ServerSocket listener) throws IOException {

		listener.close();
		System.out.println("Server closed");
	}
}