package explorer;

import ocean.AppLauncher;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SubmarineServer extends Thread {
	private ServerSocket subSocket;
	List<Submarine>  submarines = new ArrayList<>();

	public void run() {
		try {
			subSocket = new ServerSocket(8152);
			subSocket.setSoTimeout(2000);

			while (!isInterrupted()) {

				try {
					Socket client = subSocket.accept();
					Submarine submarine = new Submarine(client);
					submarine.start();
					submarines.add(submarine);
				} catch (SocketTimeoutException e) {
					//
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// iterates through all submarines and calls interrupt
			submarines.forEach(Submarine::interrupt);
			if (subSocket != null) {
				try {
					subSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
