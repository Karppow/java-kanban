package model;

public class Subtask extends Task {
    private final int epicId; // Изменяем тип на int

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status); // ID не задается, будет установлен в service.TaskManager
        this.epicId = epicId; // Сохраняем переданный ID эпика
    }

    public Subtask(int id, String name, TaskStatus taskStatus, String description, int epicId) {
        super(id, name, taskStatus, description);
        this.epicId = epicId; // Сохраняем переданный ID эпика
    }

    public int getEpicId() {
        return epicId; // Возвращаем ID эпика
    }

    public static Subtask fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2].equals("null") ? null : parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4].equals("null") ? null : parts[4];
        int epicId = Integer.parseInt(parts[5]); // Получаем ID эпика из строки
        return new Subtask(id, title, status, description, epicId); // Создаем и возвращаем подзадачу
    }
}
