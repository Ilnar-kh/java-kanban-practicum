package tracker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.history.InMemoryHistoryManager;
import tracker.server.handler.*;
import tracker.server.adapters.LocalDateTimeAdapter;
import tracker.server.adapters.DurationAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .create();

        this.httpServer = HttpServer.create(new InetSocketAddress(8080), 0);

        httpServer.createContext("/tasks", new TaskHandler(manager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(manager, gson));
        httpServer.createContext("/epics", new EpicHandler(manager, gson));
        httpServer.createContext("/history", new HistoryHandler(manager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .create();
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на порту 8080");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        var history = new InMemoryHistoryManager();
        var manager = new InMemoryTaskManager(history);
        var server = new HttpTaskServer(manager);
        server.start();
    }
}
