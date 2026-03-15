package explorer.view;

import explorer.ShipApp;

import java.awt.*;
import java.util.ArrayList;

// Please note: code regarding the torpedo-feature is AI generated
public class ShipGui {
	private ShipApp shipApp;
	private Frame win;
	private TextArea textArea;

	public ShipGui(ShipApp shipApp) {
		this.shipApp = shipApp;
		// Create window
		win = new Frame();
		win.setLayout(new BorderLayout());

		// Panel for TextArea
		Panel topPanel = new Panel(new BorderLayout());
		textArea = new TextArea("", 10, 20, TextArea.SCROLLBARS_NONE);
		textArea.setEditable(false);
		topPanel.add(textArea, BorderLayout.CENTER);

		// Panel for Buttons
		Panel buttonPanel = new Panel(new GridLayout(0, 3));

		ButtonFactory buttonFactory = new ButtonFactory();
		ArrayList<Button> buttons = buttonFactory.doMakeMeButtons(this.shipApp);
		for (Button button : buttons) {
			buttonPanel.add(button);
		}

		win.add(topPanel, BorderLayout.NORTH);
		win.add(buttonPanel, BorderLayout.CENTER);

		win.pack();
		// Make window and buttons visible
		win.setVisible(true);
	}

	public void updateTextArea(String text) {
		this.textArea.append(text + "\n");
	}
	public void updateWinTitle(String text) {
		this.win.setTitle(text);
	}
}
