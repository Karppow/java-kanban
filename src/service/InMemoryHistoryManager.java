package service;

import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> history = new LinkedList<>(); // Используем LinkedList для хранения истории
    private final Map<Integer, Task> taskMap = new HashMap<>(); // HashMap для быстрого доступа

    @Override
    public void add(Task task) {
        if (task == null) return;

        // Если задача уже есть в истории, удаляем ее
        remove(task.getId());

        // Добавляем задачу в конец списка
        history.add(task);
        taskMap.put(task.getId(), task);
    }

    @Override
    public void remove(int id) {
        Task task = taskMap.remove(id); // Удаляем задачу из HashMap
        if (task != null) {
            history.remove(task); // Удаляем задачу из LinkedList
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию истории
    }
}

