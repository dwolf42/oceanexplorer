package explorer.view;

import explorer.ShipApp;
import ocean.Course;
import ocean.Rudder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

// Please note: code regarding the torpedo-feature is AI generated
// Creates all buttons used in ShipGui to prevent gui class from cludder
public class ButtonFactory {
// TODO: add a text field which gets updated instead of the console
	public ArrayList<Button> doMakeMeButtons(ShipApp shipApp, ShipGui shipGui) {
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
				shipGui.exit();
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

		Button bTorpedo = new Button("LAUNCH TORPEDO");
		bTorpedo.setForeground(new Color(255, 0 , 0));
		bTorpedo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				shipApp.launchTorpedo();
			}
		});
		buttons.add(bTorpedo);

		return buttons;

	}
}
