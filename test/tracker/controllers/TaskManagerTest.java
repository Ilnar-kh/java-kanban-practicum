package tracker.controllers;

import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.model.Status;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    @Test
    void shouldAddAndGetTaskById() {
        Task task = new Task(0, "T", "D", Status.NEW);
        int id = manager.addNewTask(task);

        Task fetched = manager.getTask(id);
        assertNotNull(fetched);
        assertEquals("T", fetched.getName());
        assertEquals(id, fetched.getId());
    }

    @Test
    void shouldAddAndGetEpicById() {
        Epic epic = new Epic(0, "E", "EDesc");
        int id = manager.addNewEpic(epic);

        Epic fetched = manager.getEpic(id);
        assertNotNull(fetched);
        assertEquals("E", fetched.getName());
        assertEquals(id, fetched.getId());
    }

    @Test
    void shouldAddAndGetSubtaskById() {
        Epic epic = new Epic(0, "E2", "D2");
        int eid = manager.addNewEpic(epic);

        Subtask sub = new Subtask(0, "S", "SD", Status.NEW, eid);
        int sid = manager.addNewSubtask(sub);

        Subtask fetched = manager.getSubtask(sid);
        assertNotNull(fetched);
        assertEquals("S", fetched.getName());
        assertEquals(eid, fetched.getEpicId());
    }

    @Test
    void shouldReturnAllTasksEpicsSubtasks() {
        manager.addNewTask(new Task(0, "T1", "D", Status.NEW));
        manager.addNewTask(new Task(0, "T2", "D", Status.NEW));

        Epic epic = new Epic(0, "E", "D");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask(0, "S", "D", Status.NEW, epic.getId()));

        assertEquals(2, manager.getAllTasks().size());
        assertEquals(1, manager.getAllEpics().size());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    void shouldUpdateTask() {
        Task t = new Task(0, "T", "D", Status.NEW);
        int id = manager.addNewTask(t);

        Task upd = new Task(id, "TX", "DX", Status.IN_PROGRESS);
        manager.updateTask(upd);

        Task fetched = manager.getTask(id);
        assertEquals("TX", fetched.getName());
        assertEquals(Status.IN_PROGRESS, fetched.getStatus());
    }

    @Test
    void shouldUpdateEpic() {
        Epic e = new Epic(0, "E", "D");
        int id = manager.addNewEpic(e);

        Epic upd = new Epic(id, "EX", "DX");
        manager.updateEpic(upd);

        Epic fetched = manager.getEpic(id);
        assertEquals("EX", fetched.getName());
        assertEquals("DX", fetched.getDescription());
    }

    @Test
    void shouldUpdateSubtask() {
        Epic e = new Epic(0, "E", "D");
        int eid = manager.addNewEpic(e);

        Subtask s = new Subtask(0, "S", "D", Status.NEW, eid);
        int sid = manager.addNewSubtask(s);

        Subtask upd = new Subtask(sid, "SX", "DX", Status.DONE, eid);
        manager.updateSubtask(upd);

        Subtask fetched = manager.getSubtask(sid);
        assertEquals("SX", fetched.getName());
        assertEquals(Status.DONE, fetched.getStatus());
    }

    @Test
    void shouldRemoveTaskById() {
        int id = manager.addNewTask(new Task(0, "T", "D", Status.NEW));
        manager.removeTask(id);
        assertNull(manager.getTask(id));
    }

    @Test
    void shouldRemoveSubtaskById() {
        Epic e = new Epic(0, "E", "D");
        int eid = manager.addNewEpic(e);
        int sid = manager.addNewSubtask(new Subtask(0, "S", "D", Status.NEW, eid));

        manager.removeSubtask(sid);
        assertNull(manager.getSubtask(sid));
        assertFalse(manager.getEpic(eid).getSubtaskIds().contains(sid));
    }

    @Test
    void shouldRemoveEpicById() {
        Epic e = new Epic(0, "E", "D");
        int eid = manager.addNewEpic(e);
        int sid = manager.addNewSubtask(new Subtask(0, "S", "D", Status.NEW, eid));

        manager.removeEpic(eid);
        assertNull(manager.getEpic(eid));
        assertNull(manager.getSubtask(sid));
    }

    @Test
    void shouldRemoveAllTasks() {
        manager.addNewTask(new Task(0, "T1", "D", Status.NEW));
        manager.addNewTask(new Task(0, "T2", "D", Status.NEW));
        manager.removeTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldRemoveAllEpics() {
        Epic e = new Epic(0, "E", "D");
        int eid = manager.addNewEpic(e);
        manager.addNewSubtask(new Subtask(0, "S", "D", Status.NEW, eid));
        manager.removeEpics();
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldRemoveAllSubtasks() {
        Epic e = new Epic(0, "E", "D");
        int eid = manager.addNewEpic(e);
        manager.addNewSubtask(new Subtask(0, "S1", "D", Status.NEW, eid));
        manager.addNewSubtask(new Subtask(0, "S2", "D", Status.NEW, eid));
        manager.removeSubtasks();
        assertTrue(manager.getAllSubtasks().isEmpty());
        assertTrue(manager.getEpic(eid).getSubtaskIds().isEmpty());
    }
}
