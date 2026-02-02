package explorer;

import ocean.Course;
import ocean.Rudder;
import ocean.Vec2D;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShipApp {
	private Socket toOceanServer;
	private BufferedReader in;
	private PrintWriter out;
	private String name;
	private String typ;
	private Vec2D sector;
	// private Vec2D direction -> required??
	private JSONObject jsonObject;

	public ShipApp(String hostName, int portOS, String name, String type, Vec2D sector, Vec2D direction) {
		// do something ~
	}
// TODO: create class for JSON handling + UML
// TODO: how is the procedure of starting the explorer.ShipApp?
// TODO: rework naming on UML
// TODO: some colons : are wrong on the UML
// TODO: add handleMessage() parameter name to UML
// TODO: what about the Database Class? We need data from mariadb guide: https://mariadb.com/docs/connectors/connectors-quickstart-guides/mariadb-connector-j-guide
	public boolean connectOS(String hostName, int portOS) {
		// do something ~
	}

	public boolean

	public void launch(String name, String typ, Vec2D sector, Vec2D direction) {
		// do something
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