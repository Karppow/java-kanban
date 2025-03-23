package Http.Handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        int taskID = extractTaskId(path);
        if (taskID == -1) {
            sendJson(exchange, "Invalid request", 400);
            return;
        }

        Task task = taskManager.getTaskById(taskID);
        if (task == null) {
            sendJson(exchange, "Task not found", 404);
            return;
        }

        taskManager.deleteTask(taskID);
        sendJson(exchange, "Task deleted successfully", 200);
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        try {
            int taskId = extractTaskId(path);
            if (taskId != -1) {
                Task task = taskManager.getTaskById(taskId);
                if (task == null) {
                    throw new NotFoundException("Task not found");
                }
                sendJson(exchange, gson().toJson(task), 200); // Здесь мы возвращаем объект
            } else if (path.equals("/tasks")) {
                List<Task> tasks = taskManager.getAllTasks();
                sendJson(exchange, gson().toJson(tasks), 200); // Здесь мы возвращаем массив
            } else {
                throw new NotFoundException("Invalid request");
            }
        } catch (NotFoundException e) {
            sendError(exchange, e.getMessage(), 404);
        } catch (Exception e) {
            sendError(exchange, "Internal server error", 500);
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson().fromJson(body, Task.class);

        // Проверка на корректность задачи
        if (!taskManager.validateTask(task)) {
            System.out.println("Validation failed for new task!");
            sendError(exchange, "Task time conflicts with existing task", 406);
            return;
        }

        taskManager.createTask(task);
        sendJson(exchange, "Task created successfully", 201);
    }

    private void handleUpdateTask(HttpExchange exchange, int taskId, Task task) throws IOException {
        Task existingTask = taskManager.getTaskById(taskId);
        if (existingTask == null) {
            sendError(exchange, "Task with id " + taskId + " not found", 404);
            return;
        }
        if (!taskManager.validateTask(task)) {
            System.out.println("Validation failed for task update!");
            sendError(exchange, "Task time conflicts with existing task", 406);
            return;
        }
        taskManager.updateTask(task);
        sendJson(exchange, "Task updated successfully", 200);
    }

    private void handleCreateTask(HttpExchange exchange, Task task) throws IOException {
        if (!taskManager.validateTask(task)) {
            System.out.println("Validation failed for new task!");
            sendError(exchange, "Task time conflicts with existing task", 406);
            return;
        }
        taskManager.createTask(task);
        sendJson(exchange, "Task created successfully", 201);
    }
    private int extractTaskId(String path) {
        // Проверяем, соответствует ли путь ожидаемому формату
        if (path.matches("^/tasks/\\d+$")) {
            return Integer.parseInt(path.split("/")[2]); // Извлекаем ID задачи
        }
        return -1; // Возвращаем -1, если ID не найден
    }
    @Override
    protected void processPut(HttpExchange exchange, String path) throws IOException {
        int taskId = extractTaskId(path);
        if (taskId == -1) {
            sendError(exchange, "Invalid task ID", 400);
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(requestBody, Task.class);
        task.setId(taskId); // Убедитесь, что ID задачи установлен

        boolean updated = taskManager.updateTask(task);
        if (updated) {
            sendJson(exchange, "Task updated", 200);
        } else {
            sendError(exchange, "Task not found", 404);
        }
    }
    }
