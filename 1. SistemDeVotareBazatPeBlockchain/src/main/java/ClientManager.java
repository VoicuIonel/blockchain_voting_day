/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Georgi
 */

import java.io.BufferedReader;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import static java.nio.file.attribute.PosixFilePermission.*;
// Responsabil pentru toate comunicatiile de retea din partea clientului
// Intr-un fir nou, acesta va rula o bucla primind mesajele trimise de pe server si il trimite la firul principal pentru manipulare
// In firul principal, acesta gestioneaza toate mesajele primite. De asemenea, are interfete care deservesc ProcessManager
public class ClientManager extends NetworkManager {
    /* socketul care comunica cu serverul */
	private Socket _socket = null;
	private Block genesisBlock;
	private ArrayList<SealedObject> blockList;
	private ArrayList<String> parties;
	private HashSet<String> hashVotes;
	private int prevHash=0;

	private int clientId;

	public ClientManager(String addr, int port) {
		try {
			_socket = new Socket(addr, port);
			System.out.println("Conectat la server: " + addr + ":" + port);
			genesisBlock=new Block(0, "", "", "");
			hashVotes=new HashSet<>();
			parties = new ArrayList<>();
			parties.add("BJP");
			parties.add("INC");
			parties.add("BSP");

			blockList=new ArrayList<>();
			blockList.add(encrypt(genesisBlock));
		} catch (IOException e) {
			System.out.println("Nu se poate conecta la server " + addr + ":" + port);
			e.setStackTrace(e.getStackTrace());
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startClient() {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Bine ati venit la masina de vot ! ");
		String choice ="y";
		do{
			Block blockObj=null;

			String voterId= null;
			String voterName =null;
			String voteParty=null;

			try {
				System.out.print("Introduceti ID-ul alegatorului : ");
				voterId = br.readLine();
				System.out.print("Introduceti numele alegatorului : ");
				voterName = br.readLine();

				System.out.println("Vot pentru petreceri:");
				int voteChoice;

				do {
					for (int i=0 ;i<parties.size() ;i++) {
						System.out.println((i+1)+". "+ parties.get(i));
					}

					System.out.println("Introduceti votul : ");
					voteParty=br.readLine();
					voteChoice=Integer.parseInt(voteParty);
//	                System.out.println("vote choice : "+ voteChoice);
					if(voteChoice>parties.size()||voteChoice<1)
						System.out.println("Va rugam sa introduceti corect indexul .");
					else
						break;
				}while(true);

				voteParty = parties.get(voteChoice-1);
				blockObj=new Block(prevHash, voterId, voterName, voteParty);

				if(checkValidity(blockObj)) {
					hashVotes.add(voterId);
					sendMsg(new MessageStruct( 1,encrypt(blockObj) ));

					prevHash=blockObj.getBlockHash();
					blockList.add(encrypt(blockObj));
					//add
				}
				else
				{
					System.out.println("Votul este nevalid.");
				}
				System.out.println("Pune alt vot (y/n) ? ");
				choice=br.readLine();

			} catch (IOException e) {
				System.out.println("EROARE: linia de citire a esuat!");
				return;
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}while(choice.equals("y")||choice.equals("Y"));
		close();
	}

	public SealedObject encrypt(Block b) throws Exception
	{
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");

		// Creeaza un cifru
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

		//Cod pentru a scrie obiectul în fisier
		cipher.init( Cipher.ENCRYPT_MODE, sks );

		return new SealedObject( b, cipher);
	}
         public static Object decrypt(SealedObject sealedObject) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");

        // Crearea unui cifru
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sks);

        try {
//    		System.out.println(sealedObject.getObject(cipher));
            return sealedObject.getObject(cipher);
        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            
            e.printStackTrace();
            return null;
        }
    }

	private boolean checkValidity(Block blockObj) {
		
		if( hashVotes.contains((String)blockObj.getVoteObj().getVoterId() ))
			return false;
		else
			return true;
	}

	public void sendMsg(MessageStruct msg) throws IOException {
		sendMsg(_socket, msg);
	}

	// Inchideti socketul pentru a iesi
	public void close() {

		String userHomePath = System.getProperty("user.home");
		String fileName;
		fileName=userHomePath+"Desktop:\\blockchain_data";
		File f=new File(fileName);

		try
		{
			if(!f.exists())
				f.createNewFile();
			else {
				f.delete();
				f.createNewFile();
			}

			Files.setPosixFilePermissions(f.toPath(),
					EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE));
			System.out.println(fileName);

			ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(fileName,true));
			o.writeObject(blockList);

			o.close();

			_socket.close();

		} catch (IOException e1) {
			
			e1.printStackTrace();
		}

		System.exit(0);
	}

	
	public void msgHandler(MessageStruct msg, Socket src) {
		switch (msg._code) {
			case 0:
				/* tipul de mesaj trimis de la server la client */
//				System.out.println((String)msg._content.toString()) ;
				try {

					blockList.add((SealedObject)msg._content);

					Block decryptedBlock=(Block) decrypt((SealedObject)msg._content);
					hashVotes.add(decryptedBlock.getVoteObj().getVoterId());

				} catch (Exception e) {
					
					e.printStackTrace();
				}
				break;
			case 1:
				/* tipul de mesaj trimis de la difuzare catre toti clientii */
				//serverul gestioneaza acest lucru
				break;
			case 2:
				clientId=(int)(msg._content);
			default:
				break;
		}
	}
        

	/*
	 * Running a loop to receive messages from server. If it fails when receiving, the
	 * connections is broken. Close the socket and exit with -1.
	 */
	
	public void run() {
		while(true) {
                    receiveMsg(_socket);
		}
	}

    public void sendMsg(Socket _socket, MessageStruct msg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void receiveMsg(Socket _socket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public static void main(String[] args) {
        
    }
    
}
