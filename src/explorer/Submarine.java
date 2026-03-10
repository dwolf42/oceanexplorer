package explorer;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Submarine extends Thread {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;

	public Submarine(Socket connection) {
		this.connection = connection;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new PrintWriter(connection.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String line;

			while (!isInterrupted() && (line = in.readLine()) != null) {
				handleMessage(new JSONObject(new JSONTokener(line)));
			}

			System.out.println("Submarine thread exiting");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// -
		} finally {
			exit();
		}
	}

	public void handleMessage(JSONObject jsonObject) throws InterruptedException {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "ready":
				System.out.println("Ready Message: " + jsonObject);
				break;
			case "message":
				System.out.println("Message Message: " + jsonObject);
				break;
			case "measure":
				System.out.println("Measure Message: " + jsonObject);
				break;
			case "crash":
				System.out.println("Crash Message: " + jsonObject);
				break;
			case "arise":
			System.out.println("Arise Message: " + jsonObject);
			exit();
				break;
			default:
				System.out.println("Unknown Command: " + cmd);
		}
	}

	public void exit() {
		try {
			out.close();
			in.close();
			connection.close();
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
