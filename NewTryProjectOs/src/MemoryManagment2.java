
public class MemoryManagment {
	
	private int availableMemory;
	
	public MemoryManagment(int MemorySize) {
		
		availableMemory = MemorySize;
		
	}

	public void setAvailableMemory(int availableMemory) {
		this.availableMemory = availableMemory;
	}

	public void allocateMemory(int size) {
		 availableMemory -= size;
	}
	public void releaseMemory(int size) {
	  
	        availableMemory += size;
	        System.out.println("Memory released: " + size + " MB. Available Memory: " + availableMemory + " MB.");
	
	   }
	public int getAvailableMemory() {
		return availableMemory;
	}


}