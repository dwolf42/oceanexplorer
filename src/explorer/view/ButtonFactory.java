package explorer.view;

import explorer.ShipApp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonFactory extends Button {
	public ArrayList<Button> doMakeMeButtons(ShipApp shipApp) {
		ArrayList<Button> buttons = new ArrayList<>();

		Button left = new Button("left");
		left.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shipApp.navigate(0);
			}
		});
		buttons.add(left);


		Button center = new Button("center");
		center.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(1);
			}
		});
		buttons.add(center);

		Button right = new Button("right");
		center.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(2);
			}
		});
		buttons.add(right);

		Button submarine = new Button("drop submarine");
		submarine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.deploySubmarine();
			}
		});
		buttons.add(submarine);

		return buttons;

	}
}
