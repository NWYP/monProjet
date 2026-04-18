package energetic.algorithm;

import cumulative.Instance;
import energetic.method.RelaxedKameugneChecker;

public class RelaxedKameugneCheckerAlgorithm implements ICheckerAlgorithm {
    private RelaxedKameugneChecker positiveChecker;
    private RelaxedKameugneChecker negativeChecker;

    @Override
    public void initialize(Instance positiveInstance, Instance negativeInstance) {
        positiveChecker = new RelaxedKameugneChecker(positiveInstance.getTasks(), positiveInstance.getC());
        negativeChecker = new RelaxedKameugneChecker(negativeInstance.getTasks(), negativeInstance.getC());
    }


    @Override
    public boolean isConsistent() {
        return negativeChecker.isConsistent()&&positiveChecker.isConsistent();
    }
}
