package http.handler;

import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Epic;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
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
        int taskId = extractTaskId(path);

        if (taskId != -1) {
            Task taskJson = gson().fromJson(body, Task.class); // Изменено на Task
            taskJson.setId(taskId);
            taskManager.updateTask(taskJson); // Изменено на updateTask
            sendJson(exchange, "Task updated successfully", 200);
        } else {
            // Если ID отсутствует, создаем новую задачу
            Task task = gson().fromJson(body, Task.class);
            handleCreateTask(exchange, task);
        }
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
}
