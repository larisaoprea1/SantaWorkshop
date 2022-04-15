import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Factory extends Thread{

	ReentrantLock move_lock = new ReentrantLock(true);
	Workshop workshop = null;
	Vector<Elf> elfs = new Vector<Elf>();
	Vector<Gift> gifts = new Vector<Gift>();
	Semaphore semaphore = new Semaphore(10,true);
	private ReentrantLock factoryLock = new ReentrantLock();
	private ReentrantLock elfsListLock = new ReentrantLock();
	int grid_size = 0;
	int[][] grid;
	int code;
	int no_gifts_created = 0;
	int no_gifts_needed;
	
	public ReentrantLock getFactoryLock() {
		return factoryLock;
	}
	
	public Factory(Workshop workshop,int code) {
		this.workshop = workshop; //parameters initialization
		grid_size = (int) (Math.random() * (500-100) + 100);
		grid = new int[grid_size][grid_size];
		this.code = code;
		no_gifts_needed = (int) (Math.random() * (100-25) + 25); //number of gifts needed per factory
		
		new Thread(() -> { //new thread to handle position requests
			RequestPositions();
		}).start();
		
		this.start();
	}

	public void run() {
		while(factoryRunning()) { //announces workshop if there are enough gifts for a transport
			if(gifts.size() > 9) {
				workshop.announce(this);
				}
		}
		stopElfs(); //enough gifts made, stop elfs
		
		while(!gifts.isEmpty()) {	//announces that there are gifts for a transport, empty gift stock
			workshop.announce(this);
		}
		
		workshop.announceStop(this);	//announces that factory is done
		System.out.println("FACTORY "+code+" FINISHED CREATING ALL THE GIFTS NEEDED");
	}

	

	public void addElf(Elf elf) {
		int x;  //function adds a elf given from the workshop to the grid
		int y;
		move_lock.lock();  //lock locked until elf is placed
		while(true) {  //trying random spots for the elf
			x = (int) (Math.random() * (grid_size-0) + 0);
			y = (int) (Math.random() * (grid_size-0) + 0);
			if(grid[x][y] == 0)
				break;
		}
		grid[x][y] = 1;
		move_lock.unlock();
		elf.Assign(this,x,y);
		elfs.add(elf);
		elf.start();
		System.out.println("Elf "+ elf.getSerial() +" started position "+x +","+ y);
	}
	

	public void RequestMove() {
		move_lock.lock();
	}

	public boolean RequestMoveDirection(int x,int y,int i) {
		int val = -1; //function checks if the direction requested by the elf is available
		switch(i) {
		case 0:
			if(x+1 >= grid_size)
				break;
			val = grid[x+1][y];
			break;
		case 1:							//this function is not synchronized because the elf
			if(x-1 < 0)					//locks the lock before calling 
				break;					//this has been done to assure that a elf that wants to move
			val = grid[x-1][y];			//gets to check all directions before it concludes that it cannot move
			break;						
		case 2:							//if i synchronized this function instead of implementing the lock variant
			if(y+1 >= grid_size)		//multiple elfs could have entered the move phase without being able to move
				break;					//if their first choice wasn't available, effectively getting elfs stuck in a queue to move
			val = grid[x][y+1];
			break;
		case 3:
			if(y-1 < 0)
				break;
			val = grid[x][y-1];
			break;
		}
		//if the direction is available, moves the elf
		if(val == 0) {
			switch(i) {
			case 0:
				grid[x+1][y]++;
				break;
			case 1:
				grid[x-1][y]++;
				break;
			case 2:
				grid[x][y+1]++;
				break;
			case 3:
				grid[x][y-1]++;
				break;
			}
			grid[x][y]--;
			return true;
			}
		return false;
		
	}

	public void MoveFinished() {
		
		move_lock.unlock();
		
	}

	public void GiveGift(Gift gift) { //elf gives/moves gift to factory
		move_lock.lock();
		gifts.add(gift);
		no_gifts_created++;
		move_lock.unlock();
		System.out.println("Gift created at factory " + code);
	}
	
	public void RequestPositions() {  //requests positions 
		while(factoryRunning()) {
			try{
				move_lock.lock();
			
				for(int i = 0; i< elfs.size(); i++) {
					elfs.elementAt(i).GetPosition();
				}
			}
			finally {
				move_lock.unlock();
			}
			try {	//sleeps for a random amount of time
				sleep((int) (Math.random() * (5000-1000) + 1000)); // random time to check position
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean factoryRunning() {
		return no_gifts_needed > no_gifts_created; //checks if the factory should run
	}
	
	public boolean needElfs() {  //function that checks if the factory needs more production elfs
		
		if(elfs.size() < grid_size/2 && factoryRunning())
			return true;			//checks if the number of elfs < N/2 and if the factory needs more gifts
		return false;
	}

	public int getCode() {
		return code;		//get factory code, used for testing and debugging
	}

	public int getNoGifts() { //used by packing elfs
		return gifts.size();
	}

	public Vector<Gift> getGiftList() {//used by packing elfs
		return gifts;
	}

	private void stopElfs() { //signals all production elfs to stop
		for(int i=0; i<elfs.size(); i++){
			elfs.elementAt(i).stopProduction();
			
		}
		
	}

	public boolean RequestTransportAcces() { //used by packing elfs
			return semaphore.tryAcquire();
	}

	public void TransportFinished() {//used by packing elfs
		semaphore.release();
	}
	public void retireElf(Elf elf) {

		try {

			// Modifying the elfs' list and factory matrix
			elfsListLock.lock();
			factoryLock.lock();

			elfs.remove(elf);

			int X = elf.getX();
			int Y = elf.getY();

			grid[X][Y] = 0;

			System.out.println("Elf " + elf.getSerial() +" retired from factory " + code);
					
		}finally {

			elfsListLock.unlock();
			factoryLock.unlock();

		}
	}
	
}