import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;

/**
  * This examples shows hot to add icon and related menu
  * to system tray. It also shows how to do simple tool
  * tips for Swing components.
  */
public class Tray extends JFrame implements ActionListener {
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Tray();
			}});
	}
	
	JTextField duration;
	javax.swing.Timer timer;
	
	public Tray() {
		super("Tray");
		Box vBox = Box.createVerticalBox();
		Box durBox = Box.createHorizontalBox();
		durBox.add(new JLabel("Alarm in"));
		duration = new JTextField(8);
		duration.setToolTipText("Enter timer duration is seconds");
		durBox.add(duration);
		vBox.add(durBox);
		Box buttonBox = Box.createHorizontalBox();
		JButton start = new JButton("Time alarm");
		// The following line sets tool tip for the button
		start.setToolTipText("Start the timer for alarm");
		final JFrame parent = this;
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (timer != null)
					// Timers can be stopped
					timer.stop();
				try {
					int ms = Integer.parseInt(duration.getText());
					timer = new javax.swing.Timer(ms * 1000, new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							alarm();
						}});
					// Timers repeat by default, disable that
					timer.setRepeats(false);
					timer.start();
					duration.setText("");
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(parent, "Bad number " + duration.getText(), "Bad number", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonBox.add(start);
		JButton cancel = new JButton("Cancel alarm");
		cancel.setToolTipText("Cancel or stop alarm");
		cancel.addActionListener(this);
		buttonBox.add(cancel);
		vBox.add(buttonBox);
		getContentPane().add(vBox);

		// System tray support is not universal but OS dependent, therefore
		// you should always check for its support		
		if (SystemTray.isSupported()) {
			// System tray is there already, get access to related object
			SystemTray tray = SystemTray.getSystemTray();
			try {
				// Tray contains icons, which are TrayIcon objects
				tray.add(new TimerTrayIcon(this));
			} catch (AWTException ex) {
			}
		}
		
		// Close operation is hide on close so the 
		// program keeps on running when window is 
		// close, it only exists when corresponding 
		// selection is made from tray menu.
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public void alarm() {
		// This is a very ugly way to play some audio.
		// Reuse the same timer reference, so the same stopTimer()
		// method works always. This time we want the timer to repeat.
		timer = new javax.swing.Timer(400, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Toolkit.getDefaultToolkit().beep();
			}});
		timer.start();
	}
	
	public void actionPerformed(ActionEvent e) {
		stopTimer();
	}

	public void stopTimer() {	
		if (timer != null)
			timer.stop();
	}
	
}

/**
  * This class provides the tray related stuff. It extends
  * tray icon and builds the menu for the tray and the 
  * related action listener.
  */
class TimerTrayIcon extends TrayIcon implements ActionListener {
	static String EXIT_COMMAND = "exit";
	static String SHOW_COMMAND = "show";
	Tray timerProgram;
	public TimerTrayIcon(Tray t) {
		// Call super constructor with the image and tool tip text.
		super(createIconImage(), "Timer");
		// Store reference to the main program so we can control it
		timerProgram = t;
		// Notice, these are AWT components, not Swing, no "J"s here.
		// AWT components are native OS UI components. API for using
		// PopupMenu etc. is very similar to corresponding Swing 
		// components.
		PopupMenu menu = new PopupMenu();
		MenuItem cancel = new MenuItem("Cancel timer");
		MenuItem show = new MenuItem("Show");
		show.setActionCommand(SHOW_COMMAND);
		MenuItem exit = new MenuItem("Exit");
		exit.setActionCommand(EXIT_COMMAND);
		cancel.addActionListener(timerProgram);
		exit.addActionListener(this);
		show.addActionListener(this);
		menu.add(cancel);
		menu.addSeparator();
		menu.add(show);
		menu.add(exit);
		// Call setPopupMenu in TrayIcon class to add the menu to tray
		setPopupMenu(menu);
	}
	
	/**
	  * This method builds a simple icon Image to be used in tray.
	  */
	static Image createIconImage() {
		BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = r.createGraphics();
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(2, 2, 12, 12));
		g.setColor(Color.BLACK);
		g.draw(new Ellipse2D.Double(2, 2, 14, 14));
		g.draw(new Line2D.Double(8, 8, 10, 4));
		return r;
	}
	
	/**
	  * Actions originating from the tray menu are handled here.
	  */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(EXIT_COMMAND)) {
			// Stop the timer in case it is running so that
			// timer thread doesn't stop the application from
			// existing
			timerProgram.stopTimer();
			// Hide and dispose the main window
			timerProgram.setVisible(false);
			timerProgram.dispose();
			// Remove the application from tray
			SystemTray.getSystemTray().remove(this);
		}
		if (e.getActionCommand().equals(SHOW_COMMAND)) {
			// Display the main window
			timerProgram.setVisible(true);
			// and bring it to the front of other windows
			timerProgram.toFront();
		}
	}
}