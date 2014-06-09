import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;

public class Server {

	private static int port = 8989; 

	public static void main (String[] args) throws IOException
	{
	    ServerSocket server = null;

	    try {
	        // empieza conexión con el puerto seleccionado
	        server = new ServerSocket(port);
	    } catch (IOException e) {

			System.err.println("No se pudo conectar al puerto " + port);
			System.err.println(e);
			System.exit(1);
		}

		Socket client = null;

		while(true) {
		
			try {
				client = server.accept();
			} 
		
			catch (IOException e) {

				System.err.println("Ha fallado la aceptación del servidor.");
				System.err.println(e);
				System.exit(1);
			}

			// Comienza un thread para poder manejar a este cliente aceptado por el servidor
			Thread t = new Thread(new ClientConn(client));
			t.start();
		}
	}
}


class ChatServerProtocol {

	private String nick;
	private ClientConn conn;

	// se hace una tabla de hash para los nicks.
	private static Hashtable<String, ClientConn> nicks = new Hashtable<String, ClientConn>();

	private static final String msg_OK = "OK";
	private static final String msg_NICK_IN_USE = "ERROR: Nick en uso.";
	private static final String msg_SPECIFY_NICK = "ERROR: Especifica el parámetro del mensaje. Debe empezar con NICK. Ej. NICK mensaje ";
	private static final String msg_INVALID = "ERROR: Comando inválido.";
	private static final String msg_SEND_FAILED = "ERROR: Fallo al enviar.";

	// añadimos un nuevo nick a la tabla de hash mencionada; si existe, tira false, sino lo guardamos.
	private static boolean add_nick(String nick, ClientConn c) {

		if (nicks.containsKey(nick)) {
		
			return false;
		
		} else {
		
			nicks.put(nick, c);
			return true;
		
		}
	}

	public ChatServerProtocol(ClientConn c) {
		
		nick = null;
		conn = c;
	}

	private void log(String msg) {
		
		System.err.println(msg);
	}
	public boolean isAuthenticated() {
		
		return ! (nick == null);
	}
	
	//Protocolo de autentificación. Chequea que el mensaje empiece con 'nick' y que el nick no esté en uso.
	//Si todo sale OK, retorna OK. Si el nick existe o no se empieza con NICK el mensaje, advierte el error.

	private String authenticate(String msg) {

		System.out.println("El mensaje es: "+ msg);
		
		if(msg.startsWith("NICK")) {
			
			String tryNick = msg.substring(5);
			if(add_nick(tryNick, this.conn)) {
				
				log("Usuario de nick  " + tryNick + " ha ingresado.");
				this.nick = tryNick;
				return msg_OK;
			
			} else {
				return msg_NICK_IN_USE;
			}
		
		} else {
			return msg_SPECIFY_NICK;
		}
	}
	
	// Se envía un mensaje a otro usuario; recepient contiene el nick del receptor y msg el mensaje a enviar.
	// Retorna true si existe el destinatario.

	private boolean sendMsg(String recipient, String msg) {
		
		if (nicks.containsKey(recipient)) {
	
			ClientConn c = nicks.get(recipient);
			c.sendMsg(nick + ": " + msg);
			return true;
		
		} else {
		
			return false;
		
		}
	}
	
	// Proceso del mensaje proveniente por el cliente
	
	public String process(String msg) {
		
		if (!isAuthenticated()) return authenticate(msg);
		
		String[] msg_parts = msg.split(" ", 3);
		String msg_type = msg_parts[0];
	
		if(msg_type.equals("MSG")) {
			
			if(msg_parts.length < 3) return msg_INVALID;
			
			if(sendMsg(msg_parts[1], msg_parts[2])) return msg_OK;
			else return msg_SEND_FAILED;
		
		} else {
			
			return msg_INVALID;
		
		}
	}
}

class ClientConn implements Runnable {

	private Socket client;
	private BufferedReader in = null;
	private PrintWriter out = null;

	ClientConn(Socket client) {

		this.client = client;

		try {
			//obtiene el imput de este cliente
			in = new BufferedReader(new InputStreamReader(
			client.getInputStream()));
			// y el output del mismo cliente
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println(e);
			return;
		}
	}

	public void run() {
		
		String msg, response;
		ChatServerProtocol protocol = new ChatServerProtocol(this);
		
		try {

			//bucle que lee  y procesa las lineas escritas por el cliente bajo el protocolo hecho,
			//enviando la respuesta de vuelta al mismo cliente.

			while ((msg = in.readLine()) != null) {
			response = protocol.process(msg);
			out.println("SERVER: " + response);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public void sendMsg(String msg) {
		
		out.println(msg);
	
	}
}