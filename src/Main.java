import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

	public static void main(String[] args) throws Exception {

		int PORT = 8088;

		ExecutorService es = Executors.newFixedThreadPool(2);
		Semaphore main = new Semaphore(1, true);
		
		System.out.println("---------------------------");
		System.out.println("Program initialization");
		System.out.println("---------------------------");
		System.out.println("");
		
		System.out.println("-Server launching-");
		System.out.println("-Client creation-");
		System.out.println("");
		Thread.sleep(1000);

		Server server = new Server(PORT , main);
		Client client = new Client(InetAddress.getByName("localhost") ,PORT, main);
		
		es.execute(server);
		es.execute(client);
		es.shutdown();
	}
}
