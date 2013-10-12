package org.ftab.console.tablemodels;

import java.awt.Component;
import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer that renders the date in the tables down 
 * to the millisecond.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer {
	SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if( value instanceof Date) {
            value = f.format(value);
        }
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}
