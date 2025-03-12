package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW); // ID не задается, будет установлен в service.TaskManager
    }

    public Epic(int id, String name, TaskStatus taskStatus, String description) {
        super(id, name, taskStatus, description);
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void removeAllSubtasks() {
        subtaskIds.clear();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public static Epic fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2].equals("null") ? null : parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4].equals("null") ? null : parts[4];

        Epic epic = new Epic(id, title, status, description);
        if (parts.length > 5) {
            String[] subtaskIdStrings = parts[5].split(";");
            for (String subtaskIdString : subtaskIdStrings) {
                epic.addSubtask(Integer.parseInt(subtaskIdString));
            }
        }
        return epic;
    }
}
