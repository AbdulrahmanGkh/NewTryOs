import java.util.*;

public class LoadToReadyQueue extends Thread {
    private Queue<PCB> jobQueue;
    private Queue<PCB> readyQueue;
    private MemoryManagment memory;
    private int schedulingChoice;
    private int quantum;
    private Flag sjfFlag; // Flag to indicate when sjfQueue is ready
    private Flag memoryFlag;
    private PriorityQueue<PCB> sjfQueue; // This should be a class-level variable
    private Flag turn;

    public LoadToReadyQueue(Queue<PCB> jobQueue, Queue<PCB> readyQueue, MemoryManagment memory, int schedulingChoice, int quantum, Flag sjfFlag , Flag memoryFlag , Flag turn) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memory = memory;
        this.schedulingChoice = schedulingChoice;
        this.quantum = quantum;
        this.sjfFlag = sjfFlag;
        this.memoryFlag = memoryFlag;
        this.turn = turn;
        this.sjfQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime)); // Initialize sjfQueue
    }

    @Override
    public void run() {
        synchronized (jobQueue) {
            switch (schedulingChoice) {
                case 1:
                    loadFCFS();
                    break;
                case 2:
                    loadSJF();
                    break;
                case 3:
                    loadRR();
                    break;
                default:
                    System.out.println("Invalid scheduling choice.");
            }
        }
    }

    private void loadFCFS() {
        System.out.println("Loading jobs using FCFS...");
        while (!jobQueue.isEmpty()) {
            PCB job = jobQueue.peek();  // Access job from the copied queue

            // Check if memory is available and load the job
            if (job.memoryRequired <= memory.getAvailableMemory()) {
                jobQueue.remove(); // Remove the job from tempQueue, not jobQueue
                readyQueue.add(job); // Add job to readyQueue
                memory.allocateMemory(job.memoryRequired);
                job.changeState("READY");
                System.out.println("Job " + job.id + " loaded into memory. Remaining Memory: " + memory.getAvailableMemory() + " MB.");
            } else {
                try {
                    // Wait for memory to become available
                    System.out.println("Not enough memory, waiting... process: " + job.id );
                    jobQueue.wait(); // Wait for memory to be released
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            try {
                Thread.sleep(100); // Simulate delay between operations
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

    }

    private void loadSJF() {
    
    }



    private void loadRR() {
        System.out.println("Loading jobs using Round Robin with Quantum " + quantum + "...");
        
        synchronized (readyQueue) {
            while (!jobQueue.isEmpty()) {
            	
            	turn.setTurnToFalse();
                PCB job = jobQueue.peek();

                if (job.memoryRequired <= memory.getAvailableMemory()) {
                    jobQueue.poll();
                    readyQueue.add(job);
                    memory.allocateMemory(job.memoryRequired);
                    job.changeState("READY");
                    System.out.println("Job " + job.id + " loaded for Round Robin. Remaining Memory: " + memory.getAvailableMemory() + " MB.");
                } else {
                    try {
                        System.out.println("Not enough memory for Job " + job.id + ". Waiting...");
                        turn.setTurnToTrue();
                        readyQueue.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Loader interrupted while waiting for memory.");
                        return;
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Loader interrupted during delay.");
                    return;
                }
            }
        }
    }
}
