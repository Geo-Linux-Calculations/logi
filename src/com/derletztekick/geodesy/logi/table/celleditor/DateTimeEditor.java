package com.derletztekick.geodesy.logi.table.celleditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;


public class DateTimeEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {	
	private static final long serialVersionUID = -2073053438216750434L;
	private Date currentValue = null;
	private JButton button;
	private DateTimePickerDialog dialog;
	private static final String EDIT = "edit";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	public DateTimeEditor() {
		this.button = new JButton();
		this.button.setActionCommand(EDIT);
		this.button.addActionListener(this);
		this.button.setBorderPainted(false);
		this.dialog = new DateTimePickerDialog();
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			this.button.setText(this.currentValue == null ? "" : this.dateFormatter.format(this.currentValue));
//			this.dialog.setCurrentDate(this.currentValue == null ?  new Date(System.currentTimeMillis()) : this.currentValue );
			this.dialog.setCurrentDate(this.currentValue);
			this.dialog.setLocationRelativeTo(this.button);
			this.dialog.setVisible(true);
			this.currentValue = this.dialog.getCurrentDate();
			fireEditingStopped();
		} 
	}

	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return this.currentValue;
	}

	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.currentValue = (Date)value;
		this.button.setText(currentValue == null ? "" : this.dateFormatter.format(currentValue));
		return this.button;
	}
}

