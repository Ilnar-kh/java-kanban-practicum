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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerPrioritizedTest {
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
    @DisplayName("GET /prioritized → 200 и возвращает отсортированный список по времени")
    void testGetPrioritizedTasks() throws Exception {
        // создаем три задачи с разным стартом
        manager.addNewTask(new Task("A", "", Status.NEW,
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(30)));
        manager.addNewTask(new Task("B", "", Status.NEW,
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(10)));
        manager.addNewTask(new Task("C", "", Status.NEW,
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(20)));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(3, arr.length);
        // проверяем, что B (10 мин) идет первым, потом C, потом A
        assertTrue(arr[0].getStartTime().isBefore(arr[1].getStartTime()));
        assertTrue(arr[1].getStartTime().isBefore(arr[2].getStartTime()));
    }

    @Test
    @DisplayName("POST /prioritized → 404, метод не поддерживается")
    void testUnsupportedMethod() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, resp.statusCode());
    }
}
