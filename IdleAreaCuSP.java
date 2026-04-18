import java.io.File;

public class IdleAreaCuSP {
    public static void main(String[] args) throws Exception {
        File folder = new File("Data/CuSP");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            RunGreedyCuSP model = new RunGreedyCuSP(file.toString(), 5, 200);
        }
    }
}
