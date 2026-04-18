package energetic.algorithm;

import cumulative.Instance;
import energetic.method.BaptisteChecker;

public class DerienCheckerAlgorithm implements ICheckerAlgorithm {
    private BaptisteChecker positiveChecker;
    private BaptisteChecker negativeChecker;

    @Override
    public void initialize(Instance positiveInstance, Instance negativeInstance) {
        positiveChecker = new BaptisteChecker(positiveInstance.getTasks(), positiveInstance.getC(),true);
        negativeChecker = new BaptisteChecker(negativeInstance.getTasks(), negativeInstance.getC());
    }


    @Override
    public boolean isConsistent() {
        return positiveChecker.isConsistent()&&negativeChecker.isConsistent();
    }


}