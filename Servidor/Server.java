import java.net.*;
import java.io.*;
import java.util.*;
 
public class Server {
	//Puerto en el que se van a conectar los clientes
    private static int serverPort = 6969;
 
    public static void main (String[] args) throws IOException {
 		//Se inicializa server de tipo ServerSocket, para inicializar el Socket
        ServerSocket server = null;
        try {
            server = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.err.println("No se puede conectar al puerto: " + serverPort);
            System.err.println(e);
            System.exit(1);
        }
 		//se inicializa la variable client de tipo Socket, para aceptar la conexion de los clientes.
        Socket client = null;
        while(true) {
            try {
            	//Se espera a los clientes para que se conecten
                client = server.accept();
            } catch (IOException e) {
                System.err.println("Error al conectar con el cliente.");
                System.err.println(e);
                System.exit(1);
            }
            //Se inicializa la variable t, para poder aceptar multiples clientes (Necesario par ael chat)
            Thread t = new Thread(new ClienteConnection(client));
            t.start();
        }
    }
}

//Se crean los protocolos pedidos, para el manejo del chat...
class Protocol {
	//Para el uso del protocolo se utilizara una tabla hash, en el cual se identifica un valor con una llave.
	/*
	Por ejemplo, la tabla que se muestra a continuacion.

	Identificador |	Dato
	--------------|---------
	101			  |	Harry
	102			  |	Potter
	103			  |	IRONMAN
	104			  |	IRONMAN
	105			  |	HALLO
	*/

    private String nick;
    private ClienteConnection connection;
 
    /*La tabla hash, con los valores de nicks que se van a usar, se iba a usar SQLite, pero en java era mucho trabajo*/
    private static Hashtable<String, ClienteConnection> nicks = 
        new Hashtable<String, ClienteConnection>();
 
 	//Mensajes de respuesta del servidor, a los distintos aconceticimientos.
    private static final String msg_OK = "MENSAJE ENVIADO";
    private static final String msg_NICK_IN_USE = "NICK YA ESTA USADO";
    private static final String msg_SPECIFY_NICK = "ESPECIFICA UN NICK ANTES DE USAR EL CHAT";
    private static final String msg_INVALID = "COMANDO INVALIDO";
    private static final String msg_SEND_FAILED = "ERROR AL ENVIAR EL MENSAJE";
 
    //Añade un nick en la tabla hash, si existe retorna error.
    private static boolean add_nick(String nick, ClienteConnection c) {
       	//Si existe el nick en la tabla hash, retorna false, si no esta, lo agrega y retorna verdadero.
        if (nicks.containsKey(nick)) {
            return false;
        } else {
            nicks.put(nick, c);
            return true;
        }
    }
 
    public Protocol(ClienteConnection c) {
        nick = null;
        connection = c;
    }
 
    private void log(String msg) {
        System.err.println(msg);
    }
 
    public boolean isAuthenticated() {
        return ! (nick == null);
    }
 
    /**
     * "NICK" + mensaje
     * Implements the authentication protocol.
     * This consists of checking that the message starts with the NICK command
     * and that the nick following it is not already in use.
     * returns: 
     *  msg_OK if authenticated
     *  msg_NICK_IN_USE if the specified nick is already in use
     *  msg_SPECIFY_NICK if the message does not start with the NICK command 
     */
    private String authenticate(String msg) {
        if(msg.startsWith("NICK")) {
            String tryNick = msg.substring(5);
            if(add_nick(tryNick, this.connection)) {
                log(tryNick + " ha ingresado.");
                this.nick = tryNick;
                return msg_OK;
            } else {
                return msg_NICK_IN_USE;
            }
        } else {
            return msg_SPECIFY_NICK;
        }
    }
 
    /**
     * Send a message to another user.
     * @recepient contains the recepient's nick
     * @msg contains the message to send
     * return true if the nick is registered in the hash, false otherwise
     */
    private boolean sendMsg(String receptor, String message) {
        if (nicks.containsKey(receptor)) {
            ClienteConnection connection = nicks.get(receptor);
            connection.sendMsg(nick + ": " + message);
            return true;
        } else {
            return false;
        }
    }
 
    /**
     * Process a message coming from the client
     */
    public String process(String msg) {
        if (!isAuthenticated()) 
            return authenticate(msg);
 
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
 
class ClienteConnection implements Runnable {
    private Socket client;
    private BufferedReader in = null;
    private PrintWriter out = null;
 
    ClienteConnection(Socket client) {
        this.client = client;
        try {
            /* obtain an input stream to this client ... */
            in = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
            /* ... and an output stream to the same client */
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
    }
 
    public void run() {
        String msg, response;
        Protocol protocol = new Protocol(this);
        try {
            /* loop reading lines from the client which are processed 
             * according to our protocol and the resulting response is 
             * sent back to the client */
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