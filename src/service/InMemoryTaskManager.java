package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.LocalDateTime;
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
        } else {
            System.out.println("Warning: Epic with ID " + subtask.getEpicId() + " not found.");
        }
        return subtask.getId();
    }

    public boolean validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (task.getStartTime() == null) {
            throw new IllegalArgumentException("Task start time cannot be null");
        }

        if (isTaskOverlapping(task)) {
            throw new IllegalArgumentException("New task overlaps with existing tasks."); // Изменено
        }
        return true;
    }

    @Override
    public void addToHistory(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
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
    public boolean deleteTask(int id) {
        System.out.println("Attempting to delete task with ID: " + id);
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
            System.out.println("Task with ID " + id + " deleted successfully.");
            return true;
        }
        System.out.println("Task with ID " + id + " not found for deletion.");
        return false;
    }

    @Override
    public void deleteSubtask(int id) {
        System.out.println("Attempting to delete subtask with ID: " + id);
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
                System.out.println("Subtask with ID " + id + " deleted successfully from epic ID " + subtask.getEpicId() + ".");
            }
            historyManager.remove(id);
            System.out.println("Subtask with ID " + id + " deleted successfully.");
        } else {
            System.out.println("Subtask with ID " + id + " not found for deletion.");
        }
    }

    @Override
    public void deleteEpic(int id) {
        System.out.println("Attempting to delete epic with ID: " + id);
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                    historyManager.remove(subtaskId);
                    System.out.println("Subtask with ID " + subtaskId + " deleted successfully from epic ID " + id + ".");
                }
            }
            historyManager.remove(id);
            System.out.println("Epic with ID " + id + " deleted successfully.");
        } else {
            System.out.println("Epic with ID " + id + " not found for deletion.");
        }
    }

    @Override
    public Optional<Task> getTaskById(Integer taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            historyManager.add(task);
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpicById(Integer epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            Epic taskForHistory = new Epic(epic.getId(), epic.getName(), epic.getStatus(), epic.getDescription(), null);
            historyManager.add(taskForHistory);
            return Optional.of(epic);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtaskById(Integer subtaskId) {
        if (subtasks.containsKey(subtaskId)) {
            Subtask subtask = subtasks.get(subtaskId);
            Subtask taskForHistory = new Subtask(subtask.getId(), subtask.getName(), subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
            historyManager.add(taskForHistory);
            return Optional.of(subtask);
        }
        return Optional.empty();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        System.out.println("Attempting to update subtask: " + subtask);
        if (subtask == null || subtask.getId() <= 0 || !subtasks.containsKey(subtask.getId())) {
            System.out.println("Invalid subtask provided for update: " + subtask);
            throw new IllegalArgumentException("Invalid subtask");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());

        if (!oldSubtask.getStartTime().equals(subtask.getStartTime()) || !oldSubtask.getDuration().equals(subtask.getDuration())) {
            if (isTaskOverlapping(subtask)) {
                System.out.println("Updated subtask overlaps with existing tasks: " + subtask);
                throw new IllegalArgumentException("Updated subtask overlaps with existing tasks.");
            }
        }

        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
            System.out.println("Subtask with ID " + subtask.getId() + " updated successfully.");
        }
    }

    @Override
    public boolean updateTask(Task task) {
        System.out.println("Attempting to update task: " + task);
        if (task == null || task.getId() <= 0 || !tasks.containsKey(task.getId())) {
            System.out.println("Invalid task provided for update: " + task);
            throw new IllegalArgumentException("Invalid task");
        }

        Task oldTask = tasks.get(task.getId());

        if (!oldTask.getStartTime().equals(task.getStartTime()) || !oldTask.getDuration().equals(task.getDuration())) {
            if (isTaskOverlapping(task)) {
                throw new IllegalArgumentException("Updated task overlaps with existing tasks.");
            }
        }

        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return true;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || epic.getId() <= 0 || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Invalid epic");
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> subtasksByEpicId = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                subtasksByEpicId.add(subtask);
            }
        }
        return subtasksByEpicId;
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
            if (existingTask.getId() != newTask.getId()) {
                LocalDateTime existingStart = existingTask.getStartTime();
                LocalDateTime existingEnd = existingStart.plus(existingTask.getDuration());
                LocalDateTime newStart = newTask.getStartTime();
                LocalDateTime newEnd = newStart.plus(newTask.getDuration());

                if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd) ) {
                    return true;
                }
            }
        }
        return false;
    }
}
