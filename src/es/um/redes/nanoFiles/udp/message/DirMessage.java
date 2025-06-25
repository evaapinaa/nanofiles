package es.um.redes.nanoFiles.udp.message;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SESSION_KEY = "sessionkey";
	private static final String FIELDNAME_USER_LIST = "userlist";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_ADDRESS = "address";
	private static final String FIELDNAME_FILE_NAME = "filename";
	private static final String FIELDNAME_FILE_SIZE = "filesize";
	private static final String FIELDNAME_FILE_HASH = "filehash";
	private static final String FIELDNAME_FILE_PATH = "filepath";
	private static final String FIELDNAME_FILES = "files";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private String users;
	private String sessionKey;
	private String port;
	private String address;
	private String files;
	private String fileName;
	private String fileHash;
	private String fileSize;
	private String filePath;

	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {
		assert (operation.equals(DirMessageOps.OPERATION_LOGIN));
		nickname = nick;
	}

	public String getNickname() {
		return nickname;
	}

	public void setSessionKey(String sessionKey) {
		assert (operation.equals(DirMessageOps.OPERATION_LOGIN_OK));
		this.sessionKey = sessionKey;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public String getUsers() {
		return users;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		assert (operation.equals(DirMessageOps.OPERATION_PUBLISH)
				|| operation.equals(DirMessageOps.OPERATION_REGISTER_SERVER_PORT)
				|| operation.equals(DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT));
		this.port = port;
	}

	public void setUsers(String users) {
		assert (operation.equals(DirMessageOps.OPERATION_USER_LIST_OK));
		this.users = users;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		assert (operation.equals(DirMessageOps.OPERATION_DOWNLOADFROM_OK));
		this.address = address;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		assert (operation.equals(DirMessageOps.OPERATION_FILE_LIST_OK)
				|| operation.equals(DirMessageOps.OPERATION_DOWNLOADFROM_OK)
				|| operation.equals(DirMessageOps.OPERATION_PUBLISH));
		this.fileHash = fileHash;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		assert (operation.equals(DirMessageOps.OPERATION_PUBLISH));
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		assert (operation.equals(DirMessageOps.OPERATION_PUBLISH));
		this.filePath = filePath;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		assert (operation.equals(DirMessageOps.OPERATION_PUBLISH));
		this.fileSize = fileSize;
	}

	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		assert (operation.equals(DirMessageOps.OPERATION_FILE_LIST_OK));
		this.files = files;
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		String[] lines = message.split(END_LINE + "");
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim(); // Extrae el valor operación, nickname...

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null); // EVITAR 2 OPERATION REPETIDOS
				m = new DirMessage(value);
				break;
			}

			case FIELDNAME_NICKNAME: {
				assert (m != null);
				m.setNickname(value);
				break;
			}

			case FIELDNAME_SESSION_KEY: {
				assert (m != null);
				m.setSessionKey(value);
				break;
			}

			case FIELDNAME_USER_LIST: {
				assert (m != null);
				m.setUsers(value);
				break;
			}

			case FIELDNAME_PORT: {
				assert (m != null);
				m.setPort(value);
				break;
			}

			case FIELDNAME_ADDRESS: {
				assert (m != null);
				m.setAddress(value);
				break;
			}

			case FIELDNAME_FILE_NAME: {
				assert (m != null);
				m.setFileName(value);
				break;
			}

			case FIELDNAME_FILE_SIZE: {
				assert (m != null);
				m.setFileSize(value);
				break;
			}

			case FIELDNAME_FILE_HASH: {
				assert (m != null);
				m.setFileHash(value);
				break;
			}

			case FIELDNAME_FILE_PATH: {
				assert (m != null);
				m.setFilePath(value);
				break;
			}

			case FIELDNAME_FILES: {
				assert (m != null);
				m.setFiles(value);
				break;
			}

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN:
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		case DirMessageOps.OPERATION_LOGIN_OK:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_LOGOUT:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_LOGOUT_OK:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_USER_LIST:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_USER_LIST_OK:
			sb.append(FIELDNAME_USER_LIST + DELIMITER + users + END_LINE);
			break;
		case DirMessageOps.OPERATION_PUBLISH:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			sb.append(FIELDNAME_FILE_NAME + DELIMITER + fileName + END_LINE);
			sb.append(FIELDNAME_FILE_SIZE + DELIMITER + fileSize + END_LINE);
			sb.append(FIELDNAME_FILE_HASH + DELIMITER + fileHash + END_LINE);
			sb.append(FIELDNAME_FILE_PATH + DELIMITER + filePath + END_LINE);
			break;
		case DirMessageOps.OPERATION_REGISTER_SERVER_PORT:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		case DirMessageOps.OPERATION_REGISTER_SERVER_PORT_OK:
			break;
		case DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_UNREGISTER_SERVER_PORT_OK:
			break;
		case DirMessageOps.OPERATION_DOWNLOADFROM:
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		case DirMessageOps.OPERATION_DOWNLOADFROM_OK:
			sb.append(FIELDNAME_ADDRESS + DELIMITER + address + END_LINE);
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		case DirMessageOps.OPERATION_FILE_LIST:
			sb.append(FIELDNAME_SESSION_KEY + DELIMITER + sessionKey + END_LINE);
			break;
		case DirMessageOps.OPERATION_FILE_LIST_OK:
			sb.append(FIELDNAME_FILES + DELIMITER + files + END_LINE);
			break;
		case DirMessageOps.OPERATION_FILE_LIST_FAILED:
			break;

		default:
			break;
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
