package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(2, "Собрать вещи", "Упаковать в коробки", Status.NEW, 10);
        Subtask subtask2 = new Subtask(2, "Другая подзадача", "Другое описание", Status.DONE, 10);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask(2, "Собрать вещи", "Упаковать в коробки", Status.NEW, 2);

        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Подзадача не может быть своим же эпиком");
    }
}
