package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import tracker.history.InMemoryHistoryManager;

class InMemoryTaskManagerTest
        extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}
