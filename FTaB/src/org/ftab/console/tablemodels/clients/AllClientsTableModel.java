package org.ftab.console.tablemodels.clients;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.console.tablemodels.clients.structs.AllClientsStats;
import org.ftab.database.Client;
import org.ftab.database.client.RetrieveClients;

@SuppressWarnings("serial")
public class AllClientsTableModel extends ClientsTableModel<AllClientsStats, Object> {

	public AllClientsTableModel() {	}

	@Override
	protected AllClientsStats LoadData(Object arg, Connection conn)
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
		return new AllClientsStats(onlineCount, clients.size());
	}
}
