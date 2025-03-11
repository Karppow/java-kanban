package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile(file); // Загружаем данные из файла при создании менеджера
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,name,status,description,epic\n");

        for (Task task : getAllTasks()) {
            sb.append(toString(task)).append("\n");
        }
        for (Epic epic : getAllEpics()) {
            sb.append(toString(epic)).append("\n");
        }
        for (Subtask subtask : getAllSubtasks()) {
            sb.append(toString(subtask)).append("\n");
        }

        try {
            Files.writeString(file.toPath(), sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + e.getMessage());
        }
    }

    private String toString(Task task) {
        if (task instanceof Subtask) {
            return String.format("%d,SUBTASK,%s,%s,%s,%d", task.getId(), task.getTitle(), task.getStatus(), task.getDescription(), ((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,", task.getId(), task.getTitle(), task.getStatus(), task.getDescription());
        } else {
            return String.format("%d,TASK,%s,%s,%s,", task.getId(), task.getTitle(), task.getStatus(), task.getDescription());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines.subList(1, lines.size())) { // Пропускаем заголовок
                Task task = fromString(line);
                if (task != null) {
                    if (task instanceof Subtask) {
                        manager.createSubtask((Subtask) task);
                    } else if (task instanceof Epic) {
                        manager.createEpic((Epic) task);
                    } else {
                        manager.createTask(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }
        return manager;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        if ("TASK".equals(type)) {
            Task task = new Task(name, description, status);
            task.setId(id);
            return task;
        } else if ("EPIC".equals(type)) {
            Epic epic = new Epic(name, description);
            epic.setId(id);
            return epic;
        } else if ("SUBTASK".equals(type)) {
            int epicId = Integer.parseInt(parts[5]);
            Subtask subtask = new Subtask(name, description, status, epicId);
            subtask.setId(id);
            return subtask;
        }
        return null; // Если тип не распознан, возвращаем null
    }
}

