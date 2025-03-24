package model;

import service.FileBackedTaskManager;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private TaskManager taskManager;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    // Конструктор с ID
    public Epic(int id, String name, TaskStatus taskStatus, String description, TaskManager taskManager) {
        super(id, name, taskStatus, description);
        this.taskManager = taskManager; // Передаем TaskManager
    }

    @Override
    public void create(FileBackedTaskManager manager) {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static Epic fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2].equals("null") ? null : parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4].equals("null") ? null : parts[4];

        Epic epic = new Epic(id, title, status, description, null);
        if (parts.length > 5) {
            String[] subtaskIdStrings = parts[5].split(";");
            for (String subtaskIdString : subtaskIdStrings) {
                epic.addSubtask(Integer.parseInt(subtaskIdString));
            }
        }
        return epic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(String.join(";", subtaskIds.stream().map(String::valueOf).toArray(String[]::new)));
        return sb.toString();
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtaskIds.stream()
                .map(taskManager::getSubtaskById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        return subtaskIds.stream()
                .map(taskManager::getSubtaskById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtaskIds.stream()
                .map(taskManager::getSubtaskById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}