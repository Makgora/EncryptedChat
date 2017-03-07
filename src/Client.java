import java.net.*;
import java.security.Key;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Client implements Runnable {

	private final InetAddress ADRESSE;
	private final int PORT;
	private Semaphore main;
	private static String TAB = "Client : ";
	
	public Client(InetAddress ADRESSE, int PORT, Semaphore main) {
		this.ADRESSE = ADRESSE;
		this.PORT = PORT;
		this.main = main;
	}

	private String saisirTexte() {
		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();
		return str;
	}
	
	private static SecretKey generateKey(int keySize) throws Exception {
		KeyGenerator keyGenDESede = KeyGenerator.getInstance("DESede");
		keyGenDESede.init(keySize);
		SecretKey key = keyGenDESede.generateKey();
		return key;
	}

	private static void printKey(Key key) throws Exception{
		System.out.println(TAB + "Clé (" + key.getAlgorithm() + "," + key.getFormat() + ") : " + new String(key.getEncoded(), "UTF-16"));
		System.out.println("");
	}
	
	private static byte[] cryptageKey(Key publicKey, byte[] msg) throws Exception {
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(msg);
	}

	private static byte[] cryptageDESede(Key key, String msg) throws Exception {
		Cipher cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(msg.getBytes());
	}

	@Override
	public void run() {
		try {

			Thread.sleep(3000);
			//Déclaration de la socket de communication
			Socket soc = new Socket(this.ADRESSE, this.PORT);
		
			//Déclaration des flux de lecture/écriture
			BufferedReader inT = new BufferedReader(
									new InputStreamReader(soc.getInputStream()));
			PrintWriter outT = new PrintWriter(
								new BufferedWriter(
								new OutputStreamWriter(soc.getOutputStream())), true);

			//Ouverture des flux pour transmettre un objet entre le client et le serveur
			ObjectInputStream inO = new ObjectInputStream(soc.getInputStream());
			ObjectOutputStream outO = new ObjectOutputStream(soc.getOutputStream());
			
			//Petite pause pour faire joli
			
			System.out.println(TAB + "Création de la clé secrete");

			//Génération de la clé
			SecretKey secretKey = generateKey(168);
			//Affichage de la clé
			printKey(secretKey);
			//Petite pause pour faire joli
			Thread.sleep(1000);
			


			//Récupération de la clé publique
			PublicKey publicKey = (PublicKey) inO.readObject();
			Thread.sleep(1000);
			System.out.println(TAB + "Récupération de la clé publique");
			Thread.sleep(3000);
			System.out.println(TAB + "Cryptage de la clé secrete avec la clé publique");
			Thread.sleep(3000);
			//Cryptage de la clé secrete
			byte[] secretKeyCrypted = cryptageKey(publicKey, secretKey.getEncoded());
			System.out.println(TAB + "Envoi de la clé secrete cryptée");
			
			//Envoi de la clé secrete cryptée
			outO.writeObject(secretKeyCrypted);
			outO.flush();

			Thread.sleep(8000);
			System.out.println(TAB + "Saisir un texte !");
			String str;
			
			while(true) {

				str = saisirTexte();
				byte[] msgCrypte = cryptageDESede(secretKey, str);
				System.out.println(TAB + "Envoi du message crypte : " + new String(msgCrypte, "UTF-16"));
				outO.writeObject(msgCrypte);
				if(str.equals("0")) break;
			}

			Thread.sleep(3000);
			System.out.println("END");
			
			inT.close();
			outT.close();
			soc.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
