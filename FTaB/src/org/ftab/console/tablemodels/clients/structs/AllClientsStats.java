package org.ftab.console.tablemodels.clients.structs;

/**
 * Encapsulates the return data from the refresh method.
 * @author Jean-Pierre Smith
 *
 */
public class AllClientsStats {
	private int onlineClients, totalClients;
	
	/**
	 * Creates a new object to encapsulate the number
	 * of clients online and in total
	 * @param online # of clients online
	 * @param total # of clientsin total
	 */
	public AllClientsStats(int online, int total) {
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