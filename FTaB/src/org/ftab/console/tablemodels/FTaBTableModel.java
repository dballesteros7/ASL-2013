/**
 * FTaBTableModel.java 
 * Created: Oct 11, 2013
 * @author Jean-Pierre Smith
 */
package org.ftab.console.tablemodels;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.table.DefaultTableModel;

import org.ftab.console.ui.MgmtConsole;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * Abstract table model for quickly loading display-only tables in 
 * the Management Console.
 */
public abstract class FTaBTableModel extends DefaultTableModel {
	/**
	 * The class types of the data of the columns present in the
	 * table.
	 */
	private final Class[] columnTypes;
	
	/**
	 * Creates a new FTaBTableModel with the specified columns containing
	 * the specified column types.
	 * @param columNames A string array containing the names of each of the columns.
	 * @param columnTypes The type of the data in each column named above.
	 */
	public FTaBTableModel(String[] columNames, Class[] columnTypes) {
		super(columNames, 0);
		
		this.columnTypes = columnTypes;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Refreshes the data present in the table model from the database.
	 * @param arg An optional argument.
	 * @return An optional return value.
	 * @throws SQLException If the refresh fails due to an issue with the 
	 * database connection.
	 */
	public Object Refresh(Object arg) throws SQLException {
		return Refresh(arg, null);
	}
	
	/**
	 * Refreshes the data present in the table model from the database.
	 * @return An optional return value.
	 * @throws SQLException If the refresh fails due to an issue with the 
	 * database connection.
	 */
	public Object Refresh() throws SQLException {
		return Refresh(null, null);
	}

	
	/**
	 * Refreshes the data present in the table model from the database.
	 * @param arg An optional argument.
	 * @param source A specifiable data source.
	 * @return An optional return value.
	 * @throws SQLException If the refresh fails due to an issue with the 
	 * database connection.
	 */
	public Object Refresh(Object arg, PGPoolingDataSource source) throws SQLException {
		// Clear the table
		this.setRowCount(0);

		Connection conn = null;
		try {
			if (source != null)
			{
				conn = source.getConnection();
			}
			else
			{
				conn = MgmtConsole.GetSourceConnection();
			}

			return LoadData(arg, conn);

		} finally {
			if (conn != null)
				conn.close();
		}
	}
	
	/**
	 * Uses the supplied connection to load the required data into the table model.
	 * Implement this to handle differing data types and objects returned from the
	 * data source. This method is called by Refresh which handles the connection. 
	 * @param arg The argument that was passed to the Refresh method.
	 * @param conn The connection created by the Refresh method.
	 * @return An optional return value.
	 * @throws SQLException If an error occurs while using the connection.
	 */
	protected abstract Object LoadData(Object arg, Connection conn) throws SQLException;
}
