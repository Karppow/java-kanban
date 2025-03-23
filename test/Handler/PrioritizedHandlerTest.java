package Handler;

import Http.Handler.TypeAdapter.DurationAdapter;
import Http.Handler.TypeAdapter.LocalDateTimeAdapter;
import Http.HttpTaskServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHandlerTest {
    private HttpTaskServer taskServer;
    private Gson gson;
    private InMemoryTaskManager manager;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testCreatePrioritizedTask() throws IOException, InterruptedException {
        Task prioritizedTask = new Task("High Priority Task", "This task has high priority", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(prioritizedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized-tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("High Priority Task", tasks.get(0).getTitle());
    }

    @Test
    public void testGetPrioritizedTaskById() throws IOException, InterruptedException {
        Task prioritizedTask = new Task("Existing Prioritized Task", "Retrieving this task", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(prioritizedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized-tasks?id=" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] retrievedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals("Existing Prioritized Task", retrievedTasks[0].getTitle());
    }

    @Test
    public void testUpdatePrioritizedTask() throws IOException, InterruptedException {
        Task prioritizedTask = new Task("Task to Update", "Updating this prioritized task", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(prioritizedTask);
        assertTrue(taskId > 0, "Task was not created successfully");

        prioritizedTask.setId(taskId);
        prioritizedTask.setTitle("Updated Prioritized Task");
        String updatedTaskJson = gson.toJson(prioritizedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized-tasks?id=" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected status code 200 but got " + response.statusCode());

        Task retrievedTask = manager.getTaskById(taskId);
        assertEquals("Updated Prioritized Task", retrievedTask.getTitle());
    }

    @Test
    public void testDeletePrioritizedTask() throws IOException, InterruptedException {
        Task prioritizedTask = new Task("Task to Delete", "Deleting this prioritized task", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(prioritizedTask);
        assertTrue(taskId > 0, "Task was not created successfully");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized-tasks?id=" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected status code 200 but got " + response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(0, tasks.size(), "Expected no tasks but found " + tasks.size());
    }
}
