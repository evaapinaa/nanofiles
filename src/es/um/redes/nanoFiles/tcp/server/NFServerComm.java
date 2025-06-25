package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		DataOutputStream dos = null;
		DataInputStream dis = null;
		PeerMessage msg = null;
		
		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			//Lectura de una solicitud enviada por el cliente
			msg = PeerMessage.readMessageFromInputStream(dis);
			
		} catch (IOException e) {
			System.out.println("Error trying to read socket.");
		}
		
		//Dependiendo del tipo del mensaje, realizará unas determinadas acciones u otras
	    byte opcode = msg.getOpcode();
	    if ((opcode == PeerMessageOps.OPCODE_DOWNLOADFROM) || (opcode == PeerMessageOps.OPCODE_DOWNLOAD)){
	    	String filehash = msg.getFileHash();
	    	FileInfo[] files = NanoFiles.db.getFiles();
	    	FileInfo[] commonFiles = FileInfo.lookupHashSubstring(files, filehash);
	    	
	    	//Caso en el que no haya ningun fichero con dicho subhash
	    	if (commonFiles.length == 0 ) {
	    		PeerMessage m = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
	    		try {
					m.writeMessageToOutputStream(dos);
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	//Caso en el que el subhash pasado sea ambiguo
	    	else if (commonFiles.length > 1) {
	    		PeerMessage m = new PeerMessage(PeerMessageOps.OPCODE_FILEHASH_AMBIGOUS);
	    		try {
					m.writeMessageToOutputStream(dos);
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	//Caso en el que se reconoce el fichero mediante su hash
	    	else {
	        		PeerMessage m = new PeerMessage(PeerMessageOps.OPCODE_FILE);
	    	
	    		if (opcode == PeerMessageOps.OPCODE_DOWNLOADFROM) {
			    	try {
			             File fileInputStream = new File(NanoFiles.db.lookupFilePath(commonFiles[0].fileHash));
			             DataInputStream dataInputStream = new DataInputStream(new FileInputStream(fileInputStream));
			             byte data[] = new byte[(int)fileInputStream.length()];
			             dataInputStream.readFully(data);
			             dataInputStream.close();
			             m.setFileHash(commonFiles[0].fileHash);
			             m.setDownloadedFile(data);
			             m.setDownloadedFileLength((fileInputStream.length()));
			             m.writeMessageToOutputStream(dos);
			             
					} catch (FileNotFoundException e) {
						System.out.println("ERROR: Specified file not found.");
					} catch (IOException e) {
						System.out.println("ERROR: Unexpected error trying to read/write a file");
					}
	    		}
	    		
	    		else { //Caso en el que el codigo de operacion es igual a DOWNLOAD
	    			
	    			//Inicializacion
	    			int idServer = msg.getIdentifierServer();
	    			int nServers = msg.getNumberOfServersThatHaveFile();
	    			//Calculo de la cantidad de bytes a leer
	    			int chunk = ((int) commonFiles[0].fileSize) / nServers;
	    			//Calculo de la posición de inicio según el identificador del servidor, usado como "turno"
	    			int pos = (idServer-1)*chunk;
	   
	    			//Creación del array de bytes. Para ello, comprobamos si lo que queda por leer es menor al chunk.
	    			byte[] bytes;
	    			if(idServer < nServers) {
						if (commonFiles[0].fileSize - (pos+chunk) < 0) bytes = new byte[(int) commonFiles[0].fileSize - pos]; 
						else bytes = new byte[chunk];
	    			} else {
	    				if (commonFiles[0].fileSize - (pos+chunk+1) < 0) bytes = new byte[(int) commonFiles[0].fileSize - pos]; 
						else bytes = new byte[chunk+1];
	    			}
	    			
				
	    			try {
	    				//Apertura del fichero
						RandomAccessFile archivo = new RandomAccessFile(commonFiles[0].filePath, "r");
						try {
							//Lectura del fichero
							archivo.seek(pos);
							archivo.readFully(bytes);
							archivo.close();
							
							//Preparamos el mensaje para su envio al cliente
							m.setDownloadedFile(bytes);
							m.setFileHash(commonFiles[0].fileHash);
							m.setIdentifierServer((byte) idServer);
							m.setNumberOfServersThatHaveFile((byte) nServers);
				            m.setDownloadedFileLength( bytes.length);
				            m.writeMessageToOutputStream(dos);
							
						} catch (IOException e) {
							System.out.println("Error trying to read " + commonFiles[0].filePath+commonFiles[0].fileName);
							return;
						}
						
					} catch (FileNotFoundException e) {
						return; 
					}
	    			
	    		}
	    		
	    }
	    }
	}




}
