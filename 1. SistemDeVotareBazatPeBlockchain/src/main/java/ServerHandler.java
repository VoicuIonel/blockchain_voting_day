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
import java.net.Socket;
// Clasa laterala de server pentru a pregati si astepta mesajele de la un client specificat cu by_socket
public class ServerHandler extends Thread {
    /* socketul de la care primesc mesaje*/
	private Socket _socket = null;
	/* folosit pentru apelul invers */
	public ServerManager _svrMgr = null;
	
	public ServerHandler(ServerManager svrMgr, Socket socket) {
		_svrMgr = svrMgr;
		_socket = socket;
	}
	
	/*
	 * Rulati in continuare o bucla pentru a primi mesaje de la un client specificat cu  
	 * by_socket. Odata ce conexiunea este intrerupta, apelati ServerManager pentru eliminarea acestui client
	 * 
	 */
	@Override
	public void run() {
		while (true) {
			try {
				_svrMgr.receiveMsg(_socket);
			} catch (IOException | ClassNotFoundException e) {
				_svrMgr.clientDisconnected(_socket);
				break;
			}
		}
		
	}
        public static void main(String[] args) {
            
        }
    
}
