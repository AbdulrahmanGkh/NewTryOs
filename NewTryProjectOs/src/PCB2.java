public class PCB {
    int id; 			// Process ID
    int burstTime;      // Process Burst-Time      
    int remainingTime;  // Time Remaining for the time completion (Helps is RR algorithm )    
    int memoryRequired; // Process size of memory
    int waitingTime;	 
    int turnaroundTime; 
    String state;

    public PCB(int id, int burstTime, int memoryRequired) {
        this.id = id;
        this.burstTime = burstTime; 
        this.memoryRequired = memoryRequired;
        
        remainingTime = burstTime;
        waitingTime = 0;
        turnaroundTime = 0;
        state = "NEW";  
        }

    public void changeState(String state) {
        this.state = state;
    }

    //Setters & Getters
	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	public int getTurnaroundTime() {
		return turnaroundTime;
	}

	public void setTurnaroundTime(int turnaroundTime) {
		this.turnaroundTime = turnaroundTime;
	}

    
}