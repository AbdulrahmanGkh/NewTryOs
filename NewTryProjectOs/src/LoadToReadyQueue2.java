import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

public class LoadToReadyQueue extends Thread {
    private Queue<PCB> jobQueue;      // Specific queue for the algorithm (FCFS, SJF, RR)
    private Queue<PCB> readyQueue;   // Shared readyQueue for the scheduler
    private MemoryManagment memory;
    private int schedulingChoice;    // 1 = FCFS, 2 = SJF, 3 = RR
    private int quantum;             // Quantum for Round Robin
    private ReadyFlag isReady;       // Flag to signal readiness to the scheduler

    public LoadToReadyQueue(Queue<PCB> jobQueue, Queue<PCB> readyQueue, MemoryManagment memory,
                            int schedulingChoice, int quantum, ReadyFlag isReady) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memory = memory;
        this.schedulingChoice = schedulingChoice;
        this.quantum = quantum;
        this.isReady = isReady;
    }

    @Override
    public void run() {
        switch (schedulingChoice) {
            case 1: // FCFS
                loadFCFS(jobQueue);
                break;
            case 2: // SJF
                loadSJF(jobQueue);
                break;
            case 3: // RR
                loadRR(jobQueue);
                break;
            default:
                System.out.println("Invalid scheduling choice.");
                return;
        }

        synchronized (readyQueue) {
            isReady.isReady = true; // Signal that loading is complete
            readyQueue.notifyAll(); // Notify waiting threads
        }
    }

    private void loadFCFS(Queue<PCB> jobQueue) {
        System.out.println("Loading jobs using FCFS...");

        while (!jobQueue.isEmpty()) { // Use jobQueueFCFS for FCFS-specific jobs
            PCB job = jobQueue.peek();  // Peek the next job from jobQueueFCFS

            synchronized (jobQueue) { // Synchronize on jobQueueFCFS for thread safety
                // Check if memory is available and load the job
                if (job.memoryRequired <= memory.getAvailableMemory()) {
                   jobQueue.poll(); // Remove the job from jobQueueFCFS
                    readyQueue.add(job); // Add job to readyQueue
                    memory.allocateMemory(job.memoryRequired); // Allocate memory
                    job.changeState("READY");
                    System.out.println("Job " + job.id + " loaded into memory. Remaining Memory: " 
                                        + memory.getAvailableMemory() + " MB.");
                } else {
                    try {
                        // Wait for memory to become available
                        System.out.println("Not enough memory, waiting... process: " + job.id);
                     jobQueue.wait(); // Wait for memory release notification
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
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


    private void loadSJF(Queue<PCB> jobQueueSJF) {
        System.out.println("Loading jobs using SJF...");
        PriorityQueue<PCB> sjfQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime));
        synchronized (jobQueueSJF) {
            sjfQueue.addAll(jobQueueSJF); // Copy all jobs from SJF queue to PriorityQueue
            jobQueueSJF.clear(); // Clear the SJF queue
        }

        while (!sjfQueue.isEmpty()) {
            PCB job = sjfQueue.poll(); // Get the job with the shortest burst time
            synchronized (memory) {
                if (job.memoryRequired <= memory.getAvailableMemory()) {
                    readyQueue.add(job); // Add to readyQueue
                    memory.allocateMemory(job.memoryRequired); // Allocate memory
                    job.changeState("READY");
                    System.out.println("SJF: Job " + job.id + " loaded into memory. Remaining Memory: " 
                                        + memory.getAvailableMemory() + " MB.");
                } else {
                 System.out.println("SJF: Not enough memory for Job " + job.id + ". Waiting...");
                 try {
                     memory.wait();
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     System.out.println("SJF loader interrupted.");
                     return;
                 }
             }
            
         }
     }
 
            }

    private void loadRR(Queue<PCB> jobQueueRR) {
        System.out.println("Loading jobs using Round Robin with Quantum " + quantum + " ms...");
        while (!jobQueueRR.isEmpty()) {
            PCB job = jobQueueRR.poll(); // Get the next job from the RR queue
            synchronized (memory) {
                if (job.memoryRequired <= memory.getAvailableMemory()) {
                    readyQueue.add(job); // Add to readyQueue
                    memory.allocateMemory(job.memoryRequired); // Allocate memory
                    job.changeState("READY");
                    System.out.println("RR: Job " + job.id + " loaded into memory. Remaining Memory: " 
                                        + memory.getAvailableMemory() + " MB.");
                } else {
                    System.out.println("RR: Not enough memory for Job " + job.id + ". Waiting...");
                    try {
                        memory.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("RR loader interrupted.");
                        return;
                    }
                }
            }
        }
    }
}
