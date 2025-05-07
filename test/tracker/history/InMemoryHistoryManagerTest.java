package tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.model.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTasksInOrderWithoutLimit() {
        for (int i = 1; i <= 12; i++) {
            historyManager.add(new Task(i, "Задача " + i, "Описание", Status.NEW));
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(12, history.size(), "История должна хранить все просмотренные задачи без ограничений");
        assertEquals(1, history.get(0).getId(), "Первая добавленная задача должна быть первой в списке");
    }

    @Test
    void shouldRemoveDuplicatesAndMoveTaskToEnd() {
        Task t1 = new Task(1, "Задача", "Описание", Status.NEW);
        Task t2 = new Task(2, "Другая", "Описание", Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t2.getId(), history.get(0).getId());
        assertEquals(t1.getId(), history.get(1).getId());
    }

    @Test
    void shouldRemoveFromHistoryById() {
        Task t1 = new Task(1, "Одна", "Описание", Status.NEW);
        Task t2 = new Task(2, "Вторая", "Описание", Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t2.getId(), history.get(0).getId());
    }

    @Test
    void shouldNotFailOnNullTask() {
        assertDoesNotThrow(() -> historyManager.add(null));
        assertTrue(historyManager.getHistory().isEmpty(), "Null не должен добавляться в историю");
    }
}
