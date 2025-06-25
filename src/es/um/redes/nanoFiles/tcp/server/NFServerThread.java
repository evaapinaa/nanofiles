package es.um.redes.nanoFiles.tcp.server;

import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;

public class NFServerThread extends Thread {
	
	//Atributos
	private Socket socket;
	
	//Constructor de la clase
	public NFServerThread(Socket new_socket) {
		this.socket = new_socket;
	}
	
	//Metodo run defenido para lanzar el hilo
	@Override
	public void run() {
		NFServerComm.serveFilesToClient(socket);
		 System.out.println("* Disconnected client from " + socket.getLocalSocketAddress());
		 System.out.print("(nanoFiles@" + NanoFiles.sharedDirname + ") ");
	}
	
}
