package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	
	//directoryHostname == Ip del servidor
	protected void testCommunicationWithDirectory(String directoryHostname) throws IOException {
		assert (NanoFiles.testMode); //si alguien llama a este metodoy rsulta que testMode == false, ocurre una excepción
		System.out.println("[testMode] Testing communication with directory...");
		/*
		 * Crea un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * lo utiliza para hacer una prueba de comunicación con el directorio.
		 */
		//DirectoryConnector nos permite la comunicacion entre Directorio y Nanofiles
		DirectoryConnector directoryConnector = new DirectoryConnector(directoryHostname);
		if (directoryConnector.testSendAndReceive()) { //Este metodo hace literalmente todo el trabajo de la comunicacion y espera de este fokin metodo
			
			System.out.println("[testMode] Test PASSED!");
		} else {
			System.err.println("[testMode] Test FAILED!");
		}
	}

	/**
	 * Método para conectar con el directorio y obtener la "sessionKey" que se
	 * deberá utilizar en lo sucesivo para identificar a este cliente ante el
	 * directorio
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected boolean doLogin(String directoryHostname, String nickname) {
		//Creacion de un objeto DirectoryConnector utilizado por el resto de 
		//la clase
		boolean result = false;
		try {
		directoryConnector = new DirectoryConnector(directoryHostname);
		
		}catch(IOException e) {
			System.out.println("Error with Directory Address.");
		}
		
		result = directoryConnector.logIntoDirectory(nickname);
		return result;
	}

	/**
	 * Método para desconectarse del directorio: cerrar sesión y dar de baja el
	 * nombre de usuario registrado
	 */
	public boolean doLogout() {
		boolean result = directoryConnector.logoutFromDirectory();
		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	protected boolean getAndPrintUserList() {
		boolean result = false;
		String[] usernames = directoryConnector.getUserList(); 
		if ((usernames != null) && usernames.length > 0) {
			result = true;
			 System.out.printf("%-20s%-15s\n", "User", "Is A Server?");
	for ( String user : usernames) {
				//Si contiene un asterisco, quiere decir que este usuario es un servidor de ficheros
				//Para usar el asterisco con este proposito, se ha tenido que prohibir el registro
				//en el directorio de cualquier usuario cuyo nickname contenga un asterisco.
				if (user.contains(DirectoryConnector.INVALID_CHARACTER_NICKNAME)) System.out.printf("%-20s%-15s\n", user.replace("*", ""), "Yes");
				else System.out.printf("%-20s%-15s\n", user, "No");
			}
		}


		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected boolean getAndPrintFileList() {
		boolean result = false;
		FileInfo[] files = directoryConnector.getFileList();
		if (files != null) {
			result = true;
			FileInfo.printToSysout(files);
		}
		return result;
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado
	 * 
	 * @param serverPort el puerto en el que está escuchando nuestro servidor de
	 *                   ficheros
	 */

	public boolean registerFileServer(int serverPort) {	
		boolean result =directoryConnector.registerServerPort(serverPort);
		return result;
	}

	/**
	 * Método para enviar al directorio la lista de ficheros que este peer servidor
	 * comparte con el resto (ver método filelist).
	 * 
	 */
	protected boolean publishLocalFiles() {
		boolean result = directoryConnector.publishLocalFiles(NanoFiles.db.getFiles());
		return result;
	}

	/**
	 * Método para consultar al directorio el nick de un peer servidor y obtener
	 * como respuesta la dirección de socket IP:puerto asociada a dicho servidor
	 * 
	 * @param nickname el nick del servidor por cuya IP:puerto se pregunta
	 * @return La dirección de socket del servidor identificado por dich nick, o
	 *         null si no se encuentra ningún usuario con ese nick que esté
	 *         sirviendo ficheros.
	 */
	private InetSocketAddress lookupServerAddrByUsername(String nickname) {
		InetSocketAddress serverAddr = directoryConnector.lookupServerAddrByUsername(nickname);
		return serverAddr;
	}

	/**
	 * Método para obtener la dirección de socket asociada a un servidor a partir de
	 * una cadena de caracteres que contenga: i) el nick del servidor, o ii)
	 * directamente una IP:puerto.
	 * 
	 * @param serverNicknameOrSocketAddr El nick o IP:puerto del servidor por el que
	 *                                   preguntamos
	 * @return La dirección de socket del peer identificado por dicho nick, o null
	 *         si no se encuentra ningún peer con ese nick.
	 */
	public InetSocketAddress getServerAddress(String serverNicknameOrSocketAddr) {
		InetSocketAddress fserverAddr = null;
		/* Si el nickname contiene un punto, entonces sigue el formato IP:Puerto*/
		if (serverNicknameOrSocketAddr.contains(":")) { // Then it has to be a socket address (IP:port)
			//Creacion del objeto InetSocketAddress a devolver que contiene la IP y Puerto del servidor.
			int idx = serverNicknameOrSocketAddr.indexOf(":");
			String ip = serverNicknameOrSocketAddr.substring(0,idx).trim();
			String port = serverNicknameOrSocketAddr.substring(idx + 1).trim();
			
			try {
				fserverAddr = new InetSocketAddress(InetAddress.getByName(ip),Integer.parseInt(port));
			} catch (NumberFormatException | UnknownHostException e) {
				System.out.println("Error: Unknown Host");
			}
		
		} 
		//Si no sigue el formato, entonces comprobamos si dicho usuario está registrado en el directorio como servidor de ficheros
		else fserverAddr = lookupServerAddrByUsername(serverNicknameOrSocketAddr);
		
		return fserverAddr;
	}

	/**
	 * Método para consultar al directorio los nicknames de los servidores que
	 * tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 */
	public boolean getAndPrintServersNicknamesSharingThisFile(String fileHashSubstring) {
		//Preguntamos mediante directoryConnector la lista de servidores que estan compartiendo el fichero
		//identificado por el subhash pasado como parametro
		boolean result = false;
		String[] nicklist = directoryConnector.getServerNicknamesSharingThisFile(fileHashSubstring);
		if (nicklist!=null) {
			result = true;
			System.out.println ("* Available servers for this file: " + Arrays.toString(nicklist));
		}
		else System.out.println ("* No one has sharing that file");
		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 * @return Una lista de direcciones de socket de los servidores que comparten
	 *         dicho fichero, o null si dicha subcadena del hash no identifica
	 *         ningún fichero concreto (no existe o es una subcadena ambigua)
	 * 
	 */
	public LinkedList<InetSocketAddress> getServerAddressesSharingThisFile(String downloadTargetFileHash) {
		//Metodo que obtiene las ip y puertos de los servidores que estan compartiendo el fichero indicado
		//Por el parametro pasado
		LinkedList<InetSocketAddress> serverAddressList = null;		
		String[] nicknames = directoryConnector.getServerNicknamesSharingThisFile(downloadTargetFileHash);		
		if (nicknames != null) {
			serverAddressList = new LinkedList<InetSocketAddress>();
			for (String nick : nicknames) serverAddressList.add(getServerAddress(nick));
			}
		return serverAddressList;
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	public boolean unregisterFileServer() {
		boolean result = directoryConnector.unregisterServerPort();
		return result;
	}

	protected InetSocketAddress getDirectoryAddress() {
		return directoryConnector.getDirectoryAddress();
	}

}
