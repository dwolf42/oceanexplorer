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

public class Submarine extends Thread {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;
    private Database database = new Database();
    private int shipDatabaseIdentifier;         // Primary key of the ship in the database
    private String serverSubID;                 // Submarine ID received from the server
    private int subIdentifier = 0;                  // Primary key of the submarine in the database
    private int sectorID;

	public Submarine(Socket connection, int shipDatabaseIdentifier, int sectorID) throws SQLException {
		this.connection = connection;
        this.shipDatabaseIdentifier = shipDatabaseIdentifier;
        this.sectorID = sectorID;

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

                this.serverSubID = jsonObject.get("id").toString();
                if (subIdentifier == 0) {
                    subIdentifier = database.insertSubmarineData(shipDatabaseIdentifier, serverSubID);
                }

                break;
            case "message":
                System.out.println("Message Message: " + jsonObject);
                break;
            case "measure":
                System.out.println("Measure Message: " + jsonObject);
                JSONArray vecs = jsonObject.getJSONArray("vecs");

                int x = 0;
                int y = 0;
                int z = 0;

                for (int i = 0; i < vecs.length(); i++) {
                    JSONArray vec = vecs.getJSONArray(i);
                    x = vec.getInt(0);
                    y = vec.getInt(1);
                    z = vec.getInt(2);
                }

                database.insertSubMeasurements(subIdentifier, sectorID, x, y, z);
                break;
            case "crash":
                System.out.println("Crashed");
                System.out.println("Crash Message: " + jsonObject);

                JSONObject sunkPos = (JSONObject) jsonObject.query("/sunkPos");
                JSONArray vecCrash = sunkPos.getJSONArray("vec");

                database.insertSubSunkPosition(
                        vecCrash.getInt(0),
                        vecCrash.getInt(1),
                        vecCrash.getInt(2),
                        subIdentifier
                );
                break;
            case "arise":
                System.out.println("Arise Message: " + jsonObject);

                JSONObject arisePos = (JSONObject) jsonObject.query("/arisePos");
                JSONArray vecArise = arisePos.getJSONArray("vec");

                // We pick only the x and y coordinates because z is almost always 1
                database.insertSubArisePosition(
                        vecArise.getInt(0),
                        vecArise.getInt(1),
                        subIdentifier
                );

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
