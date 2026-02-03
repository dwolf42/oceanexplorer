import explorer.ShipApp;
import ocean.Vec2D;

public class Main {
	public static void main(String[] args) {
		ShipApp shipApp = new ShipApp("127.0.0.1",
			8150,
			"The Ship",
			"ship",
			new Vec2D(0, 0),
			new Vec2D(1, 1));
	}
}