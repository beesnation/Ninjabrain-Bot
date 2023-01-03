package ninjabrainbot.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.ValuePropertyLoader;
import ninjabrainbot.Main;
import ninjabrainbot.calculator.IThrow;
import ninjabrainbot.calculator.StdSettings;
import ninjabrainbot.gui.GUI;
import ninjabrainbot.gui.SizePreference;
import ninjabrainbot.gui.Theme;

/**
 * JComponent for showing a Throw.
 */
public class ThrowPanel extends ThemedPanel {

	private static final long serialVersionUID = -1522335220282509326L;
	
	private IThrow t;
	private JLabel x;
	private JLabel z;
	private JLabel alpha;
	private JLabel correction;
	private JLabel std;
	private JLabel error;
	private FlatButton removeButton;

	private boolean stdEnabled, errorsEnabled;
	private int correctionSgn;
	private Color colorNeg, colorPos;

	public ThrowPanel(GUI gui) {
		this(gui, null);
	}

	public ThrowPanel(GUI gui, IThrow t) {
		super(gui);
		setOpaque(true);
		errorsEnabled = Main.preferences.showAngleErrors.get();
		stdEnabled = Main.preferences.showSTDs.get();
		x = new JLabel((String) null, 0);
		z = new JLabel((String) null, 0);
		alpha = new JLabel((String) null, 0);
		correction = new JLabel((String) null, 0);
		std = new JLabel((String) null, 0);
		error = new JLabel((String) null, 0);
		removeButton = new FlatButton(gui, "–") {
			static final long serialVersionUID = -7702064148275208581L;
			@Override
			public Color getHoverColor(Theme theme) {
				return theme.COLOR_REMOVE_BUTTON_HOVER;
			}
			@Override
			public Color getBackgroundColor(Theme theme) {
				return theme.COLOR_NEUTRAL;
			}
		};
		add(removeButton);
		add(x);
		add(z);
		add(alpha);
		add(correction);
		add(std);
		add(error);
		setLayout(null);
		setThrow(t);
		removeButton.addActionListener(p -> gui.removeThrow(this.t));
	}


	public void setSTDsEnabled(boolean b) { stdEnabled = b;}
	public void setAngleErrorsEnabled(boolean e) {
		errorsEnabled = e;
	}

	public void setError(double d) {
		error.setText(String.format(Locale.US, "%.3f", d));
	}
	
	public void setError(String s) {
		error.setText(s);
	}

	public void updateSTD(StdSettings stds) {
		std.setText((t == null || t.lookingBelowHorizon()) ? null : String.format(Locale.US, "%.3f", t.getStd(stds)));
	}
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (x != null)
			x.setFont(font);
		if (z != null)
			z.setFont(font);
		if (alpha != null)
			alpha.setFont(font);
		if (correction != null)
			correction.setFont(font);
		if (std != null)
			std.setFont(font);
		if (error != null)
			error.setFont(font);
		if (removeButton != null)
			removeButton.setFont(font);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		int w = width;
		w -= height;
		if (this.removeButton != null)
			this.removeButton.setBounds(w, 0, height, height-1);

		int nColumns = 3;
		if (errorsEnabled) nColumns++;
		if (stdEnabled) nColumns++;
		int d = w/nColumns;

		error.setVisible(errorsEnabled);
		if(errorsEnabled) {
			w -= d;
			if (this.error != null)
				this.error.setBounds(w, 0, d, height);
		}

		std.setVisible(stdEnabled);
		if(stdEnabled) {
			w -= d;
			if (this.std != null)
				this.std.setBounds(w, 0, d, height);
		}

		w -= d;
		if (this.alpha != null) {
			if (correctionSgn == 0) {
				this.alpha.setBounds(w, 0, d, height);
				this.alpha.setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				this.alpha.setBounds(w-d/3, 0, d, height);
				this.alpha.setHorizontalAlignment(SwingConstants.RIGHT);
				this.correction.setBounds(w+2*d/3, 0, d/2, height);
				this.correction.setHorizontalAlignment(SwingConstants.LEFT);
			}
		}

		w -= d;
		if (this.x != null)
			this.x.setBounds(w, 0, d, height);

		w -= d;
		if (this.z != null)
			this.z.setBounds(w, 0, d, height);
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (x != null)
			x.setForeground(fg);
		if (z != null)
			z.setForeground(fg);
		if (alpha != null)
			alpha.setForeground(fg);
		if (correction != null)
			correction.setForeground(correctionSgn > 0 ? colorPos : colorNeg);
		if (std != null)
			std.setForeground(fg);
		if (error != null)
			error.setForeground(fg);
	}
	
	@Override
	public void updateColors(GUI gui) {
		colorNeg = gui.theme.COLOR_NEGATIVE;
		colorPos = gui.theme.COLOR_POSITIVE;
		setBorder(new MatteBorder(0, 0, 1, 0, gui.theme.COLOR_STRONGER));
		super.updateColors(gui);
	}
	
	@Override
	public void updateSize(GUI gui) {
		super.updateSize(gui);
		setPreferredSize(new Dimension(gui.size.WIDTH, gui.size.TEXT_SIZE_SMALL + gui.size.PADDING_THIN * 2));
	}
	
	public void setThrow(IThrow t) {
		if (t == null) {
			x.setText(null);
			z.setText(null);
			alpha.setText(null);
			correction.setText(null);
			removeButton.setVisible(false);
			correctionSgn = 0;
		} else {
			x.setText(String.format(Locale.US, "%.2f", t.x()));
			z.setText(String.format(Locale.US, "%.2f", t.z()));
			removeButton.setVisible(true);
			if (t.lookingBelowHorizon()) {
				alpha.setText("...");
				correction.setText(null);
				correctionSgn = 0;
			} else {
				alpha.setText(String.format(Locale.US, "%.2f", t.alpha_0()));
				correctionSgn = Math.abs(t.correction()) < 1e-7 ? 0 : (t.correction() > 0 ? 1 : -1);
				if (correctionSgn != 0) {
					correction.setText(String.format(Locale.US, t.correction() > 0 ? "+%.2f" : "%.2f", t.correction()));
					correction.setForeground(t.correction() > 0 ? colorPos : colorNeg);
				} else {
					correction.setText(null);
				}
			}
		}
		this.t = t;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (t != null) {
			if (t.manualInput()) {
				int a = 3;
				int b = 2;
				g.setColor(Color.CYAN);
				g.fillRect(b, b, a, a);
			} else if (t.altStd()) {
				int a = 3;
				int b = 2;
				g.setColor(Color.RED);
				g.fillRect(b, b, a, a);
			}
		}
	}
	
	public boolean hasThrow() {
		return t != null;
	}

	@Override
	public int getTextSize(SizePreference p) {
		return p.TEXT_SIZE_SMALL;
	}
	
	@Override
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_NEUTRAL;
	}
	
	@Override
	public Color getForegroundColor(Theme theme) {
		return theme.TEXT_COLOR_NEUTRAL;
	}


}