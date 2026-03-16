package explorer;

import explorer.view.ShipGui;
import ocean.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ShipApp {
	private Socket toOceanServer;
	private String oceanServerHost;
	private int oceanServerPortForSubmarines;
	private int submarineServerPort;
	private String submarineServerHost;
	private String name;
	private String typ;
	private String shipID;
	private int shipDatabaseIdentifier; //primary key from the ship table
	private Vec2D sector;
	private Vec2D direction;
	private JSONObject jsonObject;
	private OceanListener oceanListener;
	private ArrayList<RadarEcho> echos;
	private SubmarineServer submarineServer;
	private Database database = new Database();
	private ShipGui shipGui;

	// Please note: code regarding the torpedo-feature is AI generated
	public ShipApp(String name, String typ) throws SQLException {
		this.name = name;
		this.typ = typ;

		this.oceanServerPortForSubmarines = 8151;
		this.submarineServerPort = 8152;
		this.submarineServerHost = "127.0.0.1";

		jsonObject = new JSONObject();
		jsonObject.put("name", this.name);
		jsonObject.put("typ", this.typ);
	}

	// Send and receive messages from OceanServer
	class OceanListener extends Thread {
		private BufferedReader in;
		private PrintWriter out;

		public OceanListener() {
			try {
				in = new BufferedReader(new InputStreamReader(toOceanServer.getInputStream()));
				out = new PrintWriter(toOceanServer.getOutputStream(), true);
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

				System.out.println("OceanListener thread exiting");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException | SQLException e) {
				// -
			}
		}
	}

	// Establish connection to OceanServer, initializes OceanListener and SubmarineServer
	public boolean connectOS(String hostNameOS, int portOS) {
		try {
			toOceanServer = new Socket(hostNameOS, portOS);
			oceanListener = new OceanListener();
			oceanListener.start();
			this.oceanServerHost = hostNameOS;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Process messages (JSON) from OceanServer
	public void handleMessage(JSONObject jsonObject) throws InterruptedException, SQLException {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "launched":
				this.shipID = jsonObject.get("id").toString();
				this.shipGui.updateTextArea("Launched: " + this.shipID);

				this.insertLaunchedDataInDatabase();
				int sectorID = database.getSectorID(sector);

				submarineServer = new SubmarineServer(shipDatabaseIdentifier, sectorID);
				submarineServer.start();
				shipGui.updateWinTitle(this.shipID);
				break;
			case "message":
				message(jsonObject);
				break;
			case "move2d":
				Vec2D oldValueOfSector = sector;
				move2d(jsonObject);
				if (!oldValueOfSector.equals(sector)) {
					database.insertSector(sector.getX(), sector.getY());
				}
				break;
            case "crash":
                System.out.println(jsonObject);
                System.out.println(shipDatabaseIdentifier);
                database.setShipStatusAsInactive(shipDatabaseIdentifier);

			    //{"sunkPos":{"vec":[4050,3150,1]},"cmd":"crash","id":"#1#Leopard1","message":"CRASH: ship run ashore","sector":{"vec2":[40,31]}}
                break;
			case "scanned":
				this.shipGui.updateTextArea("Sector scanned!");
				insertScanResultsInDatabase(jsonObject);
				break;
			case "radarresponse":
				radarresponse(jsonObject);
				break;
			default:
				System.out.println("Unknown Command: " + cmd);
		}
	}

	// Spawns a ship on the ocean
	public void launch() {
		this.sector = new Vec2D(40, 40);
		this.direction = new Vec2D(0, 1);

		jsonObject.put("cmd", "launch");
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		oceanListener.out.println(jsonObject);
	}

	// Handles "message" command from OceanServer
	public void message(JSONObject jsonObject) {
		String type = jsonObject.get("type").toString();
		String text = jsonObject.get("text").toString();
		this.shipGui.updateTextArea(type + ": " + text);
	}

	// Updates sector and direction fields with new values, and displays them in the Gui
	public void move2d(JSONObject jsonObject) {
		this.sector = Vec2D.fromJson(jsonObject.getJSONObject("sector"));
		this.direction = Vec2D.fromJson(jsonObject.getJSONObject("dir"));
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());

		this.shipGui.updateTextArea("Current position: " + this.sector.toString());
		this.shipGui.updateTextArea("Current direction: " + this.direction.toString());
	}

	// Builds the JSON to be sent to OceanServer for updating ship location
	public void navigate(Rudder rudderDirection, Course courseDirection) {
		JSONObject nav = new JSONObject();
		nav.put("cmd", "navigate");
		nav.put("rudder", rudderDirection.toString());
		nav.put("course", courseDirection.toString());
		oceanListener.out.println(nav);
	}

	// Builds the JSON to be sent to OceanServer for ship scan action
	public void scan() {
		oceanListener.out.println(new JSONObject().put("cmd", "scan"));
	}

	// Builds the JSON to be sent to OceanServer for ship radar action
	public void radar() {
		oceanListener.out.println(new JSONObject().put("cmd", "radar"));
	}

	// Handles radar responses from OceanServer
	public void radarresponse(JSONObject jsonObject) {

        JSONArray response = jsonObject.getJSONArray("echos");
		echos = new ArrayList<>();

		for (int i = 0; i < response.length(); i++) {
			RadarEcho re = RadarEcho.fromJson(response.getJSONObject(i));
			echos.add(re);
		}

        try {
            database.insertShipRadarData(this.shipDatabaseIdentifier, echos);
			this.shipGui.updateTextArea("Sourrounding sectors scanned!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

	}

    private void insertLaunchedDataInDatabase() throws SQLException {
        String shipName = shipID.split("#")[2];
        this.shipDatabaseIdentifier = database.insertShipData(shipID, shipName);
    }

    private void insertScanResultsInDatabase(JSONObject jsonObject) throws SQLException {
        int totalDepthAverage = jsonObject.getInt("depth");
        float standardDeviation = jsonObject.getFloat("stddev");

        database.insertShipScanData(totalDepthAverage, standardDeviation, sector, this.shipDatabaseIdentifier);

    }

	// The gui's exit button interrupts SubmarineServer, sends exit to OceanServer and interrupts OceanListener
	// before ending the current main thread
	public void exit() {
		submarineServer.interrupt();
		oceanListener.out.println(new JSONObject().put("cmd", "exit"));
		oceanListener.interrupt();
		database.close();
		System.exit(0);
	}

	// Spawns a new submarine, which communicates via SubmarineServer
	public void deploySubmarine() {
		AppLauncher.startSubmarine("src/", shipID, submarineServerHost, submarineServerPort, oceanServerHost, oceanServerPortForSubmarines);
	}

	// Spawns a new submarine which acts like a torpedo by propelling mindlessly ahead
	public void launchTorpedo() {
		submarineServer.setNextIsTorpedo(true);
		AppLauncher.startSubmarine("src/", shipID, submarineServerHost, submarineServerPort, oceanServerHost, oceanServerPortForSubmarines);
	}

	public void setShipGui(ShipGui shipGui) {
		this.shipGui = shipGui;
	}
}