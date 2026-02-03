package explorer;

import ocean.Course;
import ocean.Rudder;
import ocean.Vec2D;
import org.json.JSONObject;

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

		if (connectOS(hostNameOS, portOS)) {
			System.out.println("Connected to OceanServer");
			launch(name, typ, sector, direction);
		} else {
			System.out.println("Failed to connect to OceanServer");
		}
	}

	class OceanListener extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					String line = in.readLine();
					JSONObject receivedJO = new JSONObject(line);
					handleMessage(receivedJO);
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
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void launch(String name, String typ, Vec2D sector, Vec2D direction) {

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

	public void handleMessage(JSONObject jsonObject) {
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