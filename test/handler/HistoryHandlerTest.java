package service.handler;

import http.handler.typeAdapter.DurationAdapter;
import http.handler.typeAdapter.LocalDateTimeAdapter;
import http.HttpTaskServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final String HISTORY_URL = BASE_URL + "/history";


    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldReturnEmptyHistoryIfNoTasksViewed() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldReturnHistoryWithViewedTasks() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 3, 1, 10, 0));
        int taskId = manager.createTask(task);

        viewTask(taskId, TaskType.TASK);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertEquals(1, history.size(), "History should contain 1 task.");
        assertEquals(taskId, history.get(0).getId(), "First task in history should be the task.");
    }

    private void viewTask(int taskId, TaskType type) throws IOException, InterruptedException {
        String endpoint = switch (type) {
            case TASK -> "/tasks/";
            case EPIC -> "/epics/";
            case SUBTASK -> "/subtasks/";
        };

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint + taskId))
                .GET()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        manager.addToHistory(taskId);
    }
}