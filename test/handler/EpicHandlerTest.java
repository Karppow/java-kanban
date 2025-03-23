package handler;

import http.handler.typeAdapter.DurationAdapter;
import http.handler.typeAdapter.LocalDateTimeAdapter;
import http.HttpTaskServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
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

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT + "/epics";

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
    void shouldCreateEpicSuccessfully() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description of epic");
        String jsonEpic = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(getResponse.body(), new TypeToken<List<Epic>>() {}.getType());

        assertEquals(1, epics.size());

        assertTrue(epics.get(0).getId() > 0, "Epic ID should be greater than 0");

        assertNotNull(epics.get(0), "Epic should not be null");
    }

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic 1", "First epic"));
        manager.createEpic(new Epic("Epic 2", "Second epic"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {}.getType());

        assertEquals(2, epics.size());
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertNotNull(retrievedEpic, "Epic should not be null");
        assertEquals(epicId, retrievedEpic.getId());

    }

    @Test
    void shouldReturn404ForNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldDeleteEpicSuccessfully() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldReturnAllSubtasksForEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = manager.createEpic(epic);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofHours(1); // Пример длительности
        int subtaskId1 = manager.createSubtask(new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId, duration, now));

        LocalDateTime later = now.plusHours(1);
        int subtaskId2 = manager.createSubtask(new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId, duration, later));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        TypeToken<List<Subtask>> listType = new TypeToken<List<Subtask>>() {};
        List<Subtask> subtasks = gson.fromJson(response.body(), listType.getType());

        assertEquals(2, subtasks.size());

        assertTrue(subtasks.stream().anyMatch(subtask -> subtask.getId() == subtaskId1));
        assertTrue(subtasks.stream().anyMatch(subtask -> subtask.getId() == subtaskId2));
    }
}

