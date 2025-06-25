package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PeerMessage {

	private byte opcode;
	

	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */

	private byte hashLength = 0;
	private String fileHash = null;
	private long fileSize = 0;
	private byte[] fileData = null;
	

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	public PeerMessage(byte op, byte hashLength, String fileHash) {
		opcode = op;
		this.hashLength = hashLength;
		this.fileHash = fileHash;
	}
	
	public PeerMessage(byte op, byte hashLength, String fileHash, long fileSize, byte[] fileData) {
		opcode = op;
		this.hashLength = hashLength;
		this.fileHash = fileHash;
		this.fileSize = fileSize;
		this.fileData = fileData;
	}
	
	public PeerMessage(byte op, long fileSize, byte[] fileData) {
		opcode = op;
		this.fileSize = fileSize;
		this.fileData = fileData;
	}
	
	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		return opcode;
	}

	public String getFileHash() {
		if(fileHash == null) return null;
		return new String(fileHash);
	}
	
	public void setHashLength(byte hashLength) {
		this.hashLength = hashLength;
	}
	
	public byte getHashLength() {
		return hashLength;
	}
	
	public void setFileHash(String fileHash) {
		assert(opcode == PeerMessageOps.OPCODE_DOWNLOAD || opcode == PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE );
		this.fileHash = fileHash;
	}
	
	public byte[] getFileData() {
		return fileData;
	}
	
	public void setFileData(byte[] fileData) {
		assert(opcode == PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE);
		this.fileData = fileData;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(long fileSize) {
		assert(opcode == PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE);
		this.fileSize = fileSize;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		byte opcode = dis.readByte();
		PeerMessage message = new PeerMessage(opcode);
		switch (opcode) {
		
		case PeerMessageOps.OPCODE_DOWNLOAD: {
			byte hashLength = dis.readByte(); // Leer la longitud del hash
			byte[] hashBytes = new byte[hashLength];// Preparar un array para el hash
            dis.readFully(hashBytes); // Garantiza la lectura completa del hash
            String fileHash = new String(hashBytes); // Convierte el hash a String
            
            // Construye el mensaje con los datos leídos
            message = new PeerMessage(opcode, hashLength, fileHash);
            break;
		}
		
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE: {
			byte hashLength = dis.readByte(); 
		    byte[] hashBytes = new byte[hashLength]; 
		    dis.readFully(hashBytes);
		    String fileHash = new String(hashBytes); 
		    long fileSize = dis.readLong(); // Leer la longitud del fichero
		    byte[] fileData = new byte[(int) fileSize]; // Crear un array para los datos del fichero
		    dis.readFully(fileData);
		    message = new PeerMessage(opcode, hashLength, fileHash, fileSize, fileData);
		    break;
		}
		
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
		    break;
		}
		
		case PeerMessageOps.OPCODE_AMBIGUOUS_HASH: {
		    break;
		}

		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {

		case PeerMessageOps.OPCODE_DOWNLOAD: {
			assert(hashLength > 0 && fileHash.length() == hashLength);
			dos.write(hashLength);
			byte[] hashValue = fileHash.getBytes();
			if (hashValue.length != hashLength) {
		        throw new IOException("File hash length does not match the expected length.");
		    }

		    // Escribe el hash del fichero al flujo de salida
		    dos.write(hashValue);
		    break;
			
		}
		
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE: {
			assert(hashLength > 0 && fileHash.length() == hashLength);
			dos.write(hashLength);
			byte[] hashValue = fileHash.getBytes();
			if (hashValue.length != hashLength) {
		        throw new IOException("File hash length does not match the expected length.");
		    }
		    dos.write(hashValue);
		    dos.writeLong(fileSize);
		    if (fileData != null && fileData.length == fileSize) {
	            dos.write(fileData);
	        } 
		    else {
	            System.err.println("File data is null or has a different length than the expected file size.");
	        }
		    break;
		}
		
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
			System.err.println("File not found: " + PeerMessageOps.OPCODE_FILE_NOT_FOUND);
		}
		
		case PeerMessageOps.OPCODE_AMBIGUOUS_HASH: {
			System.err.println("File hash is ambiguous: " + PeerMessageOps.OPCODE_AMBIGUOUS_HASH);
		}

		
		

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
