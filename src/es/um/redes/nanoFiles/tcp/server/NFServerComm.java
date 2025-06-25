package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		DataInputStream dis = null;
		DataOutputStream dos = null;

		/*
		 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
		 */
		/*
		 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo.
		 */

		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			PeerMessage request = PeerMessage.readMessageFromInputStream(dis);

			if (request.getOpcode() == PeerMessageOps.OPCODE_DOWNLOAD) {
				FileInfo[] matchingFiles = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), request.getFileHash());

				if (matchingFiles.length == 0) {
					new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND).writeMessageToOutputStream(dos);
				} else if (matchingFiles.length > 1) {
					new PeerMessage(PeerMessageOps.OPCODE_AMBIGUOUS_HASH).writeMessageToOutputStream(dos);
				} else {
					FileInfo fileInfo = matchingFiles[0];
					byte[] fileData = new byte[(int) fileInfo.fileSize];
					File file = new File(fileInfo.filePath);

					if (!file.exists()) {
						new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND).writeMessageToOutputStream(dos);
					} else {
						try {
							FileInputStream fis = new FileInputStream(file);
							fis.read(fileData);
							fis.close();
							new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE, (byte) fileInfo.fileHash.length(),
									fileInfo.fileHash, (long) file.length(), fileData).writeMessageToOutputStream(dos);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} catch (EOFException e) {
			System.out.println("Client disconnected.");
		} catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
