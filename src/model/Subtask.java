package model;

import service.FileBackedTaskManager;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId; // ID эпика

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, TaskStatus taskStatus, String description, int epicId) {
        super(id, name, taskStatus, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return startTime != null && duration != null ? startTime.plus(duration) : null;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public static Subtask fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2].equals("null") ? null : parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4].equals("null") ? null : parts[4];
        int epicId = Integer.parseInt(parts[5]);
        return new Subtask(id, title, status, description, epicId);
    }

    @Override
    public void create(FileBackedTaskManager manager) {
    }

    @Override
    public String toString() {
        return String.format("%d,SUBTASK,%s,%s,%s,%d", getId(), getTitle(), getStatus(), getDescription(), epicId);
    }
}
