import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    public static Queue<PCB> jobQueueFCFS = new LinkedList<>();
    public static Queue<PCB> jobQueueSJF = new LinkedList<>();
    public static Queue<PCB> jobQueueRR = new LinkedList<>();
    public static Queue<PCB> readyQueue = new LinkedList<>();
    public static MemoryManagment memory = new MemoryManagment(1024);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "C:\\Users\\HP\\Documents\\job.txt.txt";
        boolean[] executedAlgorithms = new boolean[3]; // Track executed algorithms: FCFS, SJF, RR

        // Corrected: Provide all required arguments to FileReaderThread
        FileReaderThread fileReader = new FileReaderThread(jobQueueFCFS, jobQueueSJF, jobQueueRR, filePath);
        fileReader.start(); // Start the thread to populate all queues

        try {
            // Wait for the FileReaderThread to finish
            fileReader.join();
        } catch (InterruptedException e) {
            // Handle any interruption during the join
            e.printStackTrace();
        }
        // Proceed with further logic after fileReader completes
        System.out.println("FileReaderThread has completed. Proceeding with the next steps.");

        boolean continueProgram = true;
        while (continueProgram) {
            readyQueue.clear(); // Clear the readyQueue after each algorithm
            memory = new MemoryManagment(1024); // Reset memory for each algorithm

            System.out.println("Choose Scheduling Algorithm:");
            if (!executedAlgorithms[0]) System.out.println("1. FCFS");
            if (!executedAlgorithms[1]) System.out.println("2. SJF");
            if (!executedAlgorithms[2]) System.out.println("3. RR");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();

            if (choice == 4) {
                continueProgram = false;
                System.out.println("Exiting the program. Goodbye!");
                break;
            }

            if (choice < 1 || choice > 3 || executedAlgorithms[choice - 1]) {
                System.out.println("Invalid choice or algorithm already executed. Try again.");
                continue;
            }

            int quantum = 0;
            if (choice == 3) {
                System.out.print("Enter Quantum for RR: ");
                quantum = scanner.nextInt();
            }

            // Select the appropriate job queue
            Queue<PCB> selectedJobQueue = switch (choice) {
                case 1 -> jobQueueFCFS;
                case 2 -> jobQueueSJF;
                case 3 -> jobQueueRR;
                default -> null;
            };

            // Load jobs into readyQueue and execute
            ReadyFlag isReady = new ReadyFlag(); // Flag to signal readiness
            LoadToReadyQueue loader = new LoadToReadyQueue(selectedJobQueue, readyQueue, memory, choice, quantum, isReady);
            loader.start();

            // Wait for ready queue to be populated
            synchronized (readyQueue) {
                while (!isReady.isReady) {
                    try {
                        readyQueue.wait(); // Wait for loader to signal readiness
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Main thread interrupted.");
                    }
                }
            }

            // Execute the selected algorithm
            ExecuteReadyQueue scheduler = new ExecuteReadyQueue(selectedJobQueue, readyQueue, quantum, memory);
            switch (choice) {
                case 1 -> scheduler.fcfsSchedule();
                case 2 -> scheduler.sjfSchedule();
                case 3 -> scheduler.rrSchedule();
            }

            executedAlgorithms[choice - 1] = true; // Mark the algorithm as executed
            System.out.println("\nScheduling completed. Returning to main menu...\n");
        }

        scanner.close();
    }
}
