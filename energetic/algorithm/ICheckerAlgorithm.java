package energetic.algorithm;


import cumulative.Instance;

public interface ICheckerAlgorithm {
    void initialize(Instance positiveInstance, Instance negativeInstance);
    //void update();
    boolean isConsistent();
}

/*
 * 		tasks = new Task[nbTasks];
        negativeTasks = new Task[nbTasks];

        int maxLct = Integer.MIN_VALUE;
        for (int i = 0; i < nbTasks; i++)
        {
            int est = startingTimes[i].getLB();
            int lct = startingTimes[i].getUB() + processingTimes[i];
            tasks[i] = new Task(i + 1, est, lct, processingTimes[i], heights[i]);
            maxLct = Math.max(maxLct, lct);
        }
        for (int i = 0; i < nbTasks; i++)
        {
            int est = startingTimes[i].getLB();
            int lct = startingTimes[i].getUB() + processingTimes[i];
            negativeTasks[i] = new Task(i + 1, -lct + maxLct, -est + maxLct, processingTimes[i], heights[i]);
        }

        positiveInstance = new Instance(tasks, capacity);
        negativeInstance = new Instance(negativeTasks, capacity);

        checkerAlgorithm.initialize(positiveInstance, negativeInstance);

*/
