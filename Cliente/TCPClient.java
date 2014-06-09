import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    private static int port = 8989; 
    private static String host = "localhost"; 

    private static BufferedReader stdIn;

    private static String nick;

    // Lee un nick desde el teclado e intenta autentificarse con el servidor con parámetro 'NICK'.
    // Si la respuesta no es OK, lee hace de nuevo. 

    private static String getNick(BufferedReader in, 
                                  PrintWriter out) throws IOException {
        System.out.print("Enter your nick: ");
        String msg = stdIn.readLine();
        out.println("NICK " + msg); // clave, esto es lo que une.
        String serverResponse = in.readLine();
        if ("SERVER: OK".equals(serverResponse)) return msg;
        System.out.println(serverResponse);
        return getNick(in, out);
    }

    public static void main (String[] args) throws IOException {

        Socket server = null;

        try {
            server = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }
        
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        // Obtiene un resultado desde el servidor...
        PrintWriter out = new PrintWriter(server.getOutputStream(), true);
        /* ... and an input stream */
        // y una recibe una respuesta.
        BufferedReader in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));

        nick = getNick(in, out);

        /* create a thread to asyncronously read messages from the server */
        //Crea un hilo para leer asincrónicamente los mensajes del servidor 
        ServerConn sc = new ServerConn(server);
        Thread t = new Thread(sc);
        t.start();

        String msg;
        // Ciclo que consiste en leer los mensajes escritos desde el teclado y enviándoselos al servidor.
        while ((msg = stdIn.readLine()) != null) {
            out.println(msg);
        }
    }
}

class ServerConn implements Runnable {
    private BufferedReader in = null;

    public ServerConn(Socket server) throws IOException {
        /* obtain an input stream from the server */
        in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));
    }

    public void run() {
        String msg;
        try {
            // ciclo que lee mensajes desde el servidor y los muestra a través de la consola. 
            
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}