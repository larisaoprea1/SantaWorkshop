import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Workshop extends Thread{
	Vector<Factory> factories = new Vector<Factory>();
	Vector<Reindeer> reindeers = new Vector<Reindeer>();
	Vector<Gift> gifts = new Vector<Gift>();
	ServerSocket socket = null;
	int factories_running;
	static int no_elfs = 0;
	public static volatile Semaphore elfRetireSemaphore = new Semaphore(0);
	private ElfRetirement elfRetire = new ElfRetirement();
	
	int nrReindeers=(int)(Math.random()*(10-8)+8);
	
	

	Workshop() {
	
		factories_running = (int)(Math.random()*(5-2)+2);	//creating factories
		for(int i=0; i<factories_running; i++) {
			factories.add(new Factory(this,i));
			
			}
		System.out.println("Factories created: " + factories_running);

		openTransportLine();			
		
		for(int i=0; i<nrReindeers; i++) {	//creating reindeer
			reindeers.add(new Reindeer(this,factories,i));
			
		}
		
		System.out.println("Reindeers spawned: " + nrReindeers);
	
		 this.start();
			
			System.out.println("Workshop is running");
		
	
		elfRetire.start();
		
	}



	
	public void run() {
		while(factoriesRunning()) {
			addElfs();		//this run() only tries to add production elfs at random times while there are factories running
			try {
			Thread.sleep((int) (Math.random() * (1000-500) + 500));
			} catch (InterruptedException e) {
			e.printStackTrace();
			}
			
		}

		
		System.out.println("All factories fullfield their orders, a total of "+ gifts.size() +" gifts had been delivered");
		elfRetire.stop();
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		
	}





	private void openTransportLine() {
		try {
			socket = new ServerSocket(7777);
			
			new Thread(() -> {	//new thread created to handle transport line
			    acceptGifts();
			}).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	
	private void acceptGifts() {
		while(factoriesRunning()) { 
			Socket elf_transport = null;
			try {
				elf_transport = socket.accept();	//attempt to make connection and take gifts
				InputStream inputStream = elf_transport.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
				
				try {
					System.out.println("GIFTS RECIVED");
					Gift gift = (Gift) objectInputStream.readObject();
					gifts.add(gift);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if(elf_transport != null)		//if connection had been made closes socket
					elf_transport.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
		
	}


	private synchronized void addElfs() {
		for(int i=0; i<factories.size(); i++) {			//tries to add production elf at each factory
			if(factories.elementAt(i).needElfs())
				factories.elementAt(i).addElf(new Elf(no_elfs++));
			}
		
	}


	public void announce(Factory F) {		
		for(int i=0;i<reindeers.size();i++) {
			if(reindeers.elementAt(i).announce(F)) {
			}
		}	
	}

	

	public synchronized void announceStop(Factory factory) {	//function called by factories that stopped producing
		factories_running--;
	}
	
	public boolean factoriesRunning() {		
		if(factories_running == 0)
			return false;
		return true;
	}
	
	
}