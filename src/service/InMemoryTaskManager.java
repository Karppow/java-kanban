package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

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
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (task.getStartTime() == null) {
            throw new IllegalArgumentException("Task start time cannot be null");
        }
        if (isTaskOverlapping(task)) {
            throw new IllegalArgumentException("New task overlaps with existing tasks.");
        }
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
        if (subtask.getStartTime() == null) {
            throw new IllegalArgumentException("Subtask start time cannot be null");
        }

        if (isTaskOverlapping(subtask)) {
            throw new IllegalArgumentException("New subtask overlaps with existing tasks.");
        }
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

    @Override
    public int createEpic(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
        return epic.getId(); // Возвращаем ID созданного эпика
    }

    public Task getTaskById(Task id) {
        return null;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    public Subtask getSubtask(int id) {
        return null;
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

    public void deleteSubtask(Subtask id) {

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
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public Task getTask(int id) {
        return getTaskById(id);
    }

    public Subtask getSubtask(Subtask id) {
        return getSubtaskById(id.getId());
    }

    @Override
    public void updateTask(int id, Task task) {

    }

    @Override
    public void updateEpic(int epicId, Epic epic) {

    }

    public Task getSubtask(Integer id) {
        return null;
    }

    private void updateEpicStatus ( int epicId){
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isTaskOverlapping(Task newTask) {
        for (Task existingTask : prioritizedTasks) {
            if (existingTask.isOverlapping(newTask)) {
                return true;
            }
        }
        return false;
    }
}