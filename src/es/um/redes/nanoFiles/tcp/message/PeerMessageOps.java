package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;

	public static final byte OPCODE_DOWNLOAD = 1;
	public static final byte OPCODE_DOWNLOAD_RESPONSE = 2;
	public static final byte OPCODE_FILE_NOT_FOUND = 3;
	public static final byte OPCODE_AMBIGUOUS_HASH = 4;
	
	public static final byte OPCODE_PUBLISH = 5;
	public static final byte OPCODE_PUBLISH_RESPONSE = 6;
	public static final byte OPCODE_PUBLISH_ERROR = 7;
	
	public static final byte OPCODE_FILELIST = 8;
	public static final byte OPCODE_FILELIST_RESPONSE = 9;
	public static final byte OPCODE_FILELIST_ERROR = 10;
	
	public static final byte OPCODE_SEARCH = 11;
	public static final byte OPCODE_SEARCH_RESPONSE = 12;
	
	public static final byte OPCODE_DOWNLOAD_CHUNK = 13;
	public static final byte OPCODE_DOWNLOAD_CHUNK_RESPONSE = 14;

	/**
	 * TODO: Definir constantes con nuevos opcodes de mensajes
	 * definidos, añadirlos al array "valid_opcodes" y añadir su
	 * representación textual a "valid_operations_str" en el mismo orden
	 */
	private static final Byte[] _valid_opcodes = {
			OPCODE_INVALID_CODE, OPCODE_DOWNLOAD, OPCODE_DOWNLOAD_RESPONSE, OPCODE_FILE_NOT_FOUND, OPCODE_AMBIGUOUS_HASH, 
			OPCODE_PUBLISH, OPCODE_PUBLISH_RESPONSE,OPCODE_PUBLISH_ERROR, 
			OPCODE_FILELIST, OPCODE_FILELIST_RESPONSE, OPCODE_FILELIST_ERROR,
			OPCODE_SEARCH, OPCODE_SEARCH_RESPONSE
			

			};
	
	private static final String[] _valid_operations_str = {
			"INVALID_OPCODE", "DOWNLOAD_REQUEST", "DOWNLOAD_RESPONSE", "FILE_NOT_FOUND", "AMBIGUOUS_HASH",
			"PUBLISH_REQUEST", "PUBLISH_RESPONSE", "PUBLISH_ERROR",
			"FILELIST_REQUEST", "FILELIST_RESPONSE", "FILELIST_ERROR",
			"SEARCH_REQUEST", "SEARCH_RESPONSE"


			};

	

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
