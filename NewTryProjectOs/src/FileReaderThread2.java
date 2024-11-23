import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

public class FileReaderThread extends Thread {
    private Queue<PCB> jobQueueFCFS;
    private Queue<PCB> jobQueueSJF;
    private Queue<PCB> jobQueueRR;
    private String filePath;

    public FileReaderThread(Queue<PCB> jobQueueFCFS, Queue<PCB> jobQueueSJF, Queue<PCB> jobQueueRR, String filePath) {
        this.jobQueueFCFS = jobQueueFCFS;
        this.jobQueueSJF = jobQueueSJF;
        this.jobQueueRR = jobQueueRR;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":|;");
                if (parts.length != 3) {
                    System.out.println("Invalid line format, skipping: " + line);
                    continue;
                }

                int id = Integer.parseInt(parts[0].trim());
                int burstTime = Integer.parseInt(parts[1].trim());
                int memoryRequired = Integer.parseInt(parts[2].trim());

                if (memoryRequired > 1024) { 
                    System.out.println("Job " + id + " isn't supported due to memory limit.");
                    continue;
                }

                PCB job = SystemCalls.createPCB(id, burstTime, memoryRequired);

                // Add the same job to all three queues
                synchronized (jobQueueFCFS) {
                    jobQueueFCFS.add(new PCB(job.id, job.burstTime, job.memoryRequired));
                }
                synchronized (jobQueueSJF) {
                    jobQueueSJF.add(new PCB(job.id, job.burstTime, job.memoryRequired));
                }
                synchronized (jobQueueRR) {
                    jobQueueRR.add(new PCB(job.id, job.burstTime, job.memoryRequired));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
