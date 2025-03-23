package model;

import service.FileBackedTaskManager;
import service.TaskManager;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    private int id;
    protected String name;
    protected String title;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(int id, String name, TaskStatus status, String description, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime; // Устанавливаем время начала
        this.duration = duration; // Устанавливаем длительность
    }

    public Task(int id, String title, TaskStatus status, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    // Геттеры и сеттеры
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void create(FileBackedTaskManager manager) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task)) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%d,TASK,%s,%s,%s,", id, title, status, description);
    }

    public static Task fromString(String value, TaskManager taskManager) {
        String[] fields = value.split(",");

        if (fields.length < 5) {
            return null; // Возвращаем null в случае некорректного формата
        }

        int id = Integer.parseInt(fields[0]);
        String taskType = fields[1];
        String title = fields[2].equals("null") ? null : fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4].equals("null") ? null : fields[4];

        LocalDateTime startTime = null;
        Duration duration = null;
        int epicId = -1; // Для подзадач

        // Проверяем наличие дополнительных полей для времени начала и длительности
        if (fields.length > 5) {
            if (!fields[5].equals("null")) {
                try {
                    startTime = LocalDateTime.parse(fields[5]);
                } catch (Exception e) {
                }
            }
        }

        if (fields.length > 6) {
            if (!fields[6].equals("null")) {
                try {
                    duration = Duration.parse(fields[6]);
                } catch (Exception e) {
                }
            }
        }

        if (fields.length > 7 && "SUBTASK".equals(taskType)) {
            try {
                epicId = Integer.parseInt(fields[7]); // Читаем ID эпика только для подзадач
            } catch (NumberFormatException e) {
            }
        }

        // Проверяем, что для подзадач указаны время начала и длительность
        if ("SUBTASK".equals(taskType) && (startTime == null || duration == null)) {
            return null; // Возвращаем null в случае отсутствия необходимых полей
        }

        Task task;

        if ("EPIC".equals(taskType)) {
            task = new Epic(id, title, status, description, null);
        } else if ("SUBTASK".equals(taskType)) {
            task = new Subtask(id, title, status, description, epicId);
            task.setStartTime(startTime); // Устанавливаем время начала
            task.setDuration(duration); // Устанавливаем длительность
        } else {
                task = new Task(id, title, status, description);
            }

            // Устанавливаем время начала и длительность для обычных задач
            if (startTime != null && duration != null) {
                task.setStartTime(startTime);
                task.setDuration(duration);
            }
            return task;
        }

            public boolean isOverlapping(Task other) {
        LocalDateTime thisStart = this.getStartTime();
        LocalDateTime thisEnd = this.getEndTime();
        LocalDateTime otherStart = other.getStartTime();
        LocalDateTime otherEnd = other.getEndTime();
        return (thisStart != null && otherStart != null) &&
                (thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd));
    }
}