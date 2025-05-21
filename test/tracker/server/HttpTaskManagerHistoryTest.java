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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerHistoryTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        // свежий менеджер + история
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        // запускаем сервер на том же менеджере
        server = new HttpTaskServer(manager);
        server.start();

        client = HttpClient.newHttpClient();
        // для разбора JSON используем тот же Gson, что и сам сервер
        gson = HttpTaskServer.getGson();

        // чистим всё перед каждым тестом
        manager.removeTasks();
        manager.removeEpics();
        manager.removeSubtasks();
    }

    @AfterEach
    void shutDown() {
        server.stop();
    }

    @Test
    @DisplayName("1. GET /history → 200 и пустой список")
    void testEmptyHistory() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), "Должен прийти 200 OK");

        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertNotNull(arr, "Тело должно быть валидным JSON-массивом");
        assertEquals(0, arr.length, "История изначально пуста");
    }

    @Test
    @DisplayName("2. После GET /tasks/{id} этот таск оказывается в истории")
    void testHistoryAfterAccess() throws Exception {
        // создаём задачу без REST-а
        Task t = new Task("T1", "D1", Status.NEW, null, null);
        int id = manager.addNewTask(t);

        // обращаемся к ней через REST, чтобы она попала в историю
        HttpRequest getTask = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .GET().build();
        HttpResponse<String> resp1 = client.send(getTask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp1.statusCode());

        // теперь смотрим историю
        HttpRequest getHist = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET().build();
        HttpResponse<String> resp2 = client.send(getHist, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp2.statusCode());

        Task[] arr = gson.fromJson(resp2.body(), Task[].class);
        assertEquals(1, arr.length, "В истории ровно один элемент");
        assertEquals(id, arr[0].getId(), "В истории — наш таск по id");
        assertEquals("T1", arr[0].getName(), "Имя тоже совпадает");
    }

    @Test
    @DisplayName("3. POST /history → 405 Method Not Allowed")
    void testPostNotAllowed() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
        assertEquals(405, resp.statusCode(), "Другие методы должны давать 405");
    }
}

