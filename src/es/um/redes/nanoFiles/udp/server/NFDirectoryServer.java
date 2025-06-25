package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	
	/*
	 * Estructura que almacaena los usuarios conectados que son servidores de ficheros junto a su puerto 
	 */
	private HashMap<String, Integer> registeredServers;
	
	/*
	 * Estructura que almacena los ficheros publicados por los servidores de ficheros 
	 */
	private HashMap<String, HashSet<FileInfo>> publishedFilesbyUsers;
	private HashMap<String, HashSet<String>> usersWhoPublishedAFile;
	
	/*
	 * Estructura que almacena todos los ficheros subidos
	 *  
	 */
	private HashSet<FileInfo> publishedFiles;
	
	
	private HashMap<String, InetSocketAddress> serverAddresses;
	
	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	private Random random = new Random();
	
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		socket = new DatagramSocket(DIRECTORY_PORT);
		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();
		registeredServers = new HashMap<String, Integer>();
		publishedFilesbyUsers = new HashMap<String, HashSet<FileInfo>>();
		usersWhoPublishedAFile = new HashMap<String, HashSet<String>>();
		publishedFiles = new HashSet<FileInfo>();
		serverAddresses = new HashMap<String, InetSocketAddress>();
		
		
		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	
	
	public void run() throws IOException {
		byte[] receptionBuffer = null;
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket requestPacket = new DatagramPacket (receptionBuffer, receptionBuffer.length);

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			socket.receive(requestPacket);
			dataLength = requestPacket.getLength();
			clientAddr = (InetSocketAddress) requestPacket.getSocketAddress(); 
			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
				messageFromClient = new String(receptionBuffer, 0 , dataLength); 
						
				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
				
					if(messageFromClient.equals("login")) {	
						String[] arrayMsgToClient = messageFromClient.split("&");
						if (!this.nicks.containsKey(arrayMsgToClient[1])) {
						
						String messageToClient = new String("loginok");
						byte[] dataToClient = messageToClient.getBytes();
						DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
						socket.send(packetToClient);
						}
						else System.out.println("Login_failed:-1");
					}
					else {
						System.out.println("Error : no length");
						
					}

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}

					//Directorio recibe mensaje por parte de un cliente
					DirMessage drFromClient = DirMessage.fromString(messageFromClient);
					//Dependiendo del tipo del mensaje, creamos un mensaje de respuesta y se lo enviamos al cliente
					DirMessage drToClient = buildResponseFromRequest(drFromClient, clientAddr);
					byte[] responseToClient = drToClient.toString().getBytes();
					DatagramPacket pcktToClient = new DatagramPacket(responseToClient, responseToClient.length, clientAddr);
					socket.send(pcktToClient);

				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		
		//Creacion de variables
		DirMessage m = null;
		int sessionKey;
		String nickname;
		
		//Actuar segun el codigo de operacion del mensaje
		switch (msg.getOperation()){
			
			//**************************OPERACION LOGIN**************************
			case DirMessageOps.OPERATION_LOGIN:
				System.out.println("Received login request from " + clientAddr);
				int condition = 0;
				//Inicialización
				nickname = msg.getNickname();
				
				//Comprobacion de que no exista ningun cliente conectado al directorio con el mismo nombre de usuario
				if (nickname.contains(DirectoryConnector.INVALID_CHARACTER_NICKNAME) || nickname.contains(DirectoryConnector.INVALID_CHARACTER_NICKNAME2) ) condition = 1;
				else if (this.nicks.containsKey(nickname)) condition = 2;
				
				
				if (condition == 0) {
					
					//Generacion de una clave para el cliente
					sessionKey = random.nextInt(10000);
					while(this.sessionKeys.containsKey(sessionKey)) sessionKey = random.nextInt(10000);
					this.nicks.put(nickname, sessionKey);
					this.sessionKeys.put(sessionKey, nickname);
					
					//Creacion del mensaje de confirmacion
					m = new DirMessage(DirMessageOps.LOGIN_OK, sessionKey);
					System.out.println("* Client " + clientAddr + " succesfully registered username " + nickname);
				
				}
				
				
				else {
					//Creacion del mensaje de error
					if (condition == 1) {
						System.out.println("* Client " + clientAddr + "failed to register username: username contains '*' or ',");
						m = new DirMessage(DirMessageOps.INVALID_USERNAME);
						}
					else{
						System.out.println("* Client " + clientAddr + "failed to register username: username isn't available");
						m = new DirMessage(DirMessageOps.USERNAME_ALREADY_REGISTERED);
						}
					
					
					}
				
	
				System.out.println("Sent login response to " + clientAddr);
				
				break;
		
				
			//**************************OPERACION LOGOUT**************************
			case DirMessageOps.OPERATION_LOGOUT:
				System.out.println("Received logout request from " + clientAddr);
				
				//Inicializacion
				sessionKey = msg.getSessionKey();			
				nickname = this.sessionKeys.get(sessionKey);
				
				//Actualizacion de estructuras de datos
				this.nicks.remove(nickname);
				this.sessionKeys.remove(sessionKey);
				
				//Creacion del mensaje de confirmacion
				m = new DirMessage(DirMessageOps.LOGOUT_OK);
				
				System.out.println("* Client " + clientAddr + " succesfully deregistered username " + nickname);
				System.out.println("Sent logout response to " + clientAddr);
				
				break;				

				
			//**************************OPERACION USERLIST**************************
			case DirMessageOps.OPERATION_GET_USERLIST:
				System.out.println("Received userlist request from " + clientAddr);
				String[] users = new String[this.nicks.size()];
				int i = 0;
				for (String nick: this.nicks.keySet()) {
					if (this.registeredServers.containsKey(nick)) users[i]=nick+DirectoryConnector.INVALID_CHARACTER_NICKNAME;
					else users[i] = nick;
					i++;
					
				}
				//Creación del mensaje de confirmación
				m = new DirMessage(DirMessageOps.GET_USERLIST_OK, String.join(",", users));
				System.out.println("* Client " + clientAddr + " succesfully obtained userlist.");
				System.out.println("Sent userlist response to " + clientAddr);
				
				break;
			
				
			//**************************OPERACION FILELIST**************************
			case DirMessageOps.OPERATION_FILELIST:
				System.out.println("Received filelist request from " + clientAddr);
				
				//Inicializacion
				
				int length = publishedFiles.size(); 
				i = 0;
				String[] filenames = new String[length];
				String[] filehashs = new String[length];
				String[] filesizes = new String[length];
				String[] servers = new String[length];
				
				
				//Creacion del mensaje de confirmacion
				
				for (FileInfo file : publishedFiles) {
					filenames[i] = file.fileName;
					filehashs[i] = file.fileHash;
					filesizes[i] = Long.toString(file.fileSize);
					//Para filelist se utiliza el asterisco para separar servidores que han publicado un mismo fichero
					//Mientras que la coma se utiliza para separar listas de servidores
					servers[i] = String.join(DirectoryConnector.INVALID_CHARACTER_NICKNAME,usersWhoPublishedAFile.get(file.fileHash));
					i++;
					}
				
				m = new DirMessage(DirMessageOps.FILELIST_OK, String.join(",", servers), 0, String.join(",", filehashs), String.join(",", filenames), String.join(",", filesizes));
				System.out.println("Sent filelist response to " + clientAddr);
					
				break;
				
				
			//**************************OPERACION REGISTER_SERVER**************************
			case DirMessageOps.OPERATION_REGISTER_SERVER:
				System.out.println("Received register_server request from " + clientAddr);
				
				//Inicializacion
				sessionKey = msg.getSessionKey();
				nickname = this.sessionKeys.get(sessionKey);
				int port = msg.getPort();
				
				//Creacion del mensaje de confirmacion o de error
				if (! registeredServers.containsValue(port)) {
					registeredServers.put(nickname, port);
					serverAddresses.put(nickname, new InetSocketAddress(clientAddr.getAddress(), port));
					//Mensaje de confirmacion
					m = new DirMessage(DirMessageOps.SERVER_REGISTERED);
					System.out.println("* Client " + clientAddr + " (" + nickname + ")" + " successfully registered as server");
					 }
					 
				else {
					//Mensaje de error
					m = new DirMessage(DirMessageOps.REGISTER_SERVER_FAIL);
					 System.out.println("* Client " + clientAddr + " failed to register as server: Invalid Port ");
					}
				
				 break;
			
				 
			//**************************OPERACION UNREGISTER_SERVER**************************
			case DirMessageOps.OPERATION_UNREGISTER_SERVER:
				System.out.println("Received unregister_server request from " + clientAddr);
				
				//Inicializacion
				sessionKey = msg.getSessionKey();
				nickname = this.sessionKeys.get(sessionKey);
				
				//Actualizacion de estructuras de datos
				registeredServers.remove(nickname);
				HashSet<FileInfo> files = publishedFilesbyUsers.get(nickname);
				if (files != null) {
				for (FileInfo file : files) {
					publishedFiles.remove(file);
					usersWhoPublishedAFile.get(file.fileHash).remove(nickname);
					}
				serverAddresses.remove(nickname);
				}

				//Creacion del mensaje de confirmacion
				m = new DirMessage(DirMessageOps.SERVER_UNREGISTERED);
				System.out.println("* Client " + clientAddr + " (" + nickname + ")" + " successfully unregistered as server");
				
				
				break;
			
				
			//**************************OPERACION PUBLISH**************************
			case DirMessageOps.OPERATION_PUBLISH:
				System.out.println("Received publish_files request from " + clientAddr);
				
				//Inicializacion
				sessionKey = msg.getSessionKey();
				nickname = this.sessionKeys.get(sessionKey);
				
				if (msg.getFilehashs()!=null) {
					
					//Obtencion de los ficheros a partir del mensaje publish
					filehashs = msg.getFilehashs().split(",");
					filenames = msg.getFilenames().split(",");
					filesizes = msg.getFilesizes().split(",");
					
					
					//Creacion de los fileinfo + actualizacion de la estructura de datos que contienen los ficheros publicados 
					
					for (i = 0 ; i<filehashs.length;i++) {
						if (!publishedFilesbyUsers.containsKey(nickname)) publishedFilesbyUsers.put(nickname, new HashSet<FileInfo>());
						if (!usersWhoPublishedAFile.containsKey(filehashs[i])) usersWhoPublishedAFile.put(filehashs[i], new HashSet<String>());
						FileInfo aux = new FileInfo(filehashs[i], filenames[i], Long.parseLong(filesizes[i]), NanoFiles.sharedDirname);
						
						publishedFilesbyUsers.get(nickname).add(aux);
						usersWhoPublishedAFile.get(filehashs[i]).add(nickname);
						publishedFiles.add(aux);
						
					}

				}
				
				//Creacion del mensaje de confirmacion
				m = new DirMessage(DirMessageOps.PUBLISH_OK);
				
				System.out.println("Sent publish_files request from " + clientAddr);
				
			break;
			
			
			//**************************OPERACION SEARCH**************************
			case DirMessageOps.OPERATION_SEARCH:
				System.out.println("Received search request from " + clientAddr);
				
				//Inicializacion
				boolean cond = true;
				String filehash = msg.getFilehashs();
				
				//Obtencion de los ficheros identificados por el hash/subhash pasado
				FileInfo[] filesfound = FileInfo.lookupHashSubstring(publishedFiles.toArray(new FileInfo[publishedFiles.size()]), filehash);
				
				//Comprobaciones de que se ha encontrado solo UN fichero
				if (filesfound.length == 0) {
					//Creacion del mensaje de error por no encontrar ningun fichero identificado por el hash pasado
					m = new DirMessage(DirMessageOps.FILE_NOT_FOUND);
					System.out.println("Specified filehash does not match any published file");
				}
				else if (filesfound.length >= 1){
					
				
					
					//Bucle recorriendo cada fichero encontrado
					for(i = 0; i < filesfound.length; i++) {
						//Condición que comprueba si todos los ficheros encontrados tienen el mismo hash
						if ( ! filesfound[0].fileHash.equals(filesfound[i].fileHash)) {cond=false;break;} //Hash ambiguo
						}
					
					//Creacion del mensaje de error por encontrar más de un fichero identificado por el hash pasado
					
					if (!cond) {
						m = new DirMessage(DirMessageOps.AMBIGOUS_FILEHASH);
						System.out.println("Specified filehash is ambigous");
					}
					
					else {
						HashSet<String> nicknames = usersWhoPublishedAFile.get(filesfound[0].fileHash);
						m = new DirMessage(DirMessageOps.SEARCH_OK, String.join(",", nicknames.toArray(new String[nicknames.size()])));
						
					}
				}

				System.out.println("Sent search request from " + clientAddr);					

				break;

			//**************************OPERACION GET SERVER ADDR**************************
			case DirMessageOps.OPERATION_GETSERVERADDR:
				nickname = msg.getNickname();
				InetSocketAddress srvAddr = serverAddresses.get(nickname);
				
				if (srvAddr != null) {
					m = new DirMessage(DirMessageOps.GETSERVERADDR_OK, serverAddresses.get(nickname).toString() );
				}

				else m = new DirMessage(DirMessageOps.GETSERVERADDR_FAIL);
				
				break;
				
			//**************************EN CASO DE QUE SE INTRODUZCA UN CODIGO DE OPERACION NO VÁLIDO**************************
			default:
				System.out.println("Unexpected message operation: \"" + msg.getOperation() + "\"");
			
		}

		return m;

		
	}
	

	
	
}

	
