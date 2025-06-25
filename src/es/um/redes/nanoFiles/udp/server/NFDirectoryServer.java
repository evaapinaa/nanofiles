package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
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
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */

	private HashMap<Integer, Integer> servers = new HashMap<Integer, Integer>();
	private HashMap<Integer, InetSocketAddress> serverAddressesPort = new HashMap<Integer, InetSocketAddress>();
	private HashMap<String, FileInfo> publicFiles = new HashMap<String, FileInfo>();

	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
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
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */

		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();
		servers = new HashMap<Integer, Integer>();
		serverAddressesPort = new HashMap<Integer, InetSocketAddress>();
		publicFiles = new HashMap<String, FileInfo>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null || servers == null) {
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
		/*
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// TODO: (Boletín UDP) Recibimos a través del socket un datagrama
			socket.receive(packetFromClient);

			// TODO: (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = packetFromClient.getLength();

			// TODO: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			clientAddr = (InetSocketAddress) packetFromClient.getSocketAddress();

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
				/*
				 * TODO: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());
				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					if (messageFromClient.equals("login")) {
						String messageToClient = new String("loginok");
						byte[] dataToClient = messageToClient.getBytes();
						DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length,
								clientAddr);
						socket.send(packetToClient);
					} else {
						System.err.println("Error: no login.");
					}

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}

					/*
					 * TODO: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());
					System.out.println("** Received message: " + messageFromClient);
					DirMessage dirMessage = DirMessage.fromString(messageFromClient);

					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					DirMessage responseMessage = buildResponseFromRequest(dirMessage, clientAddr);

					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					String messageToClient = responseMessage.toString();
					byte[] dataToClient = messageToClient.getBytes();
					DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(packetToClient);

				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();

		DirMessage response = null;

		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();

			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
			 * para generar la session key
			 */
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */

			if (!nicks.containsKey(username)) {
				int sessionKey = random.nextInt(10000);
				nicks.put(username, sessionKey);
				sessionKeys.put(sessionKey, username);

				response = new DirMessage(DirMessageOps.OPERATION_LOGIN_OK);
				response.setSessionKey(Integer.toString(sessionKey));
				System.out.println("** Login successful for " + username + " with sessionKey: " + sessionKey);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGIN_FAILED);
				System.err.println("Error: User already registered.");
			}
			break;
		}

		case DirMessageOps.OPERATION_LOGOUT: {
			Integer sessionKey = Integer.parseInt(msg.getSessionKey());

			if (sessionKeys.containsKey(sessionKey)) {
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUT_OK);
				String username = sessionKeys.get(sessionKey);
				sessionKeys.remove(sessionKey, username);
				nicks.remove(username, sessionKey);
				System.out.println("Removing user... " + username + " with sessionKey: " + sessionKey);
				System.out.println("** Loggin out.");

			} else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUT_FAILED);
				System.err.println("User not found or already logged out.");
			}
			break;
		}

		case DirMessageOps.OPERATION_USER_LIST: {
			Integer sessionKey = Integer.parseInt(msg.getSessionKey());
			if (!sessionKeys.containsKey(sessionKey)) {
				response = new DirMessage(DirMessageOps.OPERATION_USER_LIST_FAILED);
				System.out.println("** No user found for session key: " + sessionKey);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_USER_LIST_OK);
				response.setSessionKey(msg.getSessionKey());
				StringBuilder users = new StringBuilder();
				for (String name : nicks.keySet()) {
					int userSessionKey = nicks.get(name);
					if (servers.containsKey(userSessionKey)) {
						users.append(name).append(" - Serving files in port ").append(servers.get(userSessionKey))
								.append(",");
					} else {
						users.append(name).append(",");
					}
				}
				response.setUsers(users.toString());
				System.out.println("** Users listed for user with sessionKey: " + sessionKey);
			}
			break;
		}

		case DirMessageOps.OPERATION_REGISTER_SERVER_PORT: {
			Integer sessionKey = Integer.parseInt(msg.getSessionKey());
			int port = Integer.parseInt(msg.getPort());
			if (!servers.containsKey(sessionKey)) {
				servers.put(sessionKey, port);
				serverAddressesPort.put(sessionKey, clientAddr);
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_PORT_OK);
				System.out.println("** Server registered with sessionKey: " + msg.getSessionKey());
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_PORT_FAILED);
				System.err.println("Error: User not found.");
			}
			break;
		}

		case DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT: {
			Integer sessionKey = Integer.parseInt(msg.getSessionKey());
			if (servers.containsKey(sessionKey)) {
				servers.remove(sessionKey);
				response = new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT_OK);
				System.out.println("** Server unregistered with sessionKey: " + sessionKey);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT_FAILED);
				System.err.println("Error: Server not found.");
			}
			break;
		}

		case DirMessageOps.OPERATION_DOWNLOADFROM: {
			String nickname = msg.getNickname();
			int sessionKey = nicks.get(nickname);
			if (servers.containsKey(sessionKey) && serverAddressesPort.containsKey(sessionKey)) {
				response = new DirMessage(DirMessageOps.OPERATION_DOWNLOADFROM_OK);
				response.setPort(Integer.toString(servers.get(sessionKey)));
				response.setAddress(serverAddressesPort.get(sessionKey).getAddress().getHostAddress());
				System.out.println("** Server port found with nick: " + sessionKey);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_DOWNLOADFROM_FAILED);
				System.err.println("Error: Server not found.");
			}
			break;
		}

		case DirMessageOps.OPERATION_PUBLISH: {
			Integer sessionKey = Integer.parseInt(msg.getSessionKey());

			if (sessionKey != null && sessionKeys.containsKey(sessionKey) && servers.containsKey(sessionKey)) {
				String nickname = sessionKeys.get(sessionKey);
				String fileNames = msg.getFileName();
				String fileSizes = msg.getFileSize();
				String fileHashes = msg.getFileHash();
				String filePaths = msg.getFilePath();

				String[] names = fileNames.split(",");
				String[] sizes = fileSizes.split(",");
				String[] hashes = fileHashes.split(",");
				String[] paths = filePaths.split(",");

				if (names.length != sizes.length || names.length != hashes.length || names.length != paths.length) {
					System.err.println("Error: Mismatch in data lengths of file attributes.");
					response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_FAILED);
					break;
				}

				for (int i = 0; i < names.length; i++) {
					try {
						long size = Long.parseLong(sizes[i]);
						if (names[i] == null || hashes[i] == null || paths[i] == null) {
							System.err.println("Error: Null values found in file attributes.");
							continue;
						}
						if (publicFiles.containsKey(hashes[i])) {
							continue;
						}
						FileInfo file = new FileInfo(hashes[i], names[i], size, paths[i]);
						publicFiles.put(hashes[i], file);
					} catch (NumberFormatException e) {
						System.err.println("Error parsing file size for " + names[i]);
						continue;
					}
				}

				response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_OK);
				System.out.println("** File published by user: " + nickname);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_FAILED);
				System.err.println("Error: User not found or not registered as a server.");
			}
			break;
		}

		case DirMessageOps.OPERATION_FILE_LIST: {
			StringBuilder fileListBuilder = new StringBuilder();
			if (publicFiles.isEmpty()) {
				response = new DirMessage(DirMessageOps.OPERATION_FILE_LIST_FAILED);
			} else {
				for (FileInfo file : publicFiles.values()) {
					fileListBuilder.append(file.fileName).append(",").append(file.fileHash).append(",")
							.append(file.fileSize).append(",").append(file.filePath).append(";");
				}
				if (fileListBuilder.length() > 0) {
					fileListBuilder.setLength(fileListBuilder.length() - 1);
				}

				response = new DirMessage(DirMessageOps.OPERATION_FILE_LIST_OK);

				response.setFiles(fileListBuilder.toString());
				System.out.println("Response: " + response.toString());
			}
			break;
		}

		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}
}
