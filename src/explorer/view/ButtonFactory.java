package explorer.view;

import explorer.ShipApp;
import ocean.Course;
import ocean.Rudder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonFactory {
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
		bNE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Right, Course.Forward);
			}
		});
		buttons.add(bNE);

		Button bSC = new Button("SCAN");
		bSC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.scan();
			}
		});
		buttons.add(bSC);

		Button bExit = new Button("EXIT");
		bExit.setForeground(new Color(37, 150, 190));
		bExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.exit();
			}
		});
		buttons.add(bExit);

		Button bRD = new Button("RADAR");
		bRD.addActionListener(new ActionListener() {
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

		Button bSE = new Button("SE");
		bSE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.navigate(Rudder.Right, Course.Backward);
			}
		});
		buttons.add(bSE);

		Button bSub = new Button("DROP SUBMARINE");
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
