package explorer.view;

import explorer.ShipApp;
import ocean.Course;
import ocean.Rudder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonFactory extends Button {
	public ArrayList<Button> doMakeMeButtons(ShipApp shipApp) {
		ArrayList<Button> buttons = new ArrayList<>();

		Button bNW = new Button("NW");
		bNW.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shipApp.navigate(Rudder.Left, Course.Forward);
			}
		});
		buttons.add(bNW);

		Button bN = new Button("N");
		bN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Center, Course.Forward);
			}
		});
		buttons.add(bN);

		Button bNE = new Button("NE");
		bN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Center, Course.Forward);
			}
		});
		buttons.add(bNE);

		Button bSub = new Button("drop submarine");
		bSub.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.deploySubmarine();
			}
		});
		buttons.add(bSub);

		Button bExit = new Button("exit");
		bExit.setForeground(Color.green);
		bExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.exit();
			}
		});

		Button bMis = new Button("LAUNCH MISSILE");
		bMis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {}
		});

		return buttons;

	}
}
