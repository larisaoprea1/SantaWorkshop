import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public class Reindeer extends Thread {

	Workshop workshop = null;
	private Vector<Factory> factories = null;
	private volatile Factory source_factory = null; //declared as volatile to make sure the value is up to date
	Socket socket = null;
	int code=0;
	
	
	
	public Reindeer(Workshop workshop, Vector<Factory> factories,int code) {
		this.workshop = workshop;
		this.factories  = factories; //it has access to factories to read data
		this.code= code;
		
		this.start();
		
	}
	

	public void run() {
		while(workshop.factoriesRunning()) {
			readData();								//reads factories data
			if(getSourceFactory() != null) {		//if assigned a factory to take gift
				transportGifts();
				}
			setSource(null); //resets source factory
	
		}
		
		
	}

		

	private void transportGifts() {
		if(source_factory.RequestTransportAcces()) {  
			source_factory.RequestMove();		
			Vector<Gift> G = source_factory.getGiftList(); //gets gift list
				if(G.isEmpty()) {		//if no more gifts, releases and return
					source_factory.MoveFinished();
					source_factory.TransportFinished();
					return;
				}
			Gift gift = G.remove(0);		//gets first gift in list, releases lock and permit
			source_factory.MoveFinished();
			source_factory.TransportFinished();
			
			try {
				socket = new Socket("localhost", 7777);
				OutputStream outputStream = socket.getOutputStream();  // get the output stream from the socket.
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream); 
				objectOutputStream.writeObject(gift);	 // create an object output stream from the output stream so we can send an object through it
				socket.close();
				
			} catch (IOException e) {
				//e.printStackTrace();
			}
			
			System.out.println("Gift sent to workshop");
		}
												
	}

	private void readData() { //reads data from factories and waits after each reading
		for(int i=0; i<factories.size();i++) {
			factories.elementAt(i).getNoGifts();
			factories.elementAt(i).getGiftList();
			try {
				sleep((int) (Math.random() * (30-10) + 10)); // reindeer packing time
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		}
	}

	
	public boolean announce(Factory F) {  //if elf has no source to transport from, gives source
		if(source_factory == null) {	//if elf already has source, does nothing
			setSource(F);
			return true;
		}
		return false;
	}

	
	private synchronized void setSource(Factory F) {
		source_factory = F;				//sets source to factory; synchronized because can be called from either workshop through announce or by elf in loop
	}									

	
	private Factory getSourceFactory() {
		return source_factory;		
	}
	public int getCode() {
		return this.code;
	}
}
 
	
	
