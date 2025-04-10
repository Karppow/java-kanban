package http.handler;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        List<Task> tasksInHistoryList = taskManager.getAllTasks();
        sendJson(exchange, gson().toJson(tasksInHistoryList), 200);
    }
}

