package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;

public class NFServerThread extends Thread {
	/*
	 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServerComm.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */
	private Socket socket;

	public NFServerThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			NFServerComm.serveFilesToClient(socket);
		} catch (Exception e) {
			System.out.println("Error: the Internet connection has been lost. " + e.getMessage());
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				System.out.println("Error closing the socket: " + e.getMessage());
			}
		}
	}
}
