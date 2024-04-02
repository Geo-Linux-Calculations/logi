package com.derletztekick.geodesy.logi.table.celleditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freixas.jcalendar.JCalendar;

import com.derletztekick.geodesy.logi.Logi;

public class DateTimePickerDialog extends JDialog implements PropertyChangeListener {
	private static final long serialVersionUID = -5573200992180131530L;
	private JCalendar calendar = new JCalendar(
			Calendar.getInstance(Locale.getDefault()),
		    Locale.getDefault(),
		    JCalendar.DISPLAY_DATE | JCalendar.DISPLAY_TIME, false, "HH:mm:ss.S");
	private Date currentDate = new Date(System.currentTimeMillis());
	private JOptionPane optionPane;

	public DateTimePickerDialog() {
		super(new JFrame(), true);
		// Pseudo-Frame fuers ICON
		JFrame pseudoParent = (JFrame)super.getParent();
		try {pseudoParent.setIconImage(new ImageIcon(getClass().getResource(Logi.ICON)).getImage());}catch ( Exception e ) {};
		
//		this.setTitle(title);

		//Create the JOptionPane.
		this.optionPane = new JOptionPane(this.calendar,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION,
				null);

		this.setContentPane(this.optionPane);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//		addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent we) {
//				/*
//				 * Instead of directly closing the window,
//				 * we're going to change the JOptionPane's
//				 * value property.
//				 */
//				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
//			}
//		});
		this.optionPane.addPropertyChangeListener(this);
		pack();
	}
	
	public Date getCurrentDate() {
		return this.currentDate;
	}
	
	public void setCurrentDate(Date date) {
		this.currentDate = date;
		this.calendar.setDate(date == null ? new Date(System.currentTimeMillis()) : date);
	}

	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		if (isVisible() && (e.getSource() == this.optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = this.optionPane.getValue();
			int returnType = JOptionPane.CANCEL_OPTION;
			if (value instanceof Integer) 
				returnType = (Integer)value;
			
			if (returnType == JOptionPane.YES_OPTION)
				this.currentDate = calendar.getDate();
			else if (returnType == JOptionPane.NO_OPTION)
				this.currentDate = null;
		
			if (value == JOptionPane.UNINITIALIZED_VALUE)
				return;

			this.optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			this.clearAndHide();
		}
	}

	public void clearAndHide() {
		this.calendar.setDate(null);
		this.setVisible(false);
	}
}
