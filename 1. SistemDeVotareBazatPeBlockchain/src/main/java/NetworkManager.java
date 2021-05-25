/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Georgi
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
// Clasa de baza ServerManager și ClientManager pentru a furniza operatiuni de retea
public abstract class NetworkManager implements Runnable {
   /*
	 * Trimiteti un mesaj la socket
	 */
	public void sendMsg(Socket socket, MessageStruct msg) 
			throws IOException {
		ObjectOutputStream out;
		
		out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(msg);
	}
	
	/*
	 * Incercati sa primiti un mesaj de la socket
	 */
	public void receiveMsg(Socket socket) 
			throws ClassNotFoundException, IOException {
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		Object inObj = inStream.readObject();
		
		if (inObj instanceof MessageStruct) {
			MessageStruct msg = (MessageStruct) inObj;
			msgHandler(msg, socket);
		}
		
	}
	
	/*
	 * Close socket
	 */
	public void close(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * O interfata pentru ServerManager si ClientManager pentru implementare gestioneaza
         * mesajele primite
	 * 
	 */
	public abstract void msgHandler(MessageStruct msg, Socket src); 
        public static void main(String[] args) {
            
        }
    
}
