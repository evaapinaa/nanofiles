package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;
	
	public static final String INVALID_CHARACTER_NICKNAME = "*";
	public static final String INVALID_CHARACTER_NICKNAME2 = ",";
	
	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;

	public DirectoryConnector(String address) throws IOException {
		//Constructor de la clase
		InetAddress adressInetAddress = InetAddress.getByName(address);
		directoryAddress = new InetSocketAddress(adressInetAddress, DIRECTORY_PORT);	
		socket = new DatagramSocket();
	}
	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE]; 
		byte response[] = null; 
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		
		DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, directoryAddress);
		
		
		try {
		socket.send(packetToServer);
		}catch(Exception e) {
			System.out.println("Error to send DatagramPacket");
			
			System.exit(-1);
		}
			
		DatagramPacket packetFromServer = new DatagramPacket(responseData, responseData.length);
		int intentos = 0;
		while (intentos < MAX_NUMBER_OF_ATTEMPTS) {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(packetFromServer);
					
				break;
			}catch (Exception e) {
				intentos++;
				if(intentos == MAX_NUMBER_OF_ATTEMPTS) {
					System.out.println("* Timeout Exception.");
					System.exit(-1);
				}
				try {
					socket.send(packetToServer);
					}catch(Exception ee) {
						System.out.println("*Error to send DatagramPacket");
						System.exit(-1);
				}
		}
		}
		
		int respDataLen = packetFromServer.getLength();
		response = new byte[respDataLen];
		System.arraycopy(responseData, 0 , response, 0, respDataLen);
		

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
	
		boolean success = false;
		String strToSend = new String("login"); 
		byte[] dataToSend = strToSend.getBytes(); 
		byte[] receiveData = null;

		boolean communicationOk = true;
		
		try {
			receiveData = sendAndReceiveDatagrams(dataToSend);
			
		} catch (Exception e) {
			
				communicationOk = false;
				System.out.println("Error in testSendAndReceive. ");
		}
		
		if((communicationOk) && (receiveData != null)) {
			
			int receiveDataLen = receiveData.length;
			if (receiveDataLen > 0 ) {
				String strFromServer = new String(receiveData, 0, receiveDataLen); 
				if (strFromServer.equals("loginok")) {
					success = true;
				}
			}
		}

		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 */
	public boolean logIntoDirectory(String nickname) {
		boolean success = false;
		System.out.println("* Connecting to the directory...");
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_LOGIN, nickname);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
			int sessionKey = responseMessage.getSessionKey();
			if ((responseMessage.getOperation().equals(DirMessageOps.LOGIN_OK)) && (sessionKey != INVALID_SESSION_KEY)) { 
				this.sessionKey = sessionKey;
				System.out.println("* Login successful, session key is " + this.sessionKey);
				success = true;
				}
				
			else {
					
				if (responseMessage.getOperation().equals(DirMessageOps.USERNAME_ALREADY_REGISTERED)) System.out.println("* Login failed (nickname already registered). Try a different one.");
				else System.out.println("* Login failed (nickname can't contains '*' or ','). Try a different one.");
				success = false;
				}

		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() {
		String[] userlist = null;
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_GET_USERLIST, sessionKey);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));	
		//Si el mensaje de confirmacion que recibe es de ltipo GetUserListOk, el mensaje contiene la lista de usuarios
		//conectadas en ese momento al servidor
		if (responseMessage.getOperation().equals(DirMessageOps.GET_USERLIST_OK)) {
			userlist = responseMessage.getNickname().split(",");
			System.out.println("* There are the users registered in the directory "+this.directoryAddress);
		}
		
		
		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 */
	public boolean logoutFromDirectory() {
		boolean success = false;
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_LOGOUT, this.sessionKey);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		//Si el mensaje de confirmacion es del tipo LogoutOk, ha cerrado sesión correctamente.
		if ((responseMessage.getOperation().equals(DirMessageOps.LOGOUT_OK))) {
			this.sessionKey = INVALID_SESSION_KEY;
			System.out.println("* Logout succesful.");
			success = true;
		}
		//En caso contrario, no ha cerrado sesión correctamente.
		else {	
			System.out.println("* Logout failed.");
			success = false;
			}
		
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		boolean success = false;
		//Creacion del mensaje de soliciutd
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER, sessionKey, serverPort);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		//Si el mensaje de confirmacion es del tipo Server_Registered, quiere decir que el usuario se ha registrado
		//como servidor de ficheros exitosamente
		if (responseMessage.getOperation().equals(DirMessageOps.SERVER_REGISTERED)) {
			System.out.println("* File server registered with directory");
			success = true;
		}
		//Si el mensaje de confirmacion es del tipo Register_Server_Fail, quiere decir que no se ha podido
		//registrar en el directorio
		else if (responseMessage.getOperation().equals(DirMessageOps.REGISTER_SERVER_FAIL)) {
			System.out.println("*File server failed to register");
		}

		return success;
	}
	/**
	 * Método para dar de baja como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean unregisterServerPort() {
		boolean success = false;
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER, sessionKey);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		//Si el mensaje de confirmacion es del tipo Server_Unregistered, quiere decir que el server se ha dado de baja exitosamente
		if (responseMessage.getOperation().equals(DirMessageOps.SERVER_UNREGISTERED)) {
			System.out.println("* File server unregistered with directory");
			success = true;
		}
	
		return success;
	}
	
	

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_GETSERVERADDR, nick, sessionKey);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
			//Si el mensaje es del tipo GetServerAddressOk, quiere decir que el usuario se encuentra como servidor de ficheros
			if (responseMessage.getOperation().equals(DirMessageOps.GETSERVERADDR_OK)) { 
				//Obtencion de la ip a partir del mensaje de confirmacion
				String strServerAddr = responseMessage.getServerAddr();
				int idx = strServerAddr.indexOf(":");
				String ip = strServerAddr.substring(1,idx).trim();
				String port = strServerAddr.substring(idx + 1).trim();
				
				try {
					serverAddr = new InetSocketAddress(InetAddress.getByName(ip),Integer.parseInt(port));
				} catch (NumberFormatException | UnknownHostException e) {
					System.out.println("* Error trying to obtain server address for " + nick);
				}
				
				}
			//Si el mensaje es del tipo GetServerAddressFail, quiere decir que el usuario no esta registrado como servidor de ficheros
			else if (responseMessage.getOperation().equals(DirMessageOps.GETSERVERADDR_FAIL)) {
				System.out.println("* Error trying to obtain server Address for " + nick);
					}

		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;
		DirMessage dmToDirectory;
		if (files.length!=0) {
			//Creacion de los campos del mensaje
			String[] filehashs = new String[files.length];
			String[] filenames = new String[files.length];
			String[] filesizes = new String[files.length];

			//Inicializacion de los campos anteriores
			for (int i = 0 ; i < files.length ; i++) {
				filehashs[i] = files[i].fileHash;
				filenames[i] = files[i].fileName;
				filesizes[i] = Long.toString(files[i].fileSize);
			
			}
			//Creacion del mensaje de solicitud
			dmToDirectory = new DirMessage(DirMessageOps.OPERATION_PUBLISH, sessionKey, String.join(",", filehashs), String.join(",", filenames), String.join(",", filesizes));
			
		}
		else dmToDirectory = new DirMessage(DirMessageOps.OPERATION_PUBLISH, sessionKey);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		//Recepcion del mensaje de confirmacion
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		if (responseMessage.getOperation().equals(DirMessageOps.PUBLISH_OK)) { 
			System.out.println("* List of local files published to the directory");
			success = true;	
		}
		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_FILELIST, sessionKey);
		
		//Pasamos el mensaje a un array de bytes para su envio
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		
		//Envio del mensaje y recepcion de la respuesta por parte del directorio
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		
		if ((responseMessage.getOperation().equals(DirMessageOps.FILELIST_OK))) {  
		
			//Mensaje de confirmacion recibido
			System.out.println("* These are the files tracked by the directory " + this.directoryAddress);
			
			//Obtencion de los datos de los ficheros
			String[] filenames = responseMessage.getFilenames().split(",");
			String[] filehashs = responseMessage.getFilehashs().split(",");
			String[] filesizes = responseMessage.getFilesizes().split(",");
			String[] servers = responseMessage.getNickname().split(",");
			
			int length = filenames.length;
			//Si la longitud de los arrays es uno y el unico elemento que contienen es una cadena vacia, entonces no hay ficheros
			if (filenames.length == 1 && filenames[0].equals("")) length--;
			filelist = new FileInfo[length];
		
			for (int i = 0; i < length; i++) {
				filelist[i] = new FileInfo(filehashs[i], filenames[i], Long.parseLong(filesizes[i]), NanoFiles.sharedDirname, servers[i].replace("*",","));
				}
			}
	
		else System.out.println("* Filelist failed.");
		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		//Creacion del mensaje de solicitud
		DirMessage dmToDirectory= new DirMessage(DirMessageOps.OPERATION_SEARCH, sessionKey, fileHash);
		String strToDirectory = dmToDirectory.toString();
		byte[] msgToDirectory = strToDirectory.getBytes();
		//Envio del mensaje de solicitud
		byte [] responseData = sendAndReceiveDatagrams(msgToDirectory);						
		//Recepcion de la respuesta del mensaje de solicitud
		DirMessage responseMessage = DirMessage.fromString(new String(responseData, 0, responseData.length));
		//Si la respuesta es un search ok, el mensaje contiene los nicknames que estan compartiendo dicho fichero
		if (responseMessage.getOperation().equals(DirMessageOps.SEARCH_OK)) nicklist = responseMessage.getNickname().split(",");
		return nicklist;
	}




}
