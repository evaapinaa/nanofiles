package es.um.redes.nanoFiles.logic;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;

public class NFControllerLogicP2P {
	/*
	 * TODO: Para bgserve, se necesita un atributo NFServer que actuará como
	 * servidor de ficheros en segundo plano de este peer
	 */
	private NFServer bgFileServer;

	protected NFControllerLogicP2P() {

	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * 
	 */
	protected void foregroundServeFiles() {
		/*
		 * TODO: Crear objeto servidor NFServerSimple y ejecutarlo en primer plano.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		NFServerSimple serverSimple = null;

		try {
			serverSimple = new NFServerSimple();
		} catch (IOException e) {
			System.err.println("Error: Unable to start the server.");
			return;
		}
		serverSimple.run();

	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		/*
		 * 
		 * TODO: Comprobar que no existe ya un objeto NFServer previamente creado, en
		 * cuyo caso el servidor ya está en marcha. Si no lo está, crear objeto servidor
		 * NFServer y arrancarlo en segundo plano creando un nuevo hilo. Finalmente,
		 * comprobar que el servidor está escuchando en un puerto válido (>0) e imprimir
		 * mensaje informando sobre el puerto, y devolver verdadero.
		 */
		if (bgFileServer != null) {
			System.err.println("* Error: A server is already running");
		} else {
			try {
				bgFileServer = new NFServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bgFileServer.startServer();
			int port = bgFileServer.getLocalPort();
			if (port <= 0) {
				System.err.println("* Error: Invalid port");
			} else {
				System.out.println("Server listening in port " + port);
				return true;
			}
		} /*
			 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
			 * este método. Si se produce una excepción de entrada/salida (error del que no
			 * es posible recuperarse), se debe informar sin abortar el programa
			 */

		return false;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,
			String localFileName) {

		boolean result = false;

		if (fserverAddr == null) {
			System.err.println("* Cannot start download - No server address provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con el peer
		 * servidor de ficheros, y usarlo para descargar el fichero mediante su método
		 * "downloadFile". Se debe comprobar previamente si ya existe un fichero con el
		 * mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
		 * descarga. Si todo va bien, imprimir mensaje informando de que se ha
		 * completado la descarga.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */

		File file = new File(NanoFiles.sharedDirname + "/" + localFileName);
		if (file.exists()) {
			System.err.println("Error: File " + localFileName + " already exists.");
			return false;
		}

		NFConnector nfConnector;
		try {
			nfConnector = new NFConnector(fserverAddr);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + fserverAddr);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("I/O error when connecting to " + fserverAddr);
			e.printStackTrace();
			return false;
		}

		try {
			result = nfConnector.downloadFile(targetFileHash, file);
			if (result) {
				System.out.println("Download completed successfully for " + localFileName);
			} else {
				System.out.println("Download failed for " + localFileName);
			}
		} catch (IOException e) {
			System.err.println("Error during file download: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		return result;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(LinkedList<InetSocketAddress> serverAddressList,
			String targetFileHash, String localFileName) {
		boolean downloaded = false;

		if (serverAddressList == null) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con cada
		 * servidor de ficheros, y usarlo para descargar un trozo (chunk) del fichero
		 * mediante su método "downloadFileChunk". Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre en esta máquina, en cuyo caso se
		 * informa y no se realiza la descarga. Si todo va bien, imprimir mensaje
		 * informando de que se ha completado la descarga.
		 */

		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */

		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		int port = 0;
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros en
		 * segundo plano
		 */
		if (bgFileServer != null) {
			port = bgFileServer.getLocalPort();
		}
		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		if (bgFileServer != null) {
			bgFileServer.stopServer();
			bgFileServer = null;
			System.out.println("Background file server stopped.");
		} else {
			System.out.println("No background file server running.");
		}

	}

}
