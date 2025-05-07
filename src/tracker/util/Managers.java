package tracker.util;

import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}


