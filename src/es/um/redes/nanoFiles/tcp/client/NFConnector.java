package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataOutputStream dos;
	private DataInputStream dis;


	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		//Socket se crea a partir de la direccion del servidor
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		System.out.println("Connected to " + serverAddr.toString());
		//Se crean los DataInputStream/DataOutPutStream a partir de los streams
		//de entrada del socket creado.
		//dos para enviar, dis para recibir datos del servidor
		 dos = new DataOutputStream(socket.getOutputStream());
		 dis = new DataInputStream(socket.getInputStream());
	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		
		//Creacion de un mensaje binario DownloadFrom
			System.out.println("Starting download");
			PeerMessage msg = new PeerMessage (PeerMessageOps.OPCODE_DOWNLOADFROM);
			msg.setFileHash(targetFileHashSubstr);
			msg.writeMessageToOutputStream(this.dos);
		
		//Lee a traves de dis del socket y dependiendo del tipo del mensaje
		//actua de una forma u otra
		 
		PeerMessage rcv = PeerMessage.readMessageFromInputStream(dis);
		switch (rcv.getOpcode()) {
		/*Si el tipo del mensaje recibido es File, la descarga se ha realizado
		 * correctamente, escribiendose ahora el contenido del fichero en el 
		 * nuevo fichero con el nombre especificado por el usuario
		 */
		case PeerMessageOps.OPCODE_FILE:
		
			String filehash = rcv.getFileHash();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(rcv.getDownloadedFile());
			fos.close();	
			/*Comprobacion de que el contenido del nuevo fichero es identico al
			 * contenido del fichero 
			 */
			
			if (FileDigest.computeFileChecksumString(file.getAbsolutePath()).equals(filehash)) {
				downloaded = true;
				System.out.println("Succesfully downloaded remote file to " + file.getAbsolutePath());
				System.out.println("File '"+ file.getName() + "' downloaded succesfully.");
			
				}
			break;
		/*Si el tipo del mensaje recibido es File Not Found, el hash pasado por el usuario no
		 * identifica a ningun fichero*/
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			System.out.println("ERROR: File not found.");
			file.delete();
			break;
			
			
		/*Si el subhash pasado identifica a mas de un fichero, el subhash es ambiguo*/
		case PeerMessageOps.OPCODE_FILEHASH_AMBIGOUS:
			System.out.println("ERROR: Subhash '" + rcv.getFileHash() + "' recognizes multiple files");
			file.delete();
			break;
		}
	 
		return downloaded;
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}
	
	public boolean downloadFileChunk(String targetFileHashSubstr, File file, int n, int nServers) throws IOException {
		boolean success = false;
		System.out.println("Starting download");
		//Creacion del mensaje download
		PeerMessage msg = new PeerMessage (PeerMessageOps.OPCODE_DOWNLOAD);
		msg.setFileHash(targetFileHashSubstr);
		msg.setNumberOfServersThatHaveFile((byte) nServers);
		msg.setIdentifierServer((byte) n);
		//Envio del mensaje
		msg.writeMessageToOutputStream(dos);
		//Recepcion del mensaje de confirmacion
		PeerMessage rcv = PeerMessage.readMessageFromInputStream(dis);
		switch (rcv.getOpcode()) {
		
		//La descarga se ha realizado correctamente 
		case PeerMessageOps.OPCODE_FILE:
			success = true;
			String filehash = rcv.getFileHash();
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(rcv.getDownloadedFile());
			fos.close();	
			if (n==nServers) {
				success = FileDigest.computeFileChecksumString(file.getAbsolutePath()).equals(filehash);
				if (success) {
					System.out.println("Succesfully downloaded remote file to " + file.getAbsolutePath());
					System.out.println("File '"+ file.getName() + "' downloaded succesfully.");
				}
			
			}
			break;
			
			
		//File hash pasado no identifica a ningun fichero del servidor de ficheros
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			System.out.println("ERROR: Especified File not found.");
			file.delete();
			break;
		
			
		//File hash pasado identifica a mas de un fichero del servidor de ficheros
		case PeerMessageOps.OPCODE_FILEHASH_AMBIGOUS:
			System.out.println("ERROR: Subhash '" + rcv.getFileHash() + "' recognizes multiple files");
			file.delete();
			break;
		
		
		
		}
		
		
		return success;
	}
	
	

}
