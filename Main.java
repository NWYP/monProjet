public class Main {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 20; i++) {
            for (int k = 3; k <= 5; k++) {
                RunRCPSP sample = new RunRCPSP("Data/BL/bl20_" + i + ".rcp", k, 0, 100);
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        for (String i : new String[]{"0200","0400","0600","0800","1000","1200","1400","1600","1800","2000","2200","2400","2600","2800","3000","3200","3400","3600","3800","4000"}) {
//            for (int j = 1; j <= 5; j++) {
//                for (int k = 3; k <= 4; k++) {
//                    RunGreedyCuSP sample = new RunGreedyCuSP("Data/CuSP/Instance-" + i + "-" + j + ".dzn", k,  100);
//                }
//            }
//        }
//    }


//    public static void main(String[] args) throws Exception {
//        if (args.length != 4) {
//            throw new IllegalStateException("Please enter correct parameters.");
//        }
//        final String filename = args[0];
//        final int checker = Integer.parseInt(args[1]);
//        final int search = Integer.parseInt(args[2]);
//        final int timelimite = Integer.parseInt(args[3]);
//        RunRCPSP sample = new RunRCPSP(filename, checker, search, timelimite);
//    }
//    public static void main(String[] args) throws Exception{
//        if (args.length < 4) {
//            throw new IllegalStateException("Please enter correct parameters.");
//        }
//        final String filename = args[0];
//        final int checker = Integer.parseInt(args[1]);
//        final int search = Integer.parseInt(args[2]);
//        final int timelimite = Integer.parseInt(args[3]);
//        RunGreedyCuSP sample = new RunGreedyCuSP(filename, checker, search,  timelimite);
//    }
}