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
	}

	class OceanListener extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private String line;

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(toOceanServer.getInputStream()));
				out = new PrintWriter(toOceanServer.getOutputStream(), true);

				while (!isInterrupted()) {
					line = in.readLine();

					if (!line.isEmpty()) {
						String msg = handleMessage(line);
						if (msg.equals("launched")) {
							System.out.println("Ship is launched");

						} else {
							// TODO: What to do, if the message is not launched?
							System.out.println("MSG from Server: " + line);
						}
					}
					System.out.println("OceanListener thread exiting");
				} catch(IOException e){
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

		public void launch(Vec2D sector, Vec2D direction) {
			jsonObject = new JSONObject();
			jsonObject.put("cmd", "launch");
			jsonObject.put("name", this.name);
			jsonObject.put("typ", this.typ);
			jsonObject.put("sector", sector.toJson());
			jsonObject.put("dir", direction.toJson());
			oceanListener.out.println(jsonObject);
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