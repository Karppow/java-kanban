package Http.Handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/subtasks/\\d+$")) {
            int subtaskID = Integer.parseInt(path.split("/")[2]);

            // Проверяем, существует ли подзадача
            Optional<Subtask> optionalSubtask = Optional.ofNullable(taskManager.getSubtaskById(subtaskID));
            if (!optionalSubtask.isPresent()) {
                sendJson(exchange, "Subtask not found", 404);
                return;
            }
            taskManager.deleteSubtask(subtaskID);
            sendJson(exchange, "Subtask deleted successfully", 200);
        } else {
            sendJson(exchange, "Invalid request", 400);
        }
    }


    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/subtasks/\\d+$")) {
            int subtaskId = Integer.parseInt(path.split("/")[2]);
            Optional<Subtask> optionalSubtask = Optional.ofNullable(taskManager.getSubtaskById(subtaskId));

            if (optionalSubtask.isEmpty()) {
                sendError(exchange, "Subtask not found", 404);
                return;
            }

            // Получаем подзадачу из Optional
            Subtask subtask = optionalSubtask.get();
            sendJson(exchange, gson().toJson(subtask), 200);
        } else if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            sendJson(exchange, gson().toJson(subtasks), 200);
        } else {
            sendError(exchange, "Invalid request", 400);
        }
    }


    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson().fromJson(body, JsonObject.class);

        boolean hasId = json != null && json.has("id") && !json.get("id").isJsonNull();
        int subtaskId = hasId ? json.get("id").getAsInt() : -1;
        Subtask subtask = gson().fromJson(body, Subtask.class);

        if (hasId) {
            Optional<Subtask> optionalSubtask = Optional.ofNullable(taskManager.getSubtaskById(subtaskId));
            if (optionalSubtask.isEmpty()) {
                sendError(exchange, "Subtask with id " + subtaskId + " not found", 404);
                return;
            }
            if (!taskManager.validateTask(subtask)) {
                System.out.println("Validation failed for subtask update!");
                sendError(exchange, "Subtask time conflicts with existing subtask", 406);
                return;
            }
            taskManager.updateSubtask(optionalSubtask.get()); // Используем объект Subtask из Optional
            sendJson(exchange, "Subtask updated successfully", 201);
        } else {
            if (!taskManager.validateTask(subtask)) {
                System.out.println("Validation failed for new subtask!");
                sendError(exchange, "Subtask time conflicts with existing subtask", 406);
                return;
            }
            taskManager.createSubtask(subtask);
            sendJson(exchange, "Subtask created successfully", 201);
        }
    }
}

