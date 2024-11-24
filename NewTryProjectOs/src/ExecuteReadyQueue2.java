import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Comparator;

public class ExecuteReadyQueue {
    private Queue<PCB> jobQueue;
    private Queue<PCB> readyQueue;
    private Queue<PCB> PriorityQueue ;
    private PriorityQueue<PCB> sjfQueue;
    private MemoryManagment memory;
    private Flag turn;

    public ExecuteReadyQueue(Queue<PCB> jobQueue, Queue<PCB> readyQueue, int quantum, MemoryManagment memory ,Flag turn) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memory = memory;
        this.turn = turn;
       // this.sjfQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime));
    }

    // First-Come-First-Serve (FCFS) Scheduling
    public void fcfsSchedule() {
        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int processCounter = 0;

            while (!jobQueue.isEmpty() && !readyQueue.isEmpty()) {
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

                // طباعة تتبع العملية
                System.out.println("Process " + process.id + " executed. Start Time: " + startTime + 
                    ", End Time: " + endTime + ", Used Memory: " + usedMemory + 
                    " MB, Available Memory: " + availableMemory + " MB");

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

        // طباعة ملخص العمليات
        System.out.println("FCFS Scheduling completed.");
        System.out.println("Total processes executed: " + processCounter);
        System.out.printf("Average Waiting Time: %.2f ms, Average Turnaround Time: %.2f ms\n",
                (double) totalWaitingTime / processCounter, (double) totalTurnaroundTime / processCounter);
    }

    public void sjfSchedule() {
        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int processCounter = 0;
        this.sjfQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.burstTime));
        
        while (!jobQueue.isEmpty()) {
            PCB process = jobQueue.poll();
            if (process != null && process.burstTime > 0 && process.memoryRequired > 0) {
                sjfQueue.add(process);
                System.out.println("Job " + process.id + " added to SJF Queue.");
            } else {
                System.out.println("Invalid job detected and skipped.");
                synchronized (jobQueue) {
                    jobQueue.notifyAll();
                }
            }
          
        
        }


        System.out.println("Executing SJF Scheduling...");

        // معالجة العمليات في ReadyQueue
        while (!readyQueue.isEmpty()) {
            PCB process = readyQueue.poll();
            process.changeState("RUNNING");

            int startTime = currentTime;
            int endTime = startTime + process.burstTime;

            try {
                Thread.sleep(process.burstTime); // محاكاة وقت التنفيذ
            } catch (InterruptedException e) {
                System.out.println("Process " + process.id + " execution interrupted.");
            }

            // تحرير الذاكرة بعد التنفيذ
            memory.releaseMemory(process.memoryRequired);

            // تحديث القيم وحساب الأوقات
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

            // إخطار أي خيوط تنتظر تحرير الذاكرة
            synchronized (sjfQueue) {
                sjfQueue.notifyAll();
            }
        }

        System.out.println("SJF Scheduling completed.");
        System.out.println("Total processes executed: " + processCounter);
        System.out.printf("Average Waiting Time: %.2f ms, Average Turnaround Time: %.2f ms\n",
                (double) totalWaitingTime / processCounter, (double) totalTurnaroundTime / processCounter);
    }


    
    public void rrSchedule() {
        int currentTime = 0;
        int processCounter = 0;
        int endtime = 0;
        while(!jobQueue.isEmpty() || (jobQueue.isEmpty() && !readyQueue.isEmpty())) {
        synchronized (readyQueue) {
	        while (turn.isMyTurn() && (!readyQueue.isEmpty() || (jobQueue.isEmpty() && !readyQueue.isEmpty()))) {
	            PCB process = readyQueue.poll(); // استخراج العملية الحالية
	            process.changeState("RUNNING");
	
	            int startTime = currentTime; // وقت بداية التنفيذ
	            int executedTime = Math.min(process.remainingTime, 8); // مقدار الوقت المنفذ
	            currentTime += executedTime; // تحديث الوقت الحالي
	            process.remainingTime -= executedTime; 
	            
	            try {
	                Thread.sleep(executedTime);
	            } catch (InterruptedException e) {
	                System.out.println("Process " + process.id + " execution interrupted.");
	            }
	
	            if (process.remainingTime > 0) {
	            	 System.out.println("Process Executed partially: " + process.id + ", total burst time: " + process.burstTime + 
	            			 "ms, Remaining time: " + process.remainingTime
		                        +" ms, Available Memory: " + memory.getAvailableMemory() + " MB");
	                readyQueue.add(process);
	            } else {
	                // العملية انتهت
	                process.changeState("TERMINATED");
	                memory.releaseMemory(process.memoryRequired); // تحرير الذاكرة بعد التنفيذ
	                processCounter++;
	                endtime = currentTime;
	                System.out.println("Process " + process.id + " executed. Start Time: " + startTime + 
	                        ", End Time: " + endtime + ", Used Memory: " + (1024-memory.getAvailableMemory()) + 
	                        " MB, Available Memory: " + memory.getAvailableMemory() + " MB");
	                readyQueue.notifyAll();
	            }
	        }
        }
            // إخطار الخيوط الأخرى
          
        }

        // إنهاء الجدولة
        System.out.println("RR Scheduling completed.");
        System.out.println("Total processes executed: " + processCounter);
    }


    // Similar logic for SJF and RR scheduling (with proper memory release and notifications)
}
