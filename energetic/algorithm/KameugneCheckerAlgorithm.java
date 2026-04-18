package energetic.algorithm;

import cumulative.Instance;
import energetic.method.KameugneChecker;

public class KameugneCheckerAlgorithm implements ICheckerAlgorithm {
    private KameugneChecker positiveChecker;
    private KameugneChecker negativeChecker;

    @Override
    public void initialize(Instance positiveInstance, Instance negativeInstance) {
        positiveChecker = new KameugneChecker(positiveInstance.getTasks(), positiveInstance.getC());
        negativeChecker = new KameugneChecker(negativeInstance.getTasks(), negativeInstance.getC());
    }


    @Override
    public boolean isConsistent() {
        return negativeChecker.isConsistent() && positiveChecker.isConsistent();
    }
}
