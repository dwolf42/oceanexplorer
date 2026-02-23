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

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(toOceanServer.getInputStream()));
				out = new PrintWriter(toOceanServer.getOutputStream(), true);

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

	public boolean connectOS(String hostNameOS, int portOS) {
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


	public void handleMessage(JSONObject jsonObject) {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
			case "launch" -> oceanListener.out.println(jsonObject);
			case "launched" -> System.out.printf("Launched: %s, ", jsonObject.get("id").toString());
			case "message" -> message(jsonObject);
		//	case "navigate" -> navigate();
			case "move2d" -> move(jsonObject);
		//	case "crash" -> crash();
		//	case "scan" -> scan();
		//	case "scanned" -> scanned();
		//	case "radar" -> radar();
		//	case "radarresponse" -> radarresponse();
		//	case "exit" -> exit();
		}
	}
	public void launch() {
		this.sector = new Vec2D(0, 0);
		this.direction = new Vec2D(1, 1);

		jsonObject.put("cmd", "launch");
		jsonObject.put("sector", this.sector.toJson());
		jsonObject.put("dir", this.direction.toJson());
		handleMessage(jsonObject);
	}

	public void message(JSONObject jsonObject) {
		System.out.println("Message: " + jsonObject.toString());
	}

	public void move(JSONObject jsonObject) {
		// TODO: Should we also update the JSONObject, like sector and direction?
		this.sector = Vec2D.fromJson(jsonObject.getJSONObject("sector"));
		this.direction = Vec2D.fromJson(jsonObject.getJSONObject("dir"));
		System.out.printf("Current position: %s, ", this.sector.toString());
		System.out.printf("Current direction: %s\n", this.direction.toString());
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