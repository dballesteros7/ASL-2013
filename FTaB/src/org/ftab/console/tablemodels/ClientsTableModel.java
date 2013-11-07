package org.ftab.console.tablemodels;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Client;
import org.ftab.database.client.RetrieveClients;

/**
 * Provides a model for a client table. Override the LoadData
 * method to provide differing functionality for which rows are
 * displayed.
 * 
 * The table structure is:
 * Client ID	Username	Online Status
 * Integer		String		Boolean
 * @author Jean-Pierre Smith
 *
 */
public class ClientsTableModel extends FTaBTableModel {

	/**
	 * Creates a new instance of the ClientsTableModel 
	 */
	public ClientsTableModel() {
		super(new String[] {"Client ID", "Client Username", "Online"}, 
				new Class[] { Integer.class, String.class, Boolean.class });
	}

	@Override
	protected Object LoadData(Object arg, Connection conn)
			throws SQLException {
		int onlineCount = 0;
		
		ArrayList<Client> clients = new RetrieveClients().execute(conn);
		for(Client client : clients) {
			if (client.isClientOnline()) {
				onlineCount++;
			}
			
			this.addRow(new Object[] { client.getClientId(), client.getClientUsername(), 
					client.isClientOnline() } );
		}						
		return new CTReturnObject(onlineCount, clients.size());
	}

	/**
	 * Encapsulates the return data from the refresh method.
	 * @author Jean-Pierre Smith
	 *
	 */
	public static class CTReturnObject {
		private int onlineClients, totalClients;
		
		/**
		 * Creates a new object to encapsulate the number
		 * of clients online and in total
		 * @param online # of clients online
		 * @param total # of clientsin total
		 */
		public CTReturnObject(int online, int total) {
			onlineClients = online;
			totalClients = total;
		}

		/**
		 * Gets the count of online clients
		 * @return The # of online clients
		 */
		public int getOnlineClients() {
			return onlineClients;
		}
		/**
		 * Gets the total number of clients, both online
		 * and offline
		 * @return The total number of clients
		 */
		public int getTotalClients() {
			return totalClients;
		}
	}
}
