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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SealedObject;
// Responsabil pentru toate comunicatiile de retea din partea serverului
public class ServerManager extends NetworkManager {
    private ServerSocket _svrSocket = null;
	
	/* gestionati ID-ul clientului in crestere pentru a atribui unui nou client un id */
	private volatile AtomicInteger _cid = null;
	
	/* mentineti harta intre ID-ul clientului si socketul unui client */
	private volatile Map<Integer, Socket> _clients = null;
	
	
	public ServerManager(int svrPort) {
		try {
			_clients = new ConcurrentSkipListMap<Integer, Socket>();
			_cid = new AtomicInteger(0);
			
			_svrSocket = new ServerSocket(svrPort);
			
			System.out.println("In asteptarea clientilor...");
			System.out.println("Va rugam sa va conectati la " + InetAddress.getLocalHost() + ":" + svrPort + ".");
		} catch (IOException e) {
			System.out.println("EROARE: nu a putut asculta pe port " + svrPort);
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		while (true) {
			try {
				/* accepting new clients */
				Socket socket = _svrSocket.accept();
				addClient(socket);
				System.out.println("Clientul nou (cid este " + getCid(socket) + ") conectat!");
				
				/* create a new instance of ServerHandler to receive messages */
				new ServerHandler(this, socket).start();
				/* send the client id to the new client */
				sendMsg(socket, new MessageStruct(2, Integer.valueOf(getCid(socket))));
			} catch (IOException e) {
				/* ServerSocket is closed */
				break;
			}
		}
	}
	
	public void clientDisconnected(Socket client) {
		int cid = getCid(client);
		System.out.println("Clientul " + cid + " s-a deconectat.");
		
		deleteClient(cid);
	}
	
/* ================== Încep manevrele de mesaje ==================*/
	
	@Override
	public void msgHandler(MessageStruct msg, Socket src) {
		switch (msg._code) {
		case 0:
			/* tipul de mesaj trimis de la server la client */
				//clientul gestioneaza acest lucru
			break;
		case 1:
			/* tipul de mesaj trimis de la difuzare catre toti clientii */
			System.out.println("Difuzarea : " + (String)msg._content.toString());
			
			broadcast((SealedObject)msg._content,src );
			break;
		default:
			break;
		}
	}
	
	private void broadcast(SealedObject o, Socket src) {
		
		//difuzat tuturor cu excepția src
		int srcCid=getCid(src);
//		System.out.println("Source id : "+ srcCid);
		for(int i = _cid.get()-1 ;i>=0;i--) {
			if(i!=srcCid) {
				try {
					sendMsg(getClient(i), new MessageStruct(0, o));
				} catch (IOException e) {
					System.out.println("EROARE: Conexiune cu " + srcCid + " este rupt, mesajul nu poate fi trimis!");
					e.printStackTrace();
				} catch (NullPointerException e) {
					continue;
				}
				
				
			}
		}
	}
	
/* ==================Gestionarea mesajelor se termina ==================*/
	
/* ================== Metodele de gestionare a informatiilor despre clienti incep ==================*/
	private void addClient(Socket socket) {
		_clients.put(Integer.valueOf(_cid.getAndIncrement()), socket);
	}
	
	private boolean deleteClient(int idx) {
		if (_clients.remove(Integer.valueOf(idx)) == null) {
			System.out.println("stergerea a esuat!");
			return false;
		}
		return true;
	}
	
	private Socket getClient(int cid) {
		return (Socket)_clients.get(Integer.valueOf(cid));
	}
	
	private int getCid(Socket socket) {
		for (Map.Entry<Integer, Socket> entry : _clients.entrySet()) {
		    if (entry.getValue() == socket) {
		    	return entry.getKey().intValue();
		    }
		}
		return -1;
	}
	
/* ================== Metoda de gestionare a informatiilor despre clienti se incheie ==================*/
	

	public void close() {
		System.out.println("Serverul este pe cale sa se inchida. Toti clientii conectati vor iesi.");
		try {
			_svrSocket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		System.out.println("Pa~");
	}
        public static void main(String[] args) {
            
        }
    
}
