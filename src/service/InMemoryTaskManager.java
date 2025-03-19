package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 1;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

    @Override
    public int createTask(Task task) {
        validateTask(task);
        task.setId(idCounter++);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task.getId();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Epic cannot be a subtask of itself.");
        }
        validateTask(subtask);
        subtask.setId(idCounter++);
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(epic.getId());
        }

        return subtask.getId();
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (task.getStartTime() == null) {
            throw new IllegalArgumentException("Task start time cannot be null");
        }

        // Проверяем пересечение с другими задачами
        if (isTaskOverlapping(task)) {
            throw new IllegalArgumentException("New task overlaps with existing tasks."); // Изменено
        }
    }


    @Override
    public int createEpic(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
        return epic.getId(); // Возвращаем ID созданного эпика
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || subtask.getId() <= 0 || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Invalid subtask");
        }

        // Сохраняем старую подзадачу для сравнения
        Subtask oldSubtask = subtasks.get(subtask.getId());

        // Если временные параметры изменились, проверяем на пересечения
        if (!oldSubtask.getStartTime().equals(subtask.getStartTime()) || !oldSubtask.getDuration().equals(subtask.getDuration())) {
            // Проверка на пересечение с другими задачами и подзадачами
            if (isTaskOverlapping(subtask)) {
                throw new IllegalArgumentException("Updated subtask overlaps with existing tasks.");
            }
        }

        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || task.getId() <= 0 || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Invalid task");
        }

        // Сохраняем старую задачу для сравнения
        Task oldTask = tasks.get(task.getId());

        // Если временные параметры изменились, проверяем на пересечения
        if (!oldTask.getStartTime().equals(task.getStartTime()) || !oldTask.getDuration().equals(task.getDuration())) {
            // Проверка на пересечение с другими задачами и подзадачами
            if (isTaskOverlapping(task)) {
                throw new IllegalArgumentException("Updated task overlaps with existing tasks.");
            }
        }

        validateTask(task); // Validate the task before updating
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task); // Ensure the updated task is also prioritized
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || epic.getId() <= 0 || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Invalid epic");
        }
        epics.put(epic.getId(), epic); // Update the epic
        updateEpicStatus(epic.getId()); // Update the status based on its subtasks
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getAllSubtasksByEpicId(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE)) {
            epic.setStatus(TaskStatus.DONE);
        } else if (epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW)) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private List<Subtask> getAllSubtasksByEpicId(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    private boolean isTaskOverlapping(Task newTask) {
        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getId() != newTask.getId()) { // Не сравниваем с самой собой
                LocalDateTime existingStart = existingTask.getStartTime();
                LocalDateTime existingEnd = existingStart.plus(existingTask.getDuration());
                LocalDateTime newStart = newTask.getStartTime();
                LocalDateTime newEnd = newStart.plus(newTask.getDuration());

                // Проверка на пересечение
                if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd) ) {
                    return true; // Пересечение найдено
                }
            }
        }
        return false; // Пересечений нет
    }
}
