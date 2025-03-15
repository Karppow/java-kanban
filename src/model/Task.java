package model;

import service.FileBackedTaskManager;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String title, TaskStatus status, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
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

    public static Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) {
            System.out.println("Ignoring invalid task line: " + value);
            return null; // Возвращаем null в случае некорректного формата
        }

        int id = Integer.parseInt(fields[0]);
        String taskType = fields[1]; // Получаем тип задачи
        String title = fields[2].equals("null") ? null : fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4].equals("null") ? null : fields[4];

        Task task;

        // Создаем экземпляр соответствующего подкласса
        if ("EPIC".equals(taskType)) {
            task = new Epic(id, title, status, description);
        } else if ("SUBTASK".equals(taskType)) {
            int epicId = Integer.parseInt(fields[5]); // Предположим, что ID эпика хранится в fields[5]
            task = new Subtask(id, title, status, description, epicId);
        } else if ("TASK".equals(taskType)) {
            task = new Task(id, title, status, description);
        } else {
            System.out.println("Unknown task type: " + taskType);
            return null; // Или выбросьте исключение, если тип задачи не поддерживается
        }

        return task;
    }
}