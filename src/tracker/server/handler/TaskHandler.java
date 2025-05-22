package tracker.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String method = httpExchange.getRequestMethod();
            if ("GET".equals(method)) {
                get(httpExchange);
            } else if ("POST".equals(method)) {
                post(httpExchange);
            } else if ("DELETE".equals(method)) {
                delete(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(405, 0);
            }
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (IllegalStateException e) {
            sendHasInteractions(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(httpExchange, "500: " + e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void get(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        if (param.isEmpty()) {
            sendText(httpExchange, gson.toJson(taskManager.getAllTasks()));
        } else {
            int id = Integer.parseInt(param);
            Task task = taskManager.getTask(id);
            if (task == null) {
                sendNotFound(httpExchange, "Задача с id=" + id + " не найдена");
            } else {
                sendText(httpExchange, gson.toJson(task));
            }
        }
    }

    private void post(HttpExchange h) throws IOException {
        String idStr = getPathParam(h);
        String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task t = gson.fromJson(body, Task.class);

        if (idStr.isEmpty()) {
            taskManager.addNewTask(t);
        } else {
            taskManager.updateTask(t);
        }
        sendText(h, "");
    }

    private void delete(HttpExchange h) throws IOException {
        String idStr = getPathParam(h);
        if (idStr.isEmpty()) {
            taskManager.removeTasks();
        } else {
            int id = Integer.parseInt(idStr);
            taskManager.removeTask(id);
        }
        sendText(h, "");
    }

    // Метод считывания параметра пути
    private String getPathParam(HttpExchange h) {
        String full = h.getRequestURI().getPath();
        String base = h.getHttpContext().getPath();
        String param = full.substring(base.length());
        if (param.startsWith("/")) {
            param = param.substring(1);
        }
        return param;
    }
}
