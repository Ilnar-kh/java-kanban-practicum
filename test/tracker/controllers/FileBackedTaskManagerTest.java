package tracker.controllers;

import org.junit.jupiter.api.*;
import tracker.exceptions.ManagerSaveException;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest
        extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv").toFile();
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), "");

        manager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(
                tempFile, new InMemoryHistoryManager());

        assertTrue(loaded.getAllTasks().isEmpty(), "Пустые задачи");
        assertTrue(loaded.getAllEpics().isEmpty(), "Пустые эпики");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Пустые подзадачи");
    }

    @Test
    void shouldSaveAndLoadManagerWithTasks() throws IOException {
        Task task = new Task(0, "Task1", "Desc1", Status.NEW);
        int tId = manager.addNewTask(task);
        Epic epic = new Epic(0, "Epic1", "Desc2");
        int eId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask(0, "Sub1", "Desc3", Status.NEW, eId);
        int sId = manager.addNewSubtask(subtask);

        manager.save();
        String csv = Files.readString(tempFile.toPath());
        System.out.println("CSV\n" + csv);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(
                tempFile, new InMemoryHistoryManager());

        assertEquals(1, loaded.getAllTasks().size(), "Одна задача");
        assertEquals(1, loaded.getAllEpics().size(), "Один эпик");
        assertEquals(1, loaded.getAllSubtasks().size(), "Одна подзадача");
    }

    @Test
    void loadFromMissingFileThrows() {
        File bad = new File("no_such_file_12345.csv");
        assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(bad, new InMemoryHistoryManager()));
    }

    @Test
    void shouldLoadFromFileAfterSave() {
        Task t = new Task(0, "FT", "file test", Status.NEW);
        int id = manager.addNewTask(t);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(
                tempFile, new InMemoryHistoryManager());

        assertNotNull(loaded.getTask(id));
        assertEquals("FT", loaded.getTask(id).getName());
    }
}
