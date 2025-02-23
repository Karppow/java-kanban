package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status); // ID не задается, будет установлен в service.TaskManager
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}