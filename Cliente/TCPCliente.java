import java.io.*;
import java.net.*;
 
class TCPClient {
	//Puerto e Ip de conexion con el servidor
    private static int port = 6969; 
    private static String host = "localhost";
    //Lectura desde la terminal
    private static BufferedReader stdIn;
    private static String nick;
 
    //Funcion que captura el nick y lo envia al servidor para comprobar que no este en uso.
    private static String Nick(BufferedReader in, 
		PrintWriter out) throws IOException {
        System.out.print("Crea tu nick: ");
        //Guarda el valor ingresa en la linea de comandos (Reemplazar por lectura desde html)
        String nick = stdIn.readLine();
        out.println("NICK: " + nick);
        String serverResponse = in.readLine();
        if ("SERVER: MENSAJE ENVIADO".equals(serverResponse))
        	return nick;
        System.out.println(serverResponse);
        return Nick(in, out);
    }
 
    public static void main (String[] args) throws IOException {
 
        Socket server = null;
        try {
        	//Conexion al servidor, pasado el host y el puerto.
            server = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }
 
        stdIn = new BufferedReader(new InputStreamReader(System.in));
 
        PrintWriter out = new PrintWriter(server.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
 
        nick = Nick(in, out);
 
        //Crea un thread para permitir multiples clientes en el servidor.
        ServerConnection sc = new ServerConnection(server);
        Thread t = new Thread(sc);
        t.start();
 
        String stringToServer;
        //Lectura de la linea de comandos y lo envia al servidor para su procesamiento
        while ((stringToServer = stdIn.readLine()) != null) {
            out.println(stringToServer);
        }
    }
}
 
 //Clase que crea la conexiion entre el Cliente y el Servidor.
class ServerConnection implements Runnable {
    private BufferedReader in = null;
 
    public ServerConnection(Socket server) throws IOException {
        //Obtiene una respuesta del servidor
        in = new BufferedReader(new InputStreamReader(
        server.getInputStream()));
    }
    //Lee del servidor y lo muestra por pantalla.
    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}