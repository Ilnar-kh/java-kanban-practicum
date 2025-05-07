package tracker.controllers;
import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        File tempFile = File.createTempFile("testEmpty", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadManagerWithTasks() throws IOException {
        File tempFile = File.createTempFile("testTasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());

        Task task = new Task(1, "Task1", "Desc1", Status.NEW);
        Epic epic = new Epic(2, "Epic1", "Desc2", Status.NEW);
        Subtask subtask = new Subtask(3, "Subtask1", "Desc3", Status.NEW, 2);

        manager.addNewTask(task);
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        manager.save();
        String csv = Files.readString(tempFile.toPath());
        System.out.println("=== CSV CONTENT ===\n" + csv);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());
    }
}