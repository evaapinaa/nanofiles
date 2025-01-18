package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private static final int PORT_RANGE = 100; // Intentará desde 10000 hasta 10099
	private ServerSocket serverSocket = null;

	public NFServerSimple() throws IOException {
		/*
		 * TODO: Crear una direción de socket a partir del puerto especificado
		 */
		InetSocketAddress serverAddress = new InetSocketAddress(PORT);

		/*
		 * TODO: Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(serverAddress);
			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);

		} catch (IOException e) {
			for (int i = 1; i <= PORT_RANGE; i++) {
				try {
					serverSocket = new ServerSocket(PORT + i);
					serverSocket.setReuseAddress(true);
					serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
					break;
				} catch (IOException e2) {
					if (i == PORT_RANGE) {
						System.err.println("Error: Failed to bind the server socket to any port.");
						break;
					}
				}
			}

		}
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
		/*
		 * TODO: Comprobar que el socket servidor está creado y ligado
		 */
		if (serverSocket == null) {
			System.err.println("Error: Failed to run the file server");
			return;
		} else {
			System.out.println("NFServerSimple server running on " + serverSocket.getLocalSocketAddress() + ".");
			System.out.println("Type '" + STOP_SERVER_COMMAND + "' to stop the server.");
		}
		/*
		 * TODO: Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		/*
		 * TODO: Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		boolean stopServer = false;
		Socket socket = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (!stopServer) {
				try {
					socket = serverSocket.accept();
					if (socket != null) {
						NFServerComm.serveFilesToClient(socket);
					}
				} catch (SocketTimeoutException e) {
				} catch (IOException e) {
					if (stopServer) {
						System.out.println("Stopping server...");
						break;
					}
					System.err.println("Error: Problem accepting a connection.");
				}

				// Comprobar si se ha introducido el comando para detener el servidor
				if (reader.ready()) {
					String command = reader.readLine();
					if (command.equals(STOP_SERVER_COMMAND)) {
						stopServer = true;
						break;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error: Problem reading the command.");
		} finally {
			// Liberar el puerto
			if (serverSocket != null && !serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.err.println("Error closing server socket: " + e.getMessage());
				}
			}
		}

		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");

	}

}
