package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static int PORT = 10000;
	private ServerSocket serverSocket = null;
	private volatile boolean stopServer = false;
	private boolean cond = false;
	private Random random;
	public NFServerSimple() throws IOException {
		this.random = new Random();
		while (!cond) {
			try {
			InetSocketAddress InetSocketServerAddr = new InetSocketAddress(PORT);
			this.serverSocket = new ServerSocket();
			this.serverSocket.bind(InetSocketServerAddr);
            this.serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
			cond = true;
			}catch(IOException e) {
				if (e instanceof BindException) {
					PORT=10000+random.nextInt(55535);
				} 
				else {
					cond=true;
					serverSocket = null;
				
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
	public void run() throws IOException {
		if (serverSocket == null) {
            System.err.println("*Error: Failed to run file server*");
            return;
        } else {
            System.out.println("NFServerSimple server running on " + serverSocket.getLocalSocketAddress());
        }

        System.out.println("Enter 'fgstop' to stop the server");
        Socket socket = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!this.stopServer) {
                try {
                    socket = serverSocket.accept();
                    
                    if (socket != null) {
                    System.out.println("* New Client connected from " + socket.getLocalSocketAddress());
                    NFServerComm.serveFilesToClient(socket);
                    System.out.println("* Disconnected client from " + socket.getLocalSocketAddress());
           		 	}
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    if (!stopServer) {
                        System.err.println("*Error: Server not accepting a connection*");
	                    break;

                    } 
                }

                if (reader.ready()) {
                    String stop = reader.readLine();
                    if (stop.equals(STOP_SERVER_COMMAND)) {
	                    System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
                        this.stopServer = true;
                    }
                }
            }
        } finally {
            try {
                serverSocket.close();
                socket = null;

            } catch (IOException e) {
                System.err.println("*Error closing fgserve*");
            }
        }

		
	}
}
	

