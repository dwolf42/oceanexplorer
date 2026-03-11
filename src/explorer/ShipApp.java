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
import java.util.Scanner;

public class ShipApp extends Frame {
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
	private boolean isDirectionForward = true;

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

		Frame frame = new Frame("name");
		frame.setLayout(new GridLayout(1, 1));
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

	public void deploySubmarine() {
		AppLauncher.startSubmarine("src/", shipID, submarineServerHost, submarineServerPort, oceanServerHost, oceanServerPortForSubmarines);
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
			//	case "crash" -> crash();
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
		this.sector = new Vec2D(2, 5);
		this.direction = new Vec2D(-1, 1);

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
	public void navigate(int rudderDirection) {
		Scanner scanner = new Scanner(System.in);

		JSONObject nav = new JSONObject();
		nav.put("cmd", "navigate");

		System.out.printf("Current position: %s, ", this.sector.toString());
		System.out.printf("Current direction: %s\n", this.direction.toString());
		System.out.println("Set rudder Left = 0 | Center = 1 | Right = 2:");
		// To adjust the rudder without string operations, an array is created from the available enums.
		// The corresponding rudder-alignment is then used based on the index.
		Rudder[] rudders = Rudder.values();
		int selection = scanner.nextInt();
		nav.put("rudder", rudders[selection].toString());
		// flushes the remaining newline character from scanner to prevent it from skipping the following scan
		scanner.skip("\n");

		System.out.println("Set course (Forward = 0 | Backward = 1):");
		// Same procedure as for Rudder above
		Course[] courses = Course.values();
		selection = scanner.nextInt();
		nav.put("course", courses[selection]);
		scanner.skip("\n");

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

	public void setIsDirectionForward(boolean isDirectionForward) {
		this.isDirectionForward = isDirectionForward;
	}
}