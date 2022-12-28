package ninjabrainbot.gui.components;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ninjabrainbot.Main;
import ninjabrainbot.calculator.Calibrator;
import ninjabrainbot.calculator.IThrow;
import ninjabrainbot.calculator.Throw;
import ninjabrainbot.gui.GUI;
import ninjabrainbot.gui.Histogram;
import ninjabrainbot.gui.OptionsFrame;
import ninjabrainbot.gui.SizePreference;
import ninjabrainbot.gui.Theme;
import ninjabrainbot.util.I18n;

public class CalibrationPanel extends JPanel implements ThemedComponent {

	private static final long serialVersionUID = 1739847622825900761L;
	
	Calibrator calibrator;
	
	GUI gui;
	OptionsFrame optionsFrame;
	TitleBarPanel titlebarPanel;
	JLabel titletextLabel;
	FlatButton cancelButton;
	JPanel panel;
	InstructionLabel[] labels;
	ErrorTextArea errors;
	Histogram hist;
	JPanel rightPanel;
	JLabel std;
	static final int errorAreaWidth = 100;
	
	public CalibrationPanel(GUI gui, OptionsFrame frame) {
		this.gui = gui;
		gui.registerThemedComponent(this);
		optionsFrame = frame;
		calibrator = new Calibrator();
		setOpaque(false);
		setLayout(null);
		setVisible(false);
		
		titlebarPanel = new TitleBarPanel(gui, frame);
		titlebarPanel.setLayout(null);
		add(titlebarPanel);
		titletextLabel = new ThemedLabel(gui, I18n.get("calibrator.title_text_label"), true) {
			private static final long serialVersionUID = -1284032833229918460L;
			@Override
			public int getTextSize(SizePreference p) {
				return p.TEXT_SIZE_TITLE_LARGE;
			}
		};
		titlebarPanel.add(titletextLabel);
		cancelButton = getCancelButton();
		titlebarPanel.add(cancelButton);
		
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);
		labels = new InstructionLabel[] {
				new InstructionLabel(gui, I18n.get("calibrator.command_label")),
				new InstructionLabel(gui, I18n.get("calibrator.throw_label")),
				new InstructionLabel(gui, I18n.get("calibrator.measure_label")),
		};
		ThemedLabel explanation = new ThemedLabel(gui, "<html><div style='text-align: center;'>"+I18n.get("calibrator.explanation")+"</div></html>") {
			private static final long serialVersionUID = -5378176835369680709L;
			@Override
			public int getTextSize(SizePreference p) {
				return p.TEXT_SIZE_SMALL;
			}
		};
		explanation.setAlignmentX(0.5f);
		panel.add(explanation);
		panel.add(Box.createVerticalStrut(5));
		panel.add(new Divider(gui));
		panel.add(Box.createVerticalStrut(5));
		for (InstructionLabel l : labels) {
			panel.add(l);
			panel.add(Box.createVerticalStrut(5));
		}
		JPanel panel2 = new JPanel();
		panel2.setOpaque(false);
		panel2.setLayout(new BorderLayout());
		panel.add(panel2);
		errors = new ErrorTextArea(gui, new JTextArea());
		errors.setPreferredSize(new Dimension(errorAreaWidth, errorAreaWidth));
		rightPanel = new JPanel();
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new GridLayout(3, 1, 0, 0));
		std = new ThemedLabel(gui);
		std.setOpaque(false);
		std.setPreferredSize(new Dimension(errorAreaWidth, errorAreaWidth));
		JLabel l1 = new ThemedLabel(gui, I18n.get("calibrator.l1"));
		JButton done = new FlatButton(gui, I18n.get("calibrator.done"));
		done.addActionListener(p -> done());
		l1.setHorizontalAlignment(SwingConstants.CENTER);
		std.setHorizontalAlignment(SwingConstants.CENTER);
		rightPanel.add(l1);
		rightPanel.add(std);
		rightPanel.add(done);
		hist = new Histogram(gui, -0.03f, 0.03f, 11);
		panel2.add(errors, BorderLayout.LINE_START);
		panel2.add(hist, BorderLayout.CENTER);
		panel2.add(rightPanel, BorderLayout.LINE_END);
	}
	
	public void startCalibrating() {
		try {
			calibrator.startCalibrating();
			std.setText("-");
			setHighlighted(0);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public void cancel() {
		if (calibrator.isCalibrating()) {
			calibrator.stop();
			errors.area.setText("");
			hist.clear();
		}
	}
	
	private void done() {
		if (calibrator.isStrongholdDetermined()) {
			float std = (float) calibrator.getSTD();
			Main.preferences.sigma.set(std);
			optionsFrame.stopCalibrating();
		}
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		int titlebarHeight = titlebarPanel.getPreferredSize().height;
		titlebarPanel.setBounds(0, 0, width, titlebarHeight);
		titletextLabel.setBounds((titlebarHeight - gui.size.TEXT_SIZE_TITLE_LARGE)/2, 0, 300, titlebarHeight);
		int cancelButtonWidth = cancelButton.getPreferredSize().width;
		cancelButton.setBounds(width - cancelButtonWidth, 0, cancelButtonWidth, titlebarHeight);
		panel.setBounds(0, titlebarHeight, width, height - titlebarHeight);
	}
	
//	@Override
//	public void setSize(int width, int height) {
//		super.setSize(width, height);
//		titlebarPanel.setBounds(0, 0, width, GUI.TITLE_BAR_HEIGHT);
//		titletextLabel.setBounds((GUI.TITLE_BAR_HEIGHT - gui.size.TEXT_SIZE_LARGE)/2, 0, 300, GUI.TITLE_BAR_HEIGHT);
//		int cancelButtonWidth = cancelButton.getPreferredSize().width;
//		cancelButton.setBounds(width - cancelButtonWidth, 0, cancelButtonWidth, GUI.TITLE_BAR_HEIGHT);
//		panel.setBounds(0, GUI.TITLE_BAR_HEIGHT, width, height - GUI.TITLE_BAR_HEIGHT);
//	}
	
	private FlatButton getCancelButton() {
		FlatButton button = new TitleBarButton(gui, I18n.get("calibrator.cancel")) {
			private static final long serialVersionUID = 4380111129291481489L;
			@Override
			public Color getHoverColor(Theme theme) {
				return theme.COLOR_EXIT_BUTTON_HOVER;
			}
			@Override
			public void updateSize(GUI gui) {
				setFont(gui.fontSize(getTextSize(gui.size), false));
			}
		};
		button.addActionListener(p -> optionsFrame.stopCalibrating());
		return button;
	}
	
	private void setHighlighted(int i) {
		for (int j = 0; j < labels.length; j++) {
			labels[j].setHighlighted(i == j);
			labels[j].updateColors(gui);
		}
	}

	public void add(Throw t) throws InterruptedException {
		calibrator.add(t);
		int stage = 0;
		if (calibrator.isReady()) {
			if (calibrator.getNumThrows() > 0) {
				stage = 2;
			} else {
				stage = 1;
			}
		}
		setHighlighted(stage);
		updateHistogram();
	}
	
	public void changeLastAngle(double delta) {
		if (calibrator.getNumThrows() > 0) {
			calibrator.changeLastAngle(delta);
			updateHistogram();
		}
	}

	public boolean isCalibrating() {
		return calibrator.isCalibrating();
	}
	
	private void updateHistogram() {
		if (calibrator.isStrongholdDetermined()) {
			std.setText(String.format("%.3f", calibrator.getSTD()));
			StringBuilder b = new StringBuilder();
			double[] angleErrors = calibrator.getErrors();
			List<IThrow> eyeThrows = calibrator.getThrows();
			for (int i = 0; i < angleErrors.length; i++) {
				IThrow t = eyeThrows.get(i);
				double e = angleErrors[i];
				if (Math.abs(t.correction()) > 1e-7) {
					b.append(String.format(I18n.get("angle") + (t.correction() < 0 ? ": %.2f %.2f\n" : ": %.2f +%.2f\n"), t.alpha() - t.correction(), t.correction()));
				} else {
					b.append(String.format(I18n.get("angle") + ": %.2f\n", t.alpha()));
				}
				b.append(String.format(I18n.get("error") + ": %.3f\n", e));
			}
			errors.area.setText(b.toString());
			hist.setData(angleErrors);
		} else {
			StringBuilder b = new StringBuilder();
			List<IThrow> eyeThrows = calibrator.getThrows();
			for (IThrow t : eyeThrows) {
				if (Math.abs(t.correction()) > 1e-7) {
					b.append(String.format(t.correction() < 0 ? "Angle: %.2f %.2f\n" : "Angle: %.2f +%.2f\n", t.alpha() - t.correction(), t.correction()));
				} else {
					b.append(String.format("Angle: %.2f\n", t.alpha()));
				}
			}
			errors.area.setText(b.toString());
		}
	}
	
	@Override
	public void updateColors(GUI gui) {
	}
	
	@Override
	public void updateSize(GUI gui) {
		panel.setBorder(new EmptyBorder(gui.size.PADDING, 2 * gui.size.PADDING, 2 * gui.size.PADDING, 2 * gui.size.PADDING));
	}
	
}

class InstructionLabel extends ThemedLabel {

	private static final long serialVersionUID = 6451818182019649869L;

	private boolean highlighted;
	
	public InstructionLabel(GUI gui, String text) {
		super(gui, "<html><div style='text-align: center;'>" + text + "</div></html>");
		setHorizontalAlignment(SwingConstants.CENTER);
		setAlignmentX(0.5f);
	}
	
	public void setHighlighted(boolean b) {
		highlighted = b;
	}
	
	@Override
	public Color getForegroundColor(Theme theme) {
		return highlighted ? theme.TEXT_COLOR_STRONG : theme.TEXT_COLOR_WEAK;
	}
	
}

class ErrorTextArea extends JScrollPane implements ThemedComponent {

	private static final long serialVersionUID = 1059206057989769872L;
	
	final JTextArea area;
	
	public ErrorTextArea(GUI gui, JTextArea textArea) {
		super(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		area = textArea;
		area.setEditable(false);
		area.setOpaque(false);
		getViewport().setOpaque(false);
		area.setBorder(null);
		setBorder(BorderFactory.createLineBorder(getBackgroundColor(gui.theme), 1));
		setOpaque(false);
		gui.registerThemedComponent(this);
	}
	
	@Override
	public void updateSize(GUI gui) {
		area.setFont(gui.fontSize(getTextSize(gui.size), true));
	}

	@Override
	public void updateColors(GUI gui) {
		area.setForeground(getForegroundColor(gui.theme));
		setBorder(BorderFactory.createLineBorder(getBackgroundColor(gui.theme), 1));
	}
	
	public Color getForegroundColor(Theme theme) {
		return theme.TEXT_COLOR_NEUTRAL;
	}
	
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_STRONGER;
	}

	public int getTextSize(SizePreference p) {
		return p.TEXT_SIZE_TINY;
	}
	
}

