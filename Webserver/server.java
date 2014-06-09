import java.util.Scanner;
import java.io.* ;
import java.net.* ;
import java.net.HttpURLConnection;




class clienteServerTCP {

	//Largo de el arreglo donde se van a guardar los datos leidos del archivo que contiene nombres, se puede cambiar.

	private static int tcpPort = 8989; 
    private static String tcpHost = "localhost";
    //Lectura desde la terminal
    private static BufferedReader stdIn;
    private static String nick;
    private BufferedReader in = null;



    //Funcion que captura el nick y lo envia al servidor para comprobar que no este en uso.
    private static String Nick(String mensaje, BufferedReader in, 
		PrintWriter out) throws IOException {
        //Guarda el valor ingresa en la linea de comandos (Reemplazar por lectura desde html)
        String nick = mensaje;
        out.println("NICK " + nick);
        String serverResponse = in.readLine();
        if ("SERVER: OK".equals(serverResponse)) return "TRUE";
        System.out.println(serverResponse);
        return "FALSE";
    }

  	public static void main(String args[]) throws 
	UnknownHostException, IOException { 
	 	//Variables para el envio de los bytes para ser mostrados por el browser.
		byte[] buffer = new byte[1024]; 
		int bytes; 
		int PORT = 8000; 
		ServerSocket server = new ServerSocket(PORT); 

		System.out.println("Waiting for Client at port "+PORT);

		while(true){ 
			//El servidor espera un cliente. 
			Socket client = server.accept();
			System.out.println("Client accepted "+client);
			// nos aseguramos de que el fin de línea se ajuste al estándar 
			System.setProperty("line.separator","\r\n"); 
			//Creamos una variable de tipo Scanner para recibir la informacion del browser
			Scanner read = new Scanner (client.getInputStream()); 
			//Creamos una variable de tipo PrintWriter para enviar info al browser
			PrintWriter write = new PrintWriter(client.getOutputStream(),true); 
			// Lee el primer elemento de la cabezar del request
			String head = read.next();
			//En este punto vemos si corresponde a una request de tipo GET
			if(head.toString().equals("GET")){ 
			//Se guarda la ruta leida por el servidor y se le agrega un punto para coincidir con la ruta.
				String url = "." + read.next();

				if(url.equals("./recibir.html")){
					//comprobamos si existe el archivo en el servidor
					FileInputStream fis = null; 
					boolean exist = true; 
					try { 
						fis = new FileInputStream(url); 
					} catch (FileNotFoundException e) { 
						exist = false; 
					} 
					//Si el archivo existe y es mas largo que el ./ Enviamos el fichero html al browser byte por byte
					if (exist && url.length() > 2) 
						while((bytes = fis.read(buffer)) != -1 ) // enviar fichero 
							client.getOutputStream().write(buffer, 0, bytes);
					//Si no existe se manda un error mostrando que la pagina no existe 
					else {
						write.println("HTTP/1.0 404 Not Found"); 
						write.println(); 
					}

					//LEEMOS LOS MENSAJES

					Socket tcpServer = null;        
			        try {
			        	//Conexion al servidor, pasado el host y el puerto.
			            tcpServer = new Socket(tcpHost, tcpPort);
			        } catch (UnknownHostException e) {
			            System.err.println(e);
			            System.exit(1);
			        }
			 
			        PrintWriter out = new PrintWriter(tcpServer.getOutputStream(), true);
			        BufferedReader in = new BufferedReader(new InputStreamReader(tcpServer.getInputStream()));
			 
		            //Crea un thread para permitir multiples clientes en el servidor.
			        ServerConnection sc = new ServerConnection(tcpServer);
			        Thread t = new Thread(sc);
			        t.start();

			        String msg;
			        
			        while ((msg = in.readLine()) != null) {
			            System.out.println(msg);
			        }

					//Cerramos el la conexion con el browser
					client.close(); 
				}
				
				//comprobamos si existe el archivo en el servidor
				FileInputStream fis = null; 
				boolean exist = true; 
				try { 
					fis = new FileInputStream(url); 
				} catch (FileNotFoundException e) { 
					exist = false; 
				} 
				//Si el archivo existe y es mas largo que el ./ Enviamos el fichero html al browser byte por byte
				if (exist && url.length() > 2) 
					while((bytes = fis.read(buffer)) != -1 ) // enviar fichero 
						client.getOutputStream().write(buffer, 0, bytes);
				//Si no existe se manda un error mostrando que la pagina no existe 
				else {
					write.println("HTTP/1.0 404 Not Found"); 
					write.println(); 
				}
				//Cerramos el la conexion con el browser
				client.close(); 
				}
			//En el caso de que la request sea de tipo POST
			if(head.toString().equals("POST")){
				//En el caso del POST la informacion que requerimos se encuentra alfinal del request
				String line;
				String url = "." + read.next();
				System.out.println(url);


				if(url.equals("./chatnick.html")){
					System.out.println("CHATNICK!");
					try{
						//Leemos cada linea del request hasta que encuentre una linea nula
						while((line = read.nextLine()) != null){

							//System.out.println(line);
							//Si encontra una linea vacia, esta corresponde al formato del post, luego de esta estan los datos que queremos para
							//Para realizar la consulta o guardar los datos en la base de datos
							if(line.isEmpty()){
								//Mandamos la informacion que corresponde al Action del formulario, y permitimos que el
								//Servidor abra esa pagina como un get simple.
								FileInputStream fis = null; 
								boolean exist = true; 
								try { 
									fis = new FileInputStream(url); 
								}catch (FileNotFoundException e) { 
									exist = false; 
								} 
								 
								if (exist && url.length()>2) 
									while((bytes = fis.read(buffer)) != -1 )
										client.getOutputStream().write(buffer, 0, bytes); 
								else {
									write.println("HTTP/1.0 404 Not Found"); 
								 	write.println(); 
								}
								//Finalmente se cierra la conexion al cliente, lo que permite leer la ultima linea que envia el POST.
								client.close();
							}
							//Si en esa linea encuentra el simbolo & quiere decir que trae informacion.
							if(line.contains("&")){
								//En este caso, con expresiones regulares separamos cada llave/valor y lo guardamos en un arreglo.
								String [] element = line.split("[&=]+");
								//Con la funcion writeFile, escribimos en el archivo la informacion del formulario.
								//writeFile(element[1], element[3], element[5]);
								//Imprimo el mensaje
								String mensaje = element[1].replace("+"," ");
								
								System.out.println(mensaje);

								Socket tcpServer = null;
						        
						        try {
						        	//Conexion al servidor, pasado el host y el puerto.
						            tcpServer = new Socket(tcpHost, tcpPort);
						        } catch (UnknownHostException e) {
						            System.err.println(e);
						            System.exit(1);
						        }
						 		System.out.println(mensaje);
						        stdIn = new BufferedReader(new InputStreamReader(System.in));
						 
						        PrintWriter out = new PrintWriter(tcpServer.getOutputStream(), true);
						        BufferedReader in = new BufferedReader(new InputStreamReader(tcpServer.getInputStream()));
						 
								if(Nick(mensaje, in, out) == "TRUE"){
						            System.out.println("VERDADERO");
						            //Crea un thread para permitir multiples clientes en el servidor.
							        ServerConnection sc = new ServerConnection(tcpServer);
							        Thread t = new Thread(sc);
							        t.start();
						        }
						        else{
						            System.out.println("FALSO");
						            break;   
						        }
							}
						}
					}catch(Exception e){};
				}

				if(url.equals("./chat.html")){
					System.out.println("CHAT!");
					/*En este caso se debe poder enviar mensaje a algun usuario ya creado.*/
					try{
						//Leemos cada linea del request hasta que encuentre una linea nula
						while((line = read.nextLine()) != null){

							//System.out.println(line);
							//Si encontra una linea vacia, esta corresponde al formato del post, luego de esta estan los datos que queremos para
							//Para realizar la consulta o guardar los datos en la base de datos
							if(line.isEmpty()){
								//Mandamos la informacion que corresponde al Action del formulario, y permitimos que el
								//Servidor abra esa pagina como un get simple.
								FileInputStream fis = null; 
								boolean exist = true; 
								try { 
									fis = new FileInputStream(url); 
								}catch (FileNotFoundException e) { 
									exist = false; 
								} 
								 
								if (exist && url.length()>2) 
									while((bytes = fis.read(buffer)) != -1 )
										client.getOutputStream().write(buffer, 0, bytes); 
								else {
									write.println("HTTP/1.0 404 Not Found"); 
								 	write.println(); 
								}
								//Finalmente se cierra la conexion al cliente, lo que permite leer la ultima linea que envia el POST.
								client.close();
							}
							//Si en esa linea encuentra el simbolo & quiere decir que trae informacion.
							if(line.contains("&")){
								//En este caso, con expresiones regulares separamos cada llave/valor y lo guardamos en un arreglo.
								String [] element = line.split("[&=]+");
								//Con la funcion writeFile, escribimos en el archivo la informacion del formulario.
								//writeFile(element[1], element[3], element[5]);
								//Imprimo el mensaje
								String dest = element[1];
								String mensaje = element[3].replace("+"," ");
								

								Socket tcpServer = null;

							    try {
							        tcpServer = new Socket(tcpHost, tcpPort);
							    } catch (UnknownHostException e) {
							        System.err.println(e);
							        System.exit(1);
							    }


							    PrintWriter out = new PrintWriter(tcpServer.getOutputStream(), true);
							    BufferedReader in = new BufferedReader(new InputStreamReader(
							                tcpServer.getInputStream()));

							    ServerConnection sc = new ServerConnection(tcpServer);
						        Thread t = new Thread(sc);
						        t.start();

							    out.println(mensaje);
							}
						}
					}catch(Exception e){};
				}
			}
		}
	}
}
