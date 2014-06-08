import java.io.IOException;

class comandos{
	
	public static void main(String args[])  { 
		try{
		    String comando = "java -cp ./Cliente TCPClient";
		    final Process proceso= Runtime.getRuntime().exec(comando);
		} catch(IOException e){ }
	}
}
