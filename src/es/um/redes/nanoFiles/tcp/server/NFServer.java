package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {
	
	private Random random;
	private static int PORT = 10000;
	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private InetSocketAddress serverAddress;
	
	
	public NFServer() throws IOException {
		this.random = new Random();
		PORT = 10000+random.nextInt(55535);
		this.serverAddress = new InetSocketAddress(PORT);
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind(serverAddress);

	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		Socket socket;
		//Se encuentra esperando a establecer conexión con un peer
		while(!stopServer) {
		try {
			socket = serverSocket.accept();
			System.out.println("\n* New Client connected from " + socket.getLocalSocketAddress());
			NFServerThread hilo = new NFServerThread(socket);
			hilo.start();
			
		
		
		} catch (IOException e) {
			if ( !(e instanceof SocketException) ) System.out.println("Error trying to establish a connection with server");
		}
		}
		
		
	}

		//backgroundServeFiles trata en un try-catch que ocurra una excepción IOException, por lo que para evitar repetir código
		//Este metodo no trata la excepción, lo lanza y espera que lo trate la función anteriormente mencionada.
		public void startServer() throws IOException{
			if (stopServer) { stopServer=false; serverSocket.bind(serverAddress);}
			new Thread(this).start();
		}
		
		public int getServerPort() {
			return this.serverSocket.getLocalPort();
		}

		public SocketAddress getServerAddress() {
			return this.serverSocket.getLocalSocketAddress();
		}
		
		//Aqui, close da una excepción, pero no podemos ignorarla debido a que ocurre dentro de la expresión lambda definida
		public void stopServer() {
			Thread stopSrv = new Thread(() -> {				
				try {
					this.serverSocket.close();
					stopServer = true;
					
				} catch (IOException e) {System.out.println("Error trying to close ServerSocket");}
	
			});
			stopSrv.start();
		}
		
		
}
