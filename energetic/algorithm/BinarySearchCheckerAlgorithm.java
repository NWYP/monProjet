package energetic.algorithm;

import cumulative.Instance;
import energetic.method.BinarySearchChecker;

public class BinarySearchCheckerAlgorithm implements ICheckerAlgorithm {
    private BinarySearchChecker positiveChecker;
    private BinarySearchChecker negativeChecker;

    @Override
    public void initialize(Instance positiveInstance, Instance negativeInstance) {
        positiveChecker = new BinarySearchChecker(positiveInstance.getTasks(), positiveInstance.getC());
        negativeChecker = new BinarySearchChecker(negativeInstance.getTasks(), negativeInstance.getC());
    }


    @Override
    public boolean isConsistent() {
        return positiveChecker.isConsistent() && negativeChecker.isConsistent();
    }


}