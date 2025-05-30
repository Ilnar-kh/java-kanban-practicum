package tracker.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;

import java.io.IOException;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

import tracker.model.Subtask;


public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
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
            sendServerError(httpExchange, "500: " + e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    private void get(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        if (param.isEmpty()) {
            sendText(httpExchange, gson.toJson(taskManager.getAllSubtasks()));
        } else {
            int id = Integer.parseInt(param);
            sendText(httpExchange, gson.toJson(taskManager.getSubtask(id)));
        }
    }

    private void post(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        String body = getBody(httpExchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        if (param.isEmpty()) {
            taskManager.addNewSubtask(subtask);
        } else {
            taskManager.updateSubtask(subtask);
        }
        sendText(httpExchange, "");
    }

    private String getBody(HttpExchange httpExchange) throws IOException {
        return new String(
                httpExchange.getRequestBody().readAllBytes(),
                UTF_8
        );
    }

    private void delete(HttpExchange httpExchange) throws IOException {
        String param = getPathParam(httpExchange);
        if (param.isEmpty()) {
            taskManager.removeSubtasks();
        } else {
            int id = Integer.parseInt(param);
            taskManager.removeSubtask(id);
        }
        sendText(httpExchange, "");
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
