package tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.model.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager history;

    @BeforeEach
    void setUp() {
        history = new InMemoryHistoryManager();
    }

    @Test
    void shouldStoreOnlyLastTenTasks() {
        for (int i = 1; i <= 12; i++) {
            history.add(new Task(i, "Задача " + i, "Описание", Status.NEW));
        }

        List<Task> h = history.getHistory();
        assertEquals(10, h.size(), "История должна хранить не более 10 последних задач");
        assertEquals(3, h.get(0).getId(), "Первая в истории должна быть 3-я добавленная (1 и 2 откинулись)");
        assertEquals(12, h.get(h.size() - 1).getId());
    }

    @Test
    void shouldRemoveDuplicatesAndMoveTaskToEnd() {
        Task t1 = new Task(1, "Задача", "Описание", Status.NEW);
        Task t2 = new Task(2, "Другая", "Описание", Status.NEW);

        history.add(t1);
        history.add(t2);
        history.add(t1);

        List<Task> h = history.getHistory();
        assertEquals(2, h.size());
        assertEquals(2, h.get(0).getId());
        assertEquals(1, h.get(1).getId());
    }

    @Test
    void shouldRemoveFromHistoryById() {
        Task t1 = new Task(1, "Одна", "Описание", Status.NEW);
        Task t2 = new Task(2, "Вторая", "Описание", Status.NEW);

        history.add(t1);
        history.add(t2);
        history.remove(1);

        List<Task> h = history.getHistory();
        assertEquals(1, h.size());
        assertEquals(2, h.get(0).getId());
    }

    @Test
    void shouldNotFailOnNullTask() {
        assertDoesNotThrow(() -> history.add(null));
        assertTrue(history.getHistory().isEmpty(), "Null не должен добавляться в историю");
    }
}
