package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PeerMessage {




	private byte opcode;
	private byte hashLength;
	private String fileHash = null;
	private byte[] downloadedFile = null;
	private long downloadedFileLength;
	private byte numberOfServersThatHaveFile;
	private byte identifierServer; 
	
	
	
	//Constructores
	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	public PeerMessage(byte op, byte lengthHash, String hashValue) {
		opcode = op;
		hashLength = lengthHash;
		fileHash = hashValue;
	}
	
	//Metodos getter y setter
	public byte getOpcode() {
		return opcode;
	}

	public byte getHashLength() {
		return hashLength;
	}

	public String getFileHash() {
		return fileHash;
	}
	
	public void setFileHash(String fileHash) {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNLOADFROM || this.opcode == PeerMessageOps.OPCODE_FILE || this.opcode == PeerMessageOps.OPCODE_DOWNLOAD) {this.fileHash = fileHash; this.hashLength = (byte)fileHash.length();}
	}	
	

	public void setDownloadedFile(byte[] f) {
		if (this.opcode == PeerMessageOps.OPCODE_FILE) this.downloadedFile = f;
	}
	
	public void setDownloadedFileLength(long l) {
		if (this.opcode == PeerMessageOps.OPCODE_FILE) this.downloadedFileLength = l;
	}
	
	public byte[] getDownloadedFile() {
		byte[] aux = new byte[(int) this.downloadedFileLength];
		System.arraycopy(this.downloadedFile, 0, aux, 0, (int) downloadedFileLength);
		return aux;
	}
	
	public long getDownloadedFileLength() {
		return this.downloadedFileLength;
	}
	

	public void setNumberOfServersThatHaveFile(byte n) {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNLOAD || this.opcode == PeerMessageOps.OPCODE_FILE) this.numberOfServersThatHaveFile = n;
	}
	
	public int getNumberOfServersThatHaveFile() {
		return this.numberOfServersThatHaveFile;
	}
	
	public void setIdentifierServer(byte n) {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNLOAD || this.opcode == PeerMessageOps.OPCODE_FILE)this.identifierServer = n;
	}
	
	public byte getIdentifierServer() {
		return this.identifierServer;
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
		byte hashLength;
		switch (opcode) {
		
		case PeerMessageOps.OPCODE_DOWNLOADFROM :
			
			hashLength = dis.readByte();
			byte[] fileHash = new byte[hashLength];
			dis.readFully(fileHash);
			String strFileHash = new String(fileHash, 0 , fileHash.length);
			message.setFileHash(strFileHash);
			break;
		
		case PeerMessageOps.OPCODE_FILE:
			hashLength = dis.readByte();
			fileHash = new byte[hashLength];
			dis.readFully(fileHash);
			strFileHash = new String(fileHash, 0, fileHash.length);
			message.setFileHash(strFileHash);
			long downloadedFileLength = dis.readLong();
			byte[] downloadedFile = new byte[(int) downloadedFileLength];
			dis.readFully(downloadedFile);
			message.setDownloadedFileLength((int) downloadedFileLength);
			message.setDownloadedFile(downloadedFile);
			break;


		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
			
		case PeerMessageOps.OPCODE_FILEHASH_AMBIGOUS:
			break;
			
		case PeerMessageOps.OPCODE_DOWNLOAD:
			hashLength = dis.readByte();
			fileHash = new byte[hashLength];
			dis.readFully(fileHash);
			strFileHash = new String(fileHash, 0 , fileHash.length);
			message.setFileHash(strFileHash);
			byte id = dis.readByte();
			message.setIdentifierServer(id);
			byte n = dis.readByte();
			message.setNumberOfServersThatHaveFile(n);
			break;
			
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

		case PeerMessageOps.OPCODE_DOWNLOADFROM :
			assert (hashLength > 0 && fileHash.length() == hashLength);
			dos.writeByte(hashLength);
			byte[] hashValue = fileHash.getBytes();
			dos.write(hashValue);
			break;
				
		
		case PeerMessageOps.OPCODE_FILE:
			assert (hashLength > 0 && fileHash.length() == hashLength);
			dos.writeByte(hashLength);
			dos.write(fileHash.getBytes());
			dos.writeLong(downloadedFileLength);
			dos.write(downloadedFile);
			break;

		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
			
		case PeerMessageOps.OPCODE_FILEHASH_AMBIGOUS:
			break;
			
			
		case PeerMessageOps.OPCODE_DOWNLOAD:
			assert (hashLength > 0 && fileHash.length() == hashLength);
			dos.writeByte(hashLength);
			hashValue = fileHash.getBytes();
			dos.write(hashValue);
			dos.writeByte(identifierServer);
			dos.writeByte(numberOfServersThatHaveFile);
			break;

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		
	}
	




}
