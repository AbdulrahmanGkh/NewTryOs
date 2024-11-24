import java.util.*;

public class Main {
    public static Queue<PCB> jobQueue = new LinkedList<>();
    public static Queue<PCB> readyQueue = new LinkedList<>();
    static MemoryManagment memory = new MemoryManagment(1024);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "job.txt";

        // Create a flag to signal when the sjfQueue is ready
        Flag sjfFlag = new Flag(false);
        Flag memoryFlag = new Flag(false);

            FileReaderThread fileReader = new FileReaderThread(jobQueue, filePath);
            fileReader.start();

            try {
                fileReader.join();  // Wait for jobQueue to be populated before proceeding
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Choose Scheduling Algorithm:");
            System.out.println("1. FCFS");
            System.out.println("2. SJF");
            System.out.println("3. RR");
            System.out.println("4. Exit");
            int choice = scanner.nextInt();

            if (choice == 4) {
                System.out.println("Exiting the program. Goodbye!");
                scanner.close();
                return;
            }

            int quantum = 0;
            if (choice == 3) {
                System.out.print("Enter Quantum for RR: ");
                quantum = scanner.nextInt();
            }
            Flag turn = new Flag(false);
            // Load jobs into the readyQueue based on scheduling choice
            LoadToReadyQueue loader = new LoadToReadyQueue(jobQueue, readyQueue, memory, choice, quantum, sjfFlag,memoryFlag , turn);
            loader.start();

            // Create the scheduler and pass the Flag to synchronize with sjfQueue
            ExecuteReadyQueue scheduler = new ExecuteReadyQueue(jobQueue, readyQueue, quantum, memory , turn);

            switch (choice) {
                case 1:
                    scheduler.fcfsSchedule();
                    break;
                case 2:
                    scheduler.sjfSchedule();  // Execute SJF Scheduling
                    break;
                case 3:
                    scheduler.rrSchedule();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println("\nScheduling completed. Returning to main menu...\n");
        

        scanner.close();
    }
}
