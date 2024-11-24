import java.util.Queue;

public class SystemCalls {

    // System Call to create a PCB for a job
    public static PCB createPCB(int id, int burstTime, int requiredMemory) {
        PCB pcb = new PCB(id, burstTime, requiredMemory);
        pcb.changeState("NEW");
        System.out.println("Created PCB for Job " + id + " with Burst Time = " + burstTime + " ms and Memory = " + requiredMemory + " MB.");
        return pcb;
    }

    // System Call to update job times after execution
    public static void updateJobTimes(PCB job, int currentTime) {
        job.setTurnaroundTime(currentTime);
        System.out.println("Job " + job.id + " Turnaround Time: " + job.turnaroundTime + " ms.");
    }

    // System Call to print PCB details
    public static void printPCBDetails(PCB job) {
        System.out.println("PCB Details for Job " + job.id + ":");
        System.out.println("State: " + job.state);
        System.out.println("Burst Time: " + job.burstTime + " ms");
        System.out.println("Memory Required: " + job.memoryRequired + " MB");
        System.out.println("Turnaround Time: " + job.turnaroundTime + " ms");
    }

    // System Call to print the ready queue
    public static void printReadyQueue(Queue<PCB> readyQueue) {
        System.out.println("Current Ready Queue:");
        for (PCB job : readyQueue) {
            System.out.println("Job ID: " + job.id + ", State: " + job.state + ", Burst Time: " + job.burstTime + " ms, Memory Required: " + job.memoryRequired + " MB.");
        }
    }
}
