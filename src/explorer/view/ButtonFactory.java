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

		Button bSC = new Button("SCAN");
		bN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.scan();
			}
		});
		buttons.add(bSC);

		Button bExit = new Button("EXIT");
		bExit.setForeground(Color.green);
		bExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.exit();
			}
		});
		buttons.add(bExit);

		Button bRD = new Button("RADAR");
		bN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.radar();
			}
		});
		buttons.add(bRD);

		Button bSW = new Button("SW");
		bSW.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Left, Course.Backward);
			}
		});
		buttons.add(bSW);

		Button bS = new Button("S");
		bS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Center, Course.Backward);
			}
		});
		buttons.add(bS);

		Button bMis = new Button("LAUNCH MISSILE");
		bMis.addActionListener(new ActionListener() {
		Button bSE = new Button("SE");
		bSE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Right, Course.Backward);
			}
		});
		buttons.add(bSE);

			@Override
			public void actionPerformed(ActionEvent actionEvent) {}
		Button bSub = new Button("drop submarine");
		bSub.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.deploySubmarine();
			}
		});
		buttons.add(bSub);

		return buttons;

	}
}
