package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW);
        Task task2 = new Task(1, "Другая задача", "Другое описание", Status.DONE);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }
}