/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Georgi
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.System.exit;
import static java.lang.System.lineSeparator;
// Clasa principala pentru a simula votul folosind BlockChain prin intermediul retelei P2P
//O migrare bidirectionala intre server si client este acceptata utilizand Serializarea / Reflectia si Socketul de Java
//Proiectarea detaliata a sistemului, cazul utilizatorului si limitarile sunt elaborate in documentul word de la disciplina PIE
public class Main {
     private static final String DEFAULT_SERVER_ADDR = "localhost";
    private static final int DEFAULT_PORT = 6777;

    /*
     * Totul incepe de aici!
     */
    public static void main(String[] args) {
//        int clientId=0;
        System.out.println(" ----- Meniul principal ----- \n");
        System.out.println("1. Distribuie voturile");
        System.out.println("2. Vedeti voturile pe blockchain");
        System.out.println("3. Numara voturile");
        System.out.println("0. Iesire\n");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Introduceti alegerea dumneavoastra: ");
        int ch = scanner.nextInt();

        if(ch == 1)
        {
            System.out.println("\n ----- Casting Votes ----- \n");
            System.out.println("Please choose a role you want to be: server or client.");
            System.out.println("server PORT - Portul de ascultat; \"6777\" este portul implicit.");
            System.out.println("client SERVER_ADDRESS PORT - Adresa si portul serverului la care va conectati; \"localhost:6777\" este combinatia implicita adresa-prt.");
            System.out.println("Asigurati-va ca rulati mai intai serverul si apoi rulati clientul pentru a va conecta la acesta.");
            System.out.println("> ---------- ");

            Scanner in = new Scanner(System.in);
            String line = in.nextLine();
            String[] cmd = line.split("\\s+");

            if (cmd[0].contains("s"))
            {   // server selected

                /* work as server */
                int port = DEFAULT_PORT;
                if (cmd.length > 1) {
                    try {
                        port = Integer.parseInt(cmd[1]);
                    } catch(NumberFormatException e) {
                        System.out.println("Eroare: portul nu este un numar!");
                        in.close();
                        return;
                    }
                }

                ServerManager _svrMgr =new ServerManager(port);
                new Thread(_svrMgr).start();


            }
            else if (cmd[0].contains("c"))
            {
                //client selected

                /* work as client */
                String svrAddr = DEFAULT_SERVER_ADDR;
                int port = DEFAULT_PORT;
                if (cmd.length > 2) {
                    try {
                        svrAddr = cmd[1];
                        port = Integer.parseInt(cmd[2]);
                    } catch(NumberFormatException e) {
                        System.out.println("Eroare: portul nu este un numar!");
                        in.close();
                        return;
                    }
                }

                ClientManager _cltMgr = new ClientManager(svrAddr, port);

                /* new thread to receive msg */
                new Thread(_cltMgr).start();

                _cltMgr.startClient();
            }
            else {
                showHelp();
                in.close();
                return;
            }
            in.close();
        }

        // VIEW VOTES
        else if(ch == 2)
        {
            System.out.println("\n ----- Se afiseaza voturile ----- \n");

            String userHomePath = System.getProperty("user.home");
            String fileName;
            fileName=userHomePath+"Desktop:\\blockchain_data";
            File f=new File(fileName);

            try
            {
                if(!f.exists())
                    System.out.println("Fisierul blockchain nu a fost gasit");

                ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));

                ArrayList<SealedObject> arr=(ArrayList<SealedObject>) in.readObject();
                for(int i=1;i<arr.size();i++) {
                    System.out.println(decrypt(arr.get(i)));
                }
                in.close();

                System.out.println("-------------------------\n");

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        // Numarul de voturi
        else if(ch == 3)
        {
            String userHomePath = System.getProperty("user.home");
            String fileName;
            fileName=userHomePath+"blockchain_data";
            File f=new File(fileName);

            try
            {
                if(!f.exists())
                    System.out.println("Va rugam sa vă exprimati mai intai voturile !");

                else
                {
                    System.out.println();
                    System.out.println("-------------------------");
                    System.out.println("Numarul de voturi: ");
                    ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));

                    ArrayList<SealedObject> arr=(ArrayList<SealedObject>) in.readObject();
                    HashMap<String,Integer> voteMap = new HashMap<>();

                    for(int i=1; i<arr.size(); i++)
                    {
                        Block blk = (Block) decrypt(arr.get(i));
                        String key = blk.getVoteObj().getVoteParty();

                        voteMap.put(key,0);
                    }

                    for(int i=1;i<arr.size();i++) {
                        Block blk = (Block) decrypt(arr.get(i));
                        String key = blk.getVoteObj().getVoteParty();

                        voteMap.put(key, voteMap.get(key)+1);
                    }
                    in.close();

                    for(Map.Entry<String, Integer> entry : voteMap.entrySet()) {
                        System.out.println(entry.getKey() + " : " + entry.getValue());
                    }

                    System.out.println("-------------------------\n");
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        else if(ch == 0)
            exit(0);
    }

    public static void showHelp() {
        System.out.println("Reporniti si selectati rolul ca server sau client.");
        exit(0);
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
    
}
