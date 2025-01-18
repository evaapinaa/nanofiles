package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes del protocolo de comunicación con el directorio.
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_LOGIN_OK = "loginOk";
	public static final String OPERATION_LOGIN_FAILED = "loginFailed";

	
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_LOGOUT_OK = "logoutOk";
	public static final String OPERATION_LOGOUT_FAILED = "logoutFailed";
	
	public static final String OPERATION_USER_LIST = "userlist";
	public static final String OPERATION_USER_LIST_OK = "userlistOk";
	public static final String OPERATION_USER_LIST_FAILED = "userlistFailed";
	
	public static final String OPERATION_PUBLISH = "publish";
	public static final String OPERATION_PUBLISH_OK = "publishOk";
	public static final String OPERATION_PUBLISH_FAILED = "publishFailed";
	
	public static final String OPERATION_REGISTER_SERVER_PORT = "registerServerPort";
	public static final String OPERATION_REGISTER_SERVER_PORT_OK = "registerServerPortOk";
	public static final String OPERATION_REGISTER_SERVER_PORT_FAILED = "registerServerPortFailed";
	
    public static final String OPERATION_UNREGISTER_SERVER_PORT = "unregisterServerPort";
    public static final String OPERATION_UNREGISTER_SERVER_PORT_OK = "unregisterServerPortOk";
    public static final String OPERATION_UNREGISTER_SERVER_PORT_FAILED = "unregisterServerPortFailed";

	public static final String OPERATION_FILE_LIST = "filelist";
	public static final String OPERATION_FILE_LIST_OK = "filelistOk";
	public static final String OPERATION_FILE_LIST_FAILED = "filelistFailed";
	
	public static final String OPERATION_DOWNLOADFROM = "downloadfrom";
	public static final String OPERATION_DOWNLOADFROM_OK = "downloadfromOk";
	public static final String OPERATION_DOWNLOADFROM_FAILED = "downloadfromFailed";
	

}
