/**
 * ServerFactory.java
 * Created: Oct 12, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ftab.server.exceptions.ConfigurationErrorException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Factory class to build a ServerManager class based on a configuration file.
 * The configuration file is in XML format, for a detailed example and format
 * read the documentation file: (something about the config file).
 */
public class ServerFactory {

    public static ServerManager buildManager(String configurationFilePath) {
        try {
            File xmlFile = new File(configurationFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Get the configuration options
            int workerThreads = Integer.parseInt(doc
                    .getElementsByTagName("WorkerThreads").item(0)
                    .getTextContent());
            int clientsPerWorker = Integer.parseInt(doc
                    .getElementsByTagName("ClientsPerWorker").item(0)
                    .getTextContent());
            String loggingLevel = doc.getElementsByTagName("LoggingLevel")
                    .item(0).getTextContent();
            String logOutputPath = doc.getElementsByTagName("LogOutput")
                    .item(0).getTextContent();
            int listeningPort = Integer.parseInt(doc
                    .getElementsByTagName("ListeningPort").item(0)
                    .getTextContent());
            String databaseUser = doc.getElementsByTagName("DatabaseUser")
                    .item(0).getTextContent();
            String databasePassword = doc
                    .getElementsByTagName("DatabasePassword").item(0)
                    .getTextContent();
            String databaseServer = doc.getElementsByTagName("DatabaseServer")
                    .item(0).getTextContent();
            String databaseName = doc.getElementsByTagName("DatabaseName")
                    .item(0).getTextContent();
            int databaseConnections = Integer.parseInt(doc
                    .getElementsByTagName("DatabaseConnections").item(0)
                    .getTextContent());

            ServerLogger.setup(loggingLevel, logOutputPath);

            ServerManager instance = new ServerManager(workerThreads,
                    clientsPerWorker, listeningPort);
            instance.configureDatabaseConnectionPool(databaseUser,
                    databasePassword, databaseServer, databaseName,
                    databaseConnections);
            return instance;
        } catch (SAXException saxex) {
            saxex.printStackTrace();
        } catch (ParserConfigurationException pcex) {
            pcex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (ConfigurationErrorException e) {
            e.printStackTrace();
        }
        return null;
    }
}
