
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

	private static void connect(ServerSocket listener) throws IOException {

		Sockets sockets = new Sockets();
		sockets.setClient(listener.accept());
		System.out.println("Connection accepted");
		
		final ServerSocket temp = listener;
		
		Thread thread = new Thread(){
			public void run(){
				try {
					connect(temp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();

		InputStreamReader reader = new InputStreamReader(sockets.client.getInputStream());
		BufferedReader input = new BufferedReader(reader);

		parseInput(input, sockets);

		closeSockets(sockets);
	}

	private static void closeSockets(Sockets sockets) {

		try {
			sockets.client.close();
			sockets.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseInput (BufferedReader input, Sockets sockets) throws IOException {

		String word0 = input.readLine();
		switch (word0) {
		case "login": 
			validateLogin(input, sockets);
			break;
		case "sendMail": 
			handleMail(input, sockets);
			getWriter(sockets).println("mailSent\n");
			break;
		}
	}

	private static void handleMail(BufferedReader input, Sockets sockets) throws IOException {
		String to = input.readLine();
		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(to)) {
				Mail mail = new Mail("FIXA", input.readLine(), input.readLine());
				users.get(i).addMail(mail);
				break;
			}
			i++;
			if (i == users.size()) {
				accessDenied("user does not exist", sockets);
			}
		}
	}

	private static void validateLogin(BufferedReader input, Sockets sockets) throws IOException {

		String userName = input.readLine();
		String password = input.readLine();

		int i = 0;
		while(i < users.size()) {
			if (users.get(i).getUserName().equals(userName)) {
				if (users.get(i).getPassword().equals(password)) {
					grantAccess(sockets);
					break;
				} else {
					accessDenied("wrong password", sockets);
					break;
				}
			}
			i++;
			if (i == users.size()) {
				accessDenied("user does not exist", sockets);
			}
		}
	}

	private static void accessDenied(String string, Sockets sockets) {

		switch (string) {
		case "wrong password": 
			getWriter(sockets).println("wrongPwd\n");
			break;
		case "user does not exist": 
			getWriter(sockets).println("notExist\n");
			break;
		}
	}

	private static void grantAccess(Sockets sockets) {
		getWriter(sockets).println("success\n");
	}



	private static PrintWriter getWriter(Sockets sockets) {

		String clientIp = sockets.client.getInetAddress().toString();
		clientIp = clientIp.substring(1); //removing initial backslash
		System.out.println("Client ip: " + clientIp);
		PrintWriter writer = null;
		try {
			sockets.setServer(new Socket(clientIp, 8080));
			writer = new PrintWriter(sockets.server.getOutputStream(), true);
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