import java.io.File;
import java.util.Scanner;

public class CuSPInstance {
    public int C;
    public int Cmax;
    public int nbtasks;
    public Integer h[];
    public Integer[] p;
    public int[] est;
    public int[] lct;
    int processingTimesSum;

    public CuSPInstance(String fileName) throws Exception {
        Scanner s = new Scanner(new File(fileName)).useDelimiter("\\s+");
        while (!s.hasNextInt())
            s.nextLine();

        nbtasks = s.nextInt();
        C = s.nextInt();
        Cmax = s.nextInt();
        processingTimesSum = 0;
        est = new int[nbtasks];
        lct = new int[nbtasks];
        p = new Integer[nbtasks];
        h = new Integer[nbtasks];
        for (int i = 0; i < nbtasks; i++) {
            est[i] = s.nextInt();
            lct[i] = s.nextInt();
            p[i] = s.nextInt();
            h[i] = s.nextInt();
            processingTimesSum += p[i];
        }
        s.close();
    }

    public int horizon() {
        return processingTimesSum;
    }
}
