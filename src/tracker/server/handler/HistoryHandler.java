package tracker.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import tracker.controllers.TaskManager;

import java.io.IOException;

import java.util.NoSuchElementException;


public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            if ("GET".equals(httpExchange.getRequestMethod())) {
                get(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(405, 0);
            }
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (IllegalStateException e) {
            sendHasInteractions(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(httpExchange, "500");
        } finally {
            httpExchange.close();
        }
    }

    private void get(HttpExchange httpExchange) throws IOException {
        sendText(httpExchange, gson.toJson(taskManager.getHistory()));
    }
}
