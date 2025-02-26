package service;

import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList history = new CustomLinkedList(); // Используем кастомный связанный список
    private final HashMap<Integer, CustomLinkedList.Node> taskMap = new HashMap<>(); // Храним ссылки на узлы

    @Override
    public void add(Task task) {
        if (task == null) return;

        // Если задача уже есть в истории, удаляем ее
        remove(task.getId());

        // Добавляем задачу в конец списка и получаем новый узел
        CustomLinkedList.Node newNode = history.add(task);
        taskMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        CustomLinkedList.Node node = taskMap.remove(id); // Удаляем задачу из HashMap
        if (node != null) {
            history.remove(node); // Удаляем узел из кастомного связанного списка
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks = new ArrayList<>();
        CustomLinkedList.Node current = history.getHead();
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks; // Возвращаем копию истории
    }
}