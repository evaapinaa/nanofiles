package es.um.redes.nanoFiles.udp.message;

import es.um.redes.nanoFiles.udp.client.DirectoryConnector;

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

	//Nombres de todos los campos que pueden aparecen en los mensajes de este protocolo
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_FILENAMES = "filenames";
	private static final String FIELDNAME_FILEHASHS = "filehashs";
	private static final String FIELDNAME_FILESIZES = "filesizes";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_SERVERADDR = "server_address";

	

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	private String nickname = null;
	private int sessionKey = DirectoryConnector.INVALID_SESSION_KEY;
	private String filenames = null;
	private String filehashs = null;
	private String filesizes = null;
	private int port = 0;
	private String serverAddr = null;


	public DirMessage(String op) {
		this.operation = op;
	}
	
	//Utilizado para crear mensajes que envia el directorio
	public DirMessage(String op, String campo1) {
		this.operation = op;
		//Este constructor puede ser usado tanto para devolver una lista de usuarios o la direccion de un servidor
		setNickname(campo1);
		setServerAddr(campo1);
	}
	
	public DirMessage (String op, int sessionKey) {
		this(op);
		setSessionKey(sessionKey);
	}
	
	public DirMessage (String op, String nickname, int sessionKey) {
		this(op, sessionKey);
		setNickname(nickname);
	}
	
	//Utilizado para crear mensajes que envia el directorio
	public DirMessage (String op, int sessionKey, int port) {
		this(op,sessionKey);
		setPort(port);
	}
	
	public DirMessage (String op, String servers, int sessionKey, String filehashes, String filenames, String filesizes) {
		this(op, servers, sessionKey);
		setFilehashs(filehashes);
		setFilenames(filenames);
		setFilesizes(filesizes);
	}
	
	public DirMessage (String op, int sessionKey, String filehashes, String filenames, String filesizes) {
		this(op, null, sessionKey, filehashes, filenames, filesizes);
	}


	public DirMessage (String op, int sessionKey, String filehashes) {
		this(op,null,sessionKey, filehashes, null, null);
	}
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	private void setNickname(String nick) {

		//Revisar operaciones que requieren del nickname
		if (this.operation.equals(DirMessageOps.OPERATION_LOGIN) || this.operation.equals(DirMessageOps.GET_USERLIST_OK) || this.operation.equals(DirMessageOps.OPERATION_LOGOUT) || this.operation.equals(DirMessageOps.SEARCH_OK) || this.operation.equals(DirMessageOps.OPERATION_GETSERVERADDR) || this.operation.equals(DirMessageOps.FILELIST_OK)) nickname = nick;
		
	}

	public String getNickname() {
		return nickname;
	}
	
	 private void setSessionKey(int s) {		
		if (this.operation.equals(DirMessageOps.LOGIN_OK ) || this.operation.equals(DirMessageOps.OPERATION_LOGOUT) || this.operation.equals(DirMessageOps.OPERATION_GET_USERLIST) || this.operation.equals(DirMessageOps.OPERATION_FILELIST) || this.operation.equals(DirMessageOps.OPERATION_REGISTER_SERVER) || this.operation.equals(DirMessageOps.OPERATION_UNREGISTER_SERVER) || this.operation.equals(DirMessageOps.OPERATION_PUBLISH) || this.operation.equals(DirMessageOps.OPERATION_SEARCH) || this.operation.equals(DirMessageOps.OPERATION_GETSERVERADDR) ) this.sessionKey = s;

}
	
	public int getSessionKey() {
		return this.sessionKey;
	}
	
	public String getFilenames() {
		return this.filenames;
	}
	
	public String getFilesizes() {
		return this.filesizes;
	}
	
	public String getFilehashs() {
		return this.filehashs;
	}
	
	
	private void setFilehashs(String filehashs) {
		if (this.operation.equals(DirMessageOps.FILELIST_OK) || this.operation.equals(DirMessageOps.OPERATION_SEARCH) || this.operation.equals(DirMessageOps.OPERATION_PUBLISH)) this.filehashs = filehashs; 
	}
	
	private void setFilesizes(String filesizes) {
		if (this.operation.equals(DirMessageOps.FILELIST_OK) || this.operation.equals(DirMessageOps.OPERATION_PUBLISH)) this.filesizes = filesizes;
	}
	
	private void setFilenames(String filenames) {
		if (this.operation.equals(DirMessageOps.FILELIST_OK) || this.operation.equals(DirMessageOps.OPERATION_PUBLISH)) this.filenames = filenames;
	}
	
	private void setPort(int p) {
		if (this.operation.equals(DirMessageOps.OPERATION_REGISTER_SERVER)) this.port = p;
	}
	
	public int getPort() {
		return this.port;
	}
	
	private void setServerAddr(String s) {
		if (this.operation.equals(DirMessageOps.GETSERVERADDR_OK)) this.serverAddr = s; 
	}
	
	public String getServerAddr() {
		return this.serverAddr;
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
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;



		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			
			
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				 
					m = new DirMessage(value);
					
				break;
			}
			
			case FIELDNAME_NICKNAME: {
				
				m.setNickname(value);
				break;
			}

			case FIELDNAME_SESSIONKEY: { 
				assert (m != null);
				m.setSessionKey(Integer.parseInt(value));
				break;
			}
						
			
			case FIELDNAME_FILENAMES: {
				
				m.setFilenames(value);
				break;
			}
			
			case FIELDNAME_FILEHASHS: {
				m.setFilehashs(value);
				break;
			}
			
			case FIELDNAME_FILESIZES: {
				m.setFilesizes(value);
				break;
			}
			
			case FIELDNAME_PORT:
				m.setPort(Integer.parseInt(value));
				break;
				
				
			case FIELDNAME_SERVERADDR:
				
				m.setServerAddr(value);
				break;
				
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
		if (nickname != null ) sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE); // Construimos el campo
		if (sessionKey != DirectoryConnector.INVALID_SESSION_KEY) sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE); // Construimos el campo
		if (filenames != null) sb.append(FIELDNAME_FILENAMES + DELIMITER + filenames + END_LINE);
		if (filehashs != null) sb.append(FIELDNAME_FILEHASHS + DELIMITER + filehashs + END_LINE);
		if (filesizes != null) sb.append(FIELDNAME_FILESIZES + DELIMITER + filesizes + END_LINE);
		if (port != 0) sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
		if (serverAddr != null) sb.append(FIELDNAME_SERVERADDR + DELIMITER + serverAddr + END_LINE);

		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
