package explorer;

import ocean.Course;
import ocean.RadarEcho;
import ocean.Rudder;
import ocean.Vec2D;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ShipApp {
	private Socket toOceanServer;
	private String hostNameOS;
	private int portOS;
	private String name;
	private String typ;
	private Vec2D sector;
	private Vec2D direction;
	private JSONObject jsonObject;
	private OceanListener oceanListener;

	public ShipApp(String name, String typ) {
		this.name = name;
		this.typ = typ;

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
			}
		}
	}

	// TODO: mariadb JDBC guide: https://mariadb.com/docs/connectors/connectors-quickstart-guides/mariadb-connector-j-guide

	public synchronized boolean connectOS(String hostNameOS, int portOS) {
		try {
			toOceanServer = new Socket(hostNameOS, portOS);
			oceanListener = new OceanListener();
			oceanListener.start();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized void handleMessage(JSONObject jsonObject) {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "launch":
				oceanListener.out.println(jsonObject);
				break;
			case "launched":
				System.out.printf("Launched: %s, ", jsonObject.get("id").toString());
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
		notify();
	}

	/*
	Initial orientation values, not relevant for ship.navigation()
		nw = -1, 1     n = 0, 1     ne = 1, 1
		w = -1, 0					e = 1, 0
		sw = -1, -1	   s = 0, -1	se = 1, -1
	 */
	public synchronized void launch() throws InterruptedException {
		this.sector = new Vec2D(0, 0);
		this.direction = new Vec2D(1, 1);

		jsonObject.put("cmd", "launch");
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		handleMessage(jsonObject);
		wait();
	}

	public synchronized void message(JSONObject jsonObject) {
		System.out.println("Message: " + jsonObject.toString());
		notify();
	}

	public synchronized void move2d(JSONObject jsonObject) {
		this.sector = Vec2D.fromJson(jsonObject.getJSONObject("sector"));
		this.direction = Vec2D.fromJson(jsonObject.getJSONObject("dir"));
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		System.out.printf("Current position: %s, ", this.sector.toString());
		System.out.printf("Current direction: %s\n", this.direction.toString());
		notify();
	}

	// Rudder rudder, Course course
	public synchronized void navigate() throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		int selection;

		JSONObject nav = new JSONObject();
		nav.put("cmd", "navigate");

		System.out.printf("Current position: %s, ", this.sector.toString());
		System.out.printf("Current direction: %s\n", this.direction.toString());
		System.out.println("Set rudder (Left = 0 | Center = 1 | Right = 2):");
		// To adjust the rudder without string operations, an array is created from the available enums.
		// The corresponding rudder-alignment is then used based on the index.
		Rudder[] rudders = Rudder.values();
		selection = scanner.nextInt();
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
		wait();
	}

	public synchronized void scan() throws InterruptedException {
		oceanListener.out.println(new JSONObject().put("cmd", "scan"));
		wait();
	}

	public synchronized void radar() throws InterruptedException {
		oceanListener.out.println(new JSONObject().put("cmd", "radar"));
		wait();
	}

	/*

	Current position: (vec2: 3,3), Current direction: (1,1)

	Response: {"echos":
	[ {"ground":"Water","sector":{"vec2":[2,3]},"height":0},
	  {"ground":"Water","sector":{"vec2":[2,4]},"height":0},
	  {"ground":"Water","sector":{"vec2":[3,4]},"height":0},
	  {"ground":"Water","sector":{"vec2":[4,4]},"height":0},
	  {"ground":"Water","sector":{"vec2":[4,3]},"height":0},
	  {"ground":"Water","sector":{"vec2":[4,2]},"height":0},
	  {"ground":"Water","sector":{"vec2":[3,2]},"height":0},
	  {"ground":"Water","sector":{"vec2":[2,2]},"height":0}],
	  "cmd":"radarresponse","id":"#0#The Ship"}
	 */

// TODO: navigation must always trigger radar scan/response
// TODO: navigation destination must be checked against impassible by radresponse?
//  	 -> What should happen if ship moves to such an area?

	public synchronized void radarresponse(JSONObject jsonObject) {
		System.out.println("Response: " + jsonObject.toString());
		RadarEcho re = RadarEcho.fromJson(jsonObject);

		notify();
	}

	public synchronized void updateSector(Vec2D sector) {
		// do something ~
	}

	public synchronized void updatePosition(Vec2D position) {
		// do something ~
	}

	public void exit() {
		oceanListener.out.println(new JSONObject().put("cmd", "exit"));
		Thread.currentThread().interrupt();
	}
}