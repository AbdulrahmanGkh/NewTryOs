import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

public class ExecuteReadyQueue {
    private Queue<PCB> jobQueue;
    private Queue<PCB> readyQueue;
    private MemoryManagment memory;

    public ExecuteReadyQueue(Queue<PCB> jobQueue, Queue<PCB> readyQueue, int quantum, MemoryManagment memory) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memory = memory;
    }

    // First-Come-First-Serve (FCFS) Scheduling
    public void fcfsSchedule() {
        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int processCounter = 0;

        // طباعة رأس الجدول
        System.out.println("Job ID    Start Time     End Time       Burst Time     Waiting Time   Turnaround Time   Used Memory   Available Memory");
        System.out.println("---------------------------------------------------------------------------------------------------------------");

        if (!readyQueue.isEmpty()) {
            while (!readyQueue.isEmpty()) {
                PCB process = readyQueue.poll(); // Get the first job from the ready queue
                process.changeState("RUNNING");

                int startTime = currentTime; // وقت بداية التنفيذ
                int endTime = startTime + process.burstTime; // وقت نهاية التنفيذ

                // تنفيذ العملية
                try {
                    Thread.sleep(process.burstTime);
                } catch (InterruptedException e) {
                    System.out.println("Process " + process.id + " execution interrupted.");
                }

                // تحديث القيم الخاصة بالذاكرة
                int usedMemory = process.memoryRequired;
                memory.releaseMemory(usedMemory);
                int availableMemory = memory.getAvailableMemory();

                // حساب أوقات الانتظار والالتفاف
                int waitingTime = startTime; // Waiting Time
                int turnaroundTime = endTime; // Turnaround Time

                // تحديث القيم في الـ PCB
                process.setWaitingTime(waitingTime);
                process.setTurnaroundTime(turnaroundTime);

                // طباعة النتائج للجدول
                System.out.printf("%-10d %-15d %-15d %-15d %-15d %-15d %-15d %-15d\n",
                        process.id, startTime, endTime, process.burstTime, waitingTime, turnaroundTime, usedMemory, availableMemory);

                // تحديث الوقت الحالي
                currentTime += process.burstTime;

                // تحديث الأوقات الإجمالية
                totalWaitingTime += waitingTime;
                totalTurnaroundTime += turnaroundTime;
                processCounter++;

                // إخطار الخيوط الأخرى
                synchronized (jobQueue) {
                    jobQueue.notifyAll();  // Notify waiting thread to check memory
                }
            }
        } else {
            System.out.println("Your readyQueue is empty.");
        }

        // طباعة المتوسطات
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        System.out.printf("Averages   %-15s %-15s %-15s %-15s %-15s %-15.2f %-15.2f\n",
                "", "", "", "", "", (double) totalWaitingTime / processCounter, (double) totalTurnaroundTime / processCounter);
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        System.out.println("process number = " + processCounter);
        System.out.println("FCFS Scheduling completed.");
    }

    // Shortest Job First (SJF) Scheduling
    public void sjfSchedule() {
        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int processCounter = 0;

        PriorityQueue<PCB> sjfQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime));
        sjfQueue.addAll(readyQueue); // Populate the SJF queue with all processes from the ready queue

        System.out.println("Starting SJF Scheduling...");

        while (!sjfQueue.isEmpty()) {
            PCB process = sjfQueue.poll();
            process.changeState("RUNNING");

            int startTime = currentTime;
            int endTime = startTime + process.burstTime;

            // Simulate process execution
            try {
                Thread.sleep(process.burstTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Process " + process.id + " execution interrupted.");
                return;
            }

            // Release memory and update process state
            memory.releaseMemory(process.memoryRequired);
            process.changeState("TERMINATED");

            int waitingTime = startTime;
            int turnaroundTime = endTime;

            process.setWaitingTime(waitingTime);
            process.setTurnaroundTime(turnaroundTime);

            System.out.println("Process " + process.id + " executed. Start Time: " + startTime +
                    ", End Time: " + endTime + ", Used Memory: " + process.memoryRequired +
                    " MB, Available Memory: " + memory.getAvailableMemory() + " MB");

            currentTime += process.burstTime;
            totalWaitingTime += waitingTime;
            totalTurnaroundTime += turnaroundTime;
            processCounter++;
        }

        System.out.println("SJF Scheduling completed.");
        System.out.println("Total processes executed: " + processCounter);
        if (processCounter > 0) {
            System.out.printf("Average Waiting Time: %.2f ms, Average Turnaround Time: %.2f ms\n",
                    (double) totalWaitingTime / processCounter, (double) totalTurnaroundTime / processCounter);
        } else {
            System.out.println("No processes were executed.");
        }
    }

    // Round Robin (RR) Scheduling
    public void rrSchedule() {
        int currentTime = 0;
        int quantum = 8; // Default quantum value for Round Robin
        int processCounter = 0;

        System.out.println("Starting Round Robin Scheduling with Quantum " + quantum + " ms...");

        while (!readyQueue.isEmpty()) {
            PCB process = readyQueue.poll();
            process.changeState("RUNNING");

            int executedTime = Math.min(process.remainingTime, quantum);
            process.remainingTime -= executedTime;
            currentTime += executedTime;

            if (process.remainingTime > 0) {
                process.changeState("WAITING");
                readyQueue.add(process); // Re-add the process to the ready queue
                System.out.println("Process " + process.id + " executed partially. Remaining Time: " +
                        process.remainingTime + " ms.");
            } else {
                process.changeState("TERMINATED");
                memory.releaseMemory(process.memoryRequired);
                System.out.println("Process " + process.id + " completed and terminated.");
                processCounter++;
            }

            try {
                Thread.sleep(100); // Simulate execution delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Execution interrupted for process " + process.id);
            }
        }

        System.out.println("RR Scheduling completed.");
        System.out.println("Total processes executed: " + processCounter);
    }
}
