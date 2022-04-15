import java.util.Random;

public class Elf extends Thread {

	int x;
	int y;
	private Factory factory=null;
	boolean GiftsNedeed;
	private int serial;
	int[] MoveDirection =  null;  //array with 4 directions, random order every time its used 
	
	
	
	Elf(int serial){
		this.serial = serial;
		GiftsNedeed = true;
		MoveDirection = new int[4];
		MoveDirection[0] = 0;
		MoveDirection[1] = 1;
		MoveDirection[2] = 2;
		MoveDirection[3] = 3;
		
		
		 
	}
	
	
	
	public void Assign(Factory factory,int x, int y) {
		this.factory = factory;	//assigns elf to factory
		this.x = x;
		this.y = y;
		
	}
	
	public void run(){
		boolean moved;
		
		while(GiftsNedeed) {
			moved = tryToMove();  //tries to move
			if(moved){
				Gift gift = new Gift(factory.getCode(),this.serial);
				factory.GiveGift(gift);			//if it moved, it creates a gift and gives it to factory
				System.out.println("Gift created ");
				
				if(moved) {
					try {
					sleep(30*10);   //  rest time
					} catch (InterruptedException e) {			//if gift created, rests
					e.printStackTrace();
					}
				}
				
				else {
					try {	//if not created, waits
						sleep((int) (Math.random() * (50-10) + 10)); // unable to move wait time
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			
			}
		}
	}

	private boolean tryToMove() {
		shuffleDirections();  //random directions in array
		boolean moved = false;
		try{
			factory.RequestMove(); //lock
			for(int i = 0 ; i < 4 ; i++) {  //tries to move in all for directions
				if(factory.RequestMoveDirection(x,y,MoveDirection[i])) {
					Move(MoveDirection[i]);
					moved = true;
					System.out.println("Elf "+ serial+ " created gift");
					break;
				}
			}
		}
		
		finally {
			factory.MoveFinished(); //unlock
		}
		
		if(moved) 
			return true;
		return false;

	}
	


	private void Move(int i) { //updates coordinates based on direction moved
		switch(i) {
		case 0:
			x++;
			break;
		case 1:
			x--;
			break;
		case 2:
			y++;
			break;
		case 3:
			y--;
			break;
		}
		
	}


	public int GetPosition() { 
		return x*1000+y; // a codification to pass both coordinates at the same time ; the leftmost numbers are x, the rightmost numbers are y

	}


	public void stopProduction() { //updates flag to stop production
		GiftsNedeed = false;
		
		System.out.println("Elf "+ serial + " stopped");
		
	}
	
	
	private void shuffleDirections() { //function to random shuffle the direction array
		Random rand = new Random();
		for (int i = 0; i < MoveDirection.length; i++) {
			int randomIndexToSwap = rand.nextInt(MoveDirection.length);
			int temp = MoveDirection[randomIndexToSwap];
			MoveDirection[randomIndexToSwap] = MoveDirection[i];
			MoveDirection[i] = temp;
		}
		
	}
	
public int getSerial() {
	return this.serial;
}
}