package es.um.redes.nanoFiles.logic;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;





public class NFControllerLogicP2P {
	//objeto NFServer utilizado para el servidor de ficheros en segundo plano
	private NFServer srv = null;
	
	protected NFControllerLogicP2P() {

	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * 
	 */
	protected void foregroundServeFiles() {
			//Creacion de objeto NFServerSimple, utilizado para el servidor de ficheros en primer plano
			NFServerSimple server = null;
			try {
				server = new NFServerSimple();
				server.run();
			} catch (IOException e) {
					System.err.println("*Error: Unable to start the server*");
			}
			
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
		boolean result = false;
		boolean cond = false;
		//Debido a que bgserve solo se puede ejecutar cuando el cliente está
		//en un estado distinto a SERVER, quiere decir que server siempre va a ser null
		//por lo que no requerimos de una comprobacion previa
			while (!cond) {
			try {
				srv = new NFServer();
				result = true;
				srv.startServer();
				cond = true;
				System.out.println("* You are now serving files on port " + srv.getServerPort());
				System.out.println("* NFServer server running on " + srv.getServerAddress());
				} catch (IOException e) {
					if (!(e instanceof BindException)){
					cond=true;
					System.out.println("* Error trying to launch the peer as background server");
					}
					
					}
			}
				
		
		
	


		
		return result;
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
		
		NFConnector nfConnector = null;
		try {
			nfConnector = new NFConnector(fserverAddr);
		} catch (UnknownHostException e) {
			nfConnector = null;
		} catch (IOException e) {
			nfConnector = null;
		}
		
		if(nfConnector == null) {
			System.err.println("*Error: Unable to connect to server*");
			return false;
		}
		
		File f = new File(NanoFiles.sharedDirname + "/" + localFileName);
		
		if(f.exists()) {
			System.err.println("*Error: The destination file already exists*");
			return false;
		} 
		
		else {
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.err.println("*Error trying to create destination file ");
				return false;
			}
		}
		
		try {
			result = nfConnector.downloadFile(targetFileHash, f);
			if (result == false) {
				System.out.println("Error downloading the file");
				f.delete();
			}
		} catch (IOException e) {
			result = false;
			f.delete();
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
		
		//Inicializacion
		NFConnector srvConnection;
		File f = new File(NanoFiles.sharedDirname + "/" + localFileName);

		
		//Comprobamos si el fichero donde se guardará el contenido descargado existe. En ese caso devuelve falso.
		if(f.exists()) {
			System.err.println("* Error: The destination file already exists*");
			return false;
		} 
		
		//Fichero destino no existe. Se intenta crear.
		else {
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.err.println("*Error trying to create destination file ");
				return false;
			}
		
		
			
		//Para cada direccion de un servidor de ficheros se trata de establecer conexión con este para la descarga del fichero.
		
		
		int n = 1;
		int nservers = serverAddressList.size();
		for (InetSocketAddress server : serverAddressList) {
			
			try {
				srvConnection = new NFConnector(server);
				downloaded = srvConnection.downloadFileChunk(targetFileHash, f, n, nservers);
				if (!downloaded) {
					System.out.println("Error downloading the file");
					f.delete();
					return false;
				}
			} catch (IOException e) {
				System.out.println("* Error trying to establish connection with server " + server.toString() );
				f.delete();
				return false;	
			}
			n++;
			}
		
		}
		
		
		
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
		return srv.getServerPort();
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		this.srv.stopServer();
		this.srv = null;
	



	}

}
