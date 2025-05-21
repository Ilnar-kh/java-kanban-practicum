package tracker.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Task;
import tracker.model.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();
        manager.removeTasks();
        manager.removeEpics();
        manager.removeSubtasks();
    }

    @AfterEach
    void shutDown() {
        server.stop();
    }

    @Test
    @DisplayName("POST /tasks → 200 и задача сохранена")
    void testAddTask() throws Exception {
        Task t = new Task("Test", "Desc",
                Status.NEW, Duration.ofMinutes(5),
                LocalDateTime.now());
        String body = gson.toJson(t);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        List<Task> all = manager.getAllTasks();
        assertEquals(1, all.size());
        assertEquals("Test", all.get(0).getName());
    }

    @Test
    @DisplayName("GET /tasks → 200 и возвращает список")
    void testGetAllTasks() throws Exception {
        manager.addNewTask(new Task("A", "", Status.NEW, null, null));
        manager.addNewTask(new Task("B", "", Status.NEW, null, null));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, arr.length);
    }

    @Test
    @DisplayName("GET /tasks/{id} → 404, если не найден")
    void testGetByIdNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} → 200 и удаляет задачу")
    void testDeleteById() throws Exception {
        int id = manager.addNewTask(new Task("X", "", Status.NEW, null, null));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .DELETE().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    @DisplayName("POST /tasks → 406 при пересечении интервалов")
    void testTaskConflict() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Task t1 = new Task("task", "des", Status.NEW,
                Duration.ofMinutes(30), now);
        manager.addNewTask(t1);

        Task t2 = new Task("task2", "des2", Status.NEW,
                Duration.ofMinutes(60), now.plusMinutes(15));
        String conflictJson = gson.toJson(t2);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(conflictJson))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, resp.statusCode());
    }
}
