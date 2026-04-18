package energetic.algorithm;

import cumulative.Instance;
import energetic.method.SCJPChecker;

public class SCJPCheckerAlgorithm implements ICheckerAlgorithm {
    private SCJPChecker positiveChecker;
    private SCJPChecker negativeChecker;

    @Override
    public void initialize(Instance positiveInstance, Instance negativeInstance) {
        positiveChecker = new SCJPChecker(positiveInstance.getTasks(), positiveInstance.getC(),true);
        negativeChecker = new SCJPChecker(negativeInstance.getTasks(), negativeInstance.getC());
    }


    @Override
    public boolean isConsistent() {
        return negativeChecker.isConsistent()&&positiveChecker.isConsistent();
    }


}