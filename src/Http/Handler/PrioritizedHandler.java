package Http.Handler;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendJson(exchange, gson().toJson(prioritizedTasks), 200);
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Task newTask = gson().fromJson(isr, Task.class);

        taskManager.createTask(newTask); // Предполагается, что у вас есть метод для добавления задачи
        sendJson(exchange, gson().toJson(newTask), 201); // Возвращаем статус 201 Created
    }

    @Override
    protected void processPut(HttpExchange exchange, String path) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            sendJson(exchange, "Missing task ID", 400); // Bad Request
            return;
        }

        int taskId;
        try {
            taskId = Integer.parseInt(query.split("=")[1]); // Извлечение ID из query параметра
        } catch (NumberFormatException e) {
            sendJson(exchange, "Invalid task ID format", 400); // Bad Request
            return;
        }

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Task updatedTask = gson().fromJson(isr, Task.class);
        updatedTask.setId(taskId);

        boolean updated = taskManager.updateTask(updatedTask);
        if (updated) {
            sendJson(exchange, gson().toJson(updatedTask), 200);
        } else {
            sendJson(exchange, "Task not found", 404);
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            sendJson(exchange, "Missing task ID", 400); // Bad Request
            return;
        }

        int taskId;
        try {
            taskId = Integer.parseInt(query.split("=")[1]); // Извлечение ID из query параметра
        } catch (NumberFormatException e) {
            sendJson(exchange, "Invalid task ID format", 400); // Bad Request
            return;
        }

        // Логирование перед удалением
        boolean deleted = taskManager.deleteTask(taskId);
        if (deleted) {
            sendJson(exchange, "Task deleted", 200);
        } else {
            sendJson(exchange, "Task not found", 404);
        }
    }
}
