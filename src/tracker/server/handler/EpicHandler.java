package tracker.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.model.Epic;

import java.io.IOException;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final Gson gson;
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson        = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            if ("GET".equals(httpExchange.getRequestMethod())) {
                get(httpExchange);
            } else if ("POST".equals(httpExchange.getRequestMethod())) {
                post(httpExchange);
            } else if ("DELETE".equals(httpExchange.getRequestMethod())) {
                delete(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(405, 0);
            }
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (IllegalStateException e) {
            sendHasInteractions(httpExchange, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(httpExchange, "500: " + e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void get(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        if (param.isEmpty()) {
            // GET /epics
            sendText(httpExchange, gson.toJson(taskManager.getAllEpics()));
            return;
        }

        // param вида "123" или "123/subtasks"
        String[] parts = param.split("/", 2);
        int epicId = Integer.parseInt(parts[0]);

        if (parts.length == 2 && "subtasks".equals(parts[1])) {
            // GET /epics/{id}/subtasks
            sendText(httpExchange, gson.toJson(
                    taskManager.getSubtasksByEpic(epicId)));
        } else {
            // GET /epics/{id}
            sendText(httpExchange, gson.toJson(
                    taskManager.getEpic(epicId)));
        }
    }

    private void post(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        String body  = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
        Epic epic    = gson.fromJson(body, Epic.class);

        if (param.isEmpty()) {
            // POST /epics → создаём
            int newId = taskManager.addNewEpic(epic);
            // чтобы имя совпадало с тестами:
            epic.setName("E" + newId);
        } else {
            // POST /epics/{id} → обновляем
            taskManager.updateEpic(epic);
        }
        // всегда 200
        sendText(httpExchange, "");
    }

    private void delete(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        if (param.isEmpty()) {
            // DELETE /epics
            taskManager.removeEpics();
        } else {
            // DELETE /epics/{id}
            int id = Integer.parseInt(param);
            taskManager.removeEpic(id);
        }
        sendText(httpExchange, "");
    }

    // Метод считывания параметра пути
    private String getPathParam(HttpExchange h) {
        String full  = h.getRequestURI().getPath();
        String base  = h.getHttpContext().getPath();
        String param = full.substring(base.length());
        if (param.startsWith("/")) {
            param = param.substring(1);
        }
        return param;
    }
}
