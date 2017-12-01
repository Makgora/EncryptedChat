package src;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.concurrent.Semaphore;

public class Server implements Runnable {
	
	private final int PORT;
	private final static String TAB = "                                                         Server : ";
	private Semaphore main; 
	
	public Server(int PORT, Semaphore main) {
		this.PORT = PORT;
		this.main = main;
	}

	public int getPORT() {
		return this.PORT;
	}

	public static void printKey(Key key) throws Exception{
		System.out.println(TAB + "Key (" + key.getAlgorithm() + "," + key.getFormat() + ") : " + new String(key.getEncoded(), "UTF-16"));
	}
	
	public static KeyPair generateKeyPair(int keySize) throws Exception{
		KeyPairGenerator keyGenRSA = KeyPairGenerator.getInstance("RSA");
		keyGenRSA.initialize(keySize);
		return keyGenRSA.genKeyPair();
	}
	
	public static PublicKey generatePublicKey(KeyPair keyPair) throws Exception{
		return keyPair.getPublic();
	}
	
	public static PrivateKey generatePrivateKey(KeyPair keyPair) throws Exception{
		return keyPair.getPrivate();
	}

	public static Key byteArrayToKeyDES(byte[] secretKeyCrypted) throws Exception {

		SecretKeyFactory sf = SecretKeyFactory.getInstance("DESede");
		return sf.generateSecret(new DESedeKeySpec(secretKeyCrypted));
//
//		DESedeKeySpec keyDES = new DESedeKeySpec(secretKeyCrypted);
//		Key keyDESbis;
//		SecretKeyFactory sf;
//		sf = SecretKeyFactory.getInstance("DESede");
//		keyDESbis = sf.generateSecret(keyDES);
//		return keyDESbis;
	}

	public static byte[] deCryptageDESede(Key key, byte[] msg) throws Exception {
		Cipher cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(msg);
	}

	public static byte[] decryptRSA(byte[] msg, PrivateKey privateKey) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(msg);
	}

	@Override
	public void run() {
		try {

			Thread.sleep(1000);
			System.out.println(TAB + "OK !");
			System.out.println(TAB + "Waiting");
			
			//Déclaration de la socket de communication
			ServerSocket s = new ServerSocket(this.PORT);

			//En attente d'une connexion
			Socket soc = s.accept();
		
			//Déclaration des flux de lecture/écriture
			BufferedReader inT = new BufferedReader(
									new InputStreamReader(soc.getInputStream()));
			PrintWriter outT = new PrintWriter(
								new BufferedWriter(
								new OutputStreamWriter(soc.getOutputStream())), true);
			
			//Ouverture des flux pour transmettre un objet entre le client et le serveur
			ObjectOutputStream outO = new ObjectOutputStream(soc.getOutputStream());
			ObjectInputStream inO = new ObjectInputStream(soc.getInputStream());
			Thread.sleep(2000);
			System.out.println(TAB + "RSA key creation");

			//Génération de la clé RSA
			KeyPair keyPair = generateKeyPair(1024);
			//Récupération de la clé publique
			PublicKey publicKey = generatePublicKey(keyPair);
			//Récupération de la clé privée
			PrivateKey privateKey = generatePrivateKey(keyPair);
			//Affichage de la clé publique
			printKey(publicKey);
			//Affichage de la clé privée
			printKey(privateKey);
			//Petite pause pour faire beau

			Thread.sleep(3000);
			System.out.println(TAB + "Envoi de la clé publique");
			
			//Envoi de la clé publique
			outO.writeObject(publicKey);
			outO.flush();
			
			//Récupération de la clé secréte cyptéée (en byte[])
			byte[] secretKeyCrypted  = (byte[]) inO.readObject();
			Thread.sleep(1000);
			System.out.println(TAB + "Récupération de la clé secréte cyptée");
			Thread.sleep(3000);
			Key secretKey = byteArrayToKeyDES(decryptRSA(secretKeyCrypted, privateKey));
			System.out.println(TAB + "Décryptage de la clé secrète cryptée avec la cle privee");
			Thread.sleep(3000);
			System.out.println(TAB + "En attente d'un message codé");

			String str;
			while(true) {
				byte[] msgCrypte = (byte[])inO.readObject();
				str = new String(deCryptageDESede(secretKey, msgCrypte));
				System.out.println(TAB + "Décryptage du message : " + new String(msgCrypte, "UTF-16"));
				Thread.sleep(1000);
				if(str.equals("0")) break;
				System.out.println(TAB + str);
				outT.println(str);
			}
			
			System.out.println(TAB + "Fermeture.");
			
			inT.close();
			outT.close();
			soc.close();
			s.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SecretKey generateKey(int keySize) throws Exception {
		KeyGenerator keyGenDESede = KeyGenerator.getInstance("DESede");
		keyGenDESede.init(keySize);
		SecretKey key = keyGenDESede.generateKey();
		return key;
	}
}
