package explorer.view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonFactory extends Button {
	public ArrayList<Button> doMakeMeButtons() {

		Button left = new Button("left");
		left.setSize(20, 20);



		Button center = new Button("center");
		Button right = new Button("right");
		Button submarine = new Button("drop submarine");



		ArrayList<Button> buttons = new ArrayList<>();
		buttons.add(new Checkbox("one", true))
	}
}
