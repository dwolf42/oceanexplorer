package explorer;

import ocean.Course;
import ocean.Rudder;
import ocean.Vec2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShipApp {
	private Socket toOceanServer;
	private BufferedReader in;
	private PrintWriter out;
	private String hostNameOS;
	private int portOS;
	private String name;
	private String typ;
	private Vec2D sector;
	private Vec2D direction;
	private JSONObject jsonObject;
	private OceanListener oceanListener;

	public ShipApp(String hostNameOS, int portOS, String name, String typ, Vec2D sector, Vec2D direction) {
		this.hostNameOS = hostNameOS;
		this.name = name;
		this.typ = typ;
		this.sector = sector;
		this.direction = direction;

		// establish connection to OceanServer
		if (connectOS(hostNameOS, portOS)) {
			System.out.println("Connected to OceanServer");
		} else {
			System.out.println("Failed to connect to OceanServer");
		}
	}

	class OceanListener extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					in.readLine(); // filler
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("OceanListener thread exiting");
			}
		}
	}

	// TODO: mariadb JDBC guide: https://mariadb.com/docs/connectors/connectors-quickstart-guides/mariadb-connector-j-guide

	public boolean connectOS(String hostNameOS, int portOS) {
		// Connect to OceanServer
		// Initialize reader/writer objects
		// Return true if connection is successful
		try {
			toOceanServer = new Socket(hostNameOS, portOS);
			in = new BufferedReader(new InputStreamReader(toOceanServer.getInputStream()));
			out = new PrintWriter(toOceanServer.getOutputStream(), true);
			launch(name, typ, sector, direction);
			String msg = handleMessage(in.readLine());

			if (msg.equals("launched")) {
				oceanListener = new OceanListener();
				oceanListener.start();
				return true;
			}
			System.out.println("MSG from Server: " + msg);
			toOceanServer.close();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public void launch(String name, String typ, Vec2D sector, Vec2D direction) {
		jsonObject = new JSONObject();
		jsonObject.put("cmd", "launch");
		jsonObject.put("name", name);
		jsonObject.put("typ", typ);
		jsonObject.put("sector", sector.toJson());
		jsonObject.put("dir", direction.toJson());
		out.println(jsonObject);
			/*
{ “cmd“:“launch“, “name“:“schiffname“, “typ“:“ship“,
“sector“:{ “vec2“:[x,y] }, “dir“:{ “vec2“:[dx,dy] }

			 */
	}

	public void navigate(Rudder rudder, Course course) {
		// do something ~
	}

	public void scan() {
		// do something ~
	}

	public void radar() {
		// do something ~
	}

	public String handleMessage(String msgFromServer) {
		JSONObject oceanJson = new JSONObject(new JSONTokener(msgFromServer));
		if (oceanJson.get("cmd").equals("launched")) {
			return "launched";
		} else {
			return oceanJson.get("type") + ": " + oceanJson.get("text");
		}
	}

	public void updateSector(Vec2D sector) {
		// do something ~
	}

	public void updatePosition(Vec2D position) {
		// do something ~
	}

	public void exit() {
		// do something ~
	}
}