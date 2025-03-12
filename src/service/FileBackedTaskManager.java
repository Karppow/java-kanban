package service;

import exceptions.ManagerReadFileException;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
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
        try {
            super.deleteTask(id);
            save();
        } catch (ManagerSaveException e) {
            // Логируем или обрабатываем исключение
            System.err.println("Ошибка при сохранении после удаления задачи: " + e.getMessage());
        }
    }

    @Override
    public void deleteSubtask(Subtask id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public List<Task> getHistory() {
        return List.of();
    }

    @Override
    public Task getTask(int id) {
        return null;
    }

    @Override
    public Task getSubtask(Subtask id) {
        return null;
    }

    @Override
    public void updateTask(int id, Task task) {
        save();
    }

    void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(formatTask(task));
            }
            for (Epic epic : getAllEpics()) {
                writer.write(formatEpic(epic));
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(formatSubtask(subtask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных", e);
        }
    }

    private String formatTask(Task task) {
        return String.format("%d,TASK,%s,%s,%s,\n", task.getId(), task.getTitle(), task.getStatus(), task.getDescription());
    }

    private String formatEpic(Epic epic) {
        return String.format("%d,EPIC,%s,%s,%s,\n", epic.getId(), epic.getTitle(), epic.getStatus(), epic.getDescription());
    }

    private String formatSubtask(Subtask subtask) {
        return String.format("%d,SUBTASK,%s,%s,%s,%d\n", subtask.getId(), subtask.getTitle(), subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.exists()) {
            return new FileBackedTaskManager(file); // Возвращаем новый менеджер, если файл не существует
        }
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines.subList(1, lines.size())) { // Пропускаем заголовок
                Task task = Task.fromString(line);
                if (task instanceof Subtask) {
                    manager.createSubtask((Subtask) task);
                } else if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else {
                    manager.createTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerReadFileException("Ошибка загрузки данных", e);
        }
        return manager;
    }
}
