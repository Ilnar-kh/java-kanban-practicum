package tracker.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest {
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
    @DisplayName("POST /epics → 200 и эпик в менеджере")
    void testAddEpic() throws Exception {
        Epic e = new Epic(1, "Epic name", "Epic description", Status.NEW);
        String json = gson.toJson(e);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        List<Epic> all = manager.getAllEpics();
        assertEquals(1, all.size());
        assertEquals("E1", all.get(0).getName());
    }

    @Test
    @DisplayName("GET /epics/{id}/subtasks → 200 и список сабтасков")
    void testGetEpicSubtasks() throws Exception {
        Epic e = new Epic(1, "Epic name", "Epic description", Status.NEW);
        int eid = manager.addNewEpic(e);
        int sid = manager.addNewSubtask(new Subtask(1, "sub", "sub description", Status.NEW, 1));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + eid + "/subtasks"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        Subtask[] arr = gson.fromJson(resp.body(), Subtask[].class);
        assertEquals(1, arr.length);
        assertEquals(sid, arr[0].getId());
    }

    @Test
    @DisplayName("DELETE /epics/{id} → 200 и удаляет эпик")
    void testDeleteEpic() throws Exception {
        Epic e = new Epic(1, "Epic name", "Epic description", Status.NEW);
        int eid = manager.addNewEpic(e);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + eid))
                .DELETE().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        assertTrue(manager.getAllEpics().isEmpty());
    }
}
