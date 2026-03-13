package explorer;

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
import java.util.ArrayList;
import java.util.Arrays;

public class ShipApp {
	private Socket toOceanServer;
	private String oceanServerHost;
	private int oceanServerPortForSubmarines;
	private int submarineServerPort;
	private String submarineServerHost;
	private String name;
	private String typ;
	private String shipID;
	private Vec2D sector;
	private Vec2D direction;
	private JSONObject jsonObject;
	private OceanListener oceanListener;
	private ArrayList<RadarEcho> echos;
	private SubmarineServer submarineServer;

	/*
		Ship Directons
		nw = -1, 1     n = 0, 1     ne = 1, 1
		w = -1, 0					e = 1, 0
		sw = -1, -1	   s = 0, -1	se = 1, -1
	 */

	public ShipApp(String name, String typ) {
		this.name = name;
		this.typ = typ;

		this.oceanServerPortForSubmarines = 8151;
		this.submarineServerPort = 8152;
		this.submarineServerHost = "127.0.0.1";

		jsonObject = new JSONObject();
		jsonObject.put("name", this.name);
		jsonObject.put("typ", this.typ);


	}

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
			} catch (InterruptedException e) {
				// -
			}
		}
	}


	// TODO: mariadb JDBC guide: https://mariadb.com/docs/connectors/connectors-quickstart-guides/mariadb-connector-j-guide

	public boolean connectOS(String hostNameOS, int portOS) {
		try {
			toOceanServer = new Socket(hostNameOS, portOS);
			oceanListener = new OceanListener();
			oceanListener.start();
			this.oceanServerHost = hostNameOS;
			submarineServer = new SubmarineServer();
			submarineServer.start();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized void handleMessage(JSONObject jsonObject) throws InterruptedException {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "launch":
				oceanListener.out.println(jsonObject);
				break;
			case "launched":
				this.shipID = jsonObject.get("id").toString();
				System.out.printf("Launched: %s, ", this.shipID);
				break;
			case "message":
				message(jsonObject);
				break;
			case "move2d":
				move2d(jsonObject);
				break;
//			case "crash":

			//{"depth":-34,"cmd":"scanned","id":"#0#The Ship","stddev":12.487269}
			case "scanned":
				System.out.println("Scan Result: " + jsonObject.toString());
				break;
			case "radarresponse":
				radarresponse(jsonObject);
				break;
			default:
				System.out.println("Unknown Command: " + cmd);
		}
		notifyAll();
	}

	public void launch() throws InterruptedException {
		this.sector = new Vec2D(1, 1);
		this.direction = new Vec2D(0, 1);

		jsonObject.put("cmd", "launch");
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		handleMessage(jsonObject);
	}

	public void message(JSONObject jsonObject) {
		System.out.println("Message: " + jsonObject.toString());
	}

	public void move2d(JSONObject jsonObject) {
		this.sector = Vec2D.fromJson(jsonObject.getJSONObject("sector"));
		this.direction = Vec2D.fromJson(jsonObject.getJSONObject("dir"));
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		System.out.printf("Current position: %s, ", this.sector.toString());
		System.out.printf("Current direction: %s\n", this.direction.toString());
	}

	// Rudder rudder, Course course
	public void navigate(Rudder rudderDirection, Course courseDirection) {
		JSONObject nav = new JSONObject();
		nav.put("cmd", "navigate");

		nav.put("rudder", rudderDirection.toString());

		nav.put("course", courseDirection.toString());

		oceanListener.out.println(nav);
	}

	public void scan() {
		oceanListener.out.println(new JSONObject().put("cmd", "scan"));
	}

	public void radar() {
		oceanListener.out.println(new JSONObject().put("cmd", "radar"));
	}

	public void radarresponse(JSONObject jsonObject) {
		System.out.println("Response: " + jsonObject.toString());
		JSONArray response = new JSONArray(jsonObject.getJSONArray("echos"));
		echos = new ArrayList<>();

		for (int i = 0; i < response.length(); i++) {
			RadarEcho re = RadarEcho.fromJson(response.getJSONObject(i));
			echos.add(re);
		}

		System.out.println(" ");
		System.out.println(Arrays.toString(echos.toArray()));
	}

	public void exit() {
		oceanListener.out.println(new JSONObject().put("cmd", "exit"));
		submarineServer.interrupt();
		oceanListener.interrupt();
		Thread.currentThread().interrupt();
	}

	public String getShipId() {
		return this.shipID;
	}

	public void deploySubmarine() {
		AppLauncher.startSubmarine("src/", shipID, submarineServerHost, submarineServerPort, oceanServerHost, oceanServerPortForSubmarines);
	}

	public void launchTorpedo() {
		submarineServer.setNextIsTorpedo(true);
		AppLauncher.startSubmarine("src/", shipID, submarineServerHost, submarineServerPort, oceanServerHost, oceanServerPortForSubmarines);
	}

}