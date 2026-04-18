import java.io.File;
import java.util.Scanner;

public class CuSPInstance1 {
    public int nbTasks;
    public int Capacity;
    public Integer[] duration;
    public Integer[] demand;
    public int horizon;
    public String name;
    String fileName;
    public CuSPInstance1(String fileName) throws Exception {
        this.fileName = fileName;
        Scanner s = new Scanner(new File(fileName));
        String line = s.nextLine().split("=")[1];
        nbTasks = Integer.parseInt(line.split(";")[0]);
        String line1 = s.nextLine().split("=")[1];
        Capacity = Integer.parseInt(line1.split(";")[0]);
        String line2 = s.nextLine().split("\\[")[1];
        String[] line3 = line2.split("];")[0].split(",");
        String line4 = s.nextLine().split("\\[")[1];
        String[] line5 = line4.split("];")[0].split(",");
        int sumProcessingTime = 0;
        duration = new Integer[nbTasks];
        demand = new Integer[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            duration[i] = Integer.parseInt(line3[i]);
            demand[i] = Integer.parseInt(line5[i]);
            sumProcessingTime += duration[i];
        }
        horizon = sumProcessingTime;
        name = fileName;
    }
}
