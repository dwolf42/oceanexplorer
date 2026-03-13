package explorer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Submarine extends Thread {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;
    private Database database = new Database();
    private int shipDatabaseIdentifier;
    private String isActive = "Yes";
    private String isSunk = "No";

	public Submarine(Socket connection, int shipDatabaseIdentifier) throws SQLException {
		this.connection = connection;
        this.shipDatabaseIdentifier = shipDatabaseIdentifier;

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
		} catch (SQLException e) {
            e.printStackTrace();
        } finally {
			exit();
		}
	}

	public void handleMessage(JSONObject jsonObject) throws InterruptedException, SQLException {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "ready":
				System.out.println("Ready Message: " + jsonObject);
                database.insertSubmarineData(shipDatabaseIdentifier);

				break;
			case "message":
				System.out.println("Message Message: " + jsonObject);
				break;
			case "measure":
				System.out.println("Measure Message: " + jsonObject);
				break;
            case "crash":
                System.out.println("Crashed");
                System.out.println("Crash Message: " + jsonObject);

                JSONObject sunkPos = (JSONObject) jsonObject.query("/sunkPos");
                JSONArray vecCrash = sunkPos.getJSONArray("vec");

                database.insertSubSunkPosition(vecCrash.getInt(0), vecCrash.getInt(1), vecCrash.getInt(2));
                break;
            case "arise":
                System.out.println("Arised");
                System.out.println("Arise Message: " + jsonObject);

                JSONObject arisePos = (JSONObject) jsonObject.query("/arisePos");
                JSONArray vecArise = arisePos.getJSONArray("vec");

                // We pick only the x and y coordinates because z is almost always 1
                database.insertSubArisePosition(vecArise.getInt(0), vecArise.getInt(1));

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
