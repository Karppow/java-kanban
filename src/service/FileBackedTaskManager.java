package service;

import exceptions.ManagerReadFileException;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private int maxId = 0; // Счетчик максимальных идентификаторов

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int createTask(Task task) {
        task.setId(++maxId); // Устанавливаем уникальный идентификатор
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        subtask.setId(++maxId); // Устанавливаем уникальный идентификатор
        int createdSubtaskId = super.createSubtask(subtask);
        save();
        return createdSubtaskId;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++maxId); // Устанавливаем уникальный идентификатор
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

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public Task getTask(int id) {
        return super.getTaskById(id);
    }

    public Subtask getSubtask(int id) {
        return super.getSubtaskById(id);
    }

    public void updateTask(int id, Task task) {
        save();
    }

    void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
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
        return String.format("%d,SUBTASK,%s,%s,%s,%d\n",
                subtask.getId(),
                subtask.getTitle(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getEpicId());
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.exists()) {
            return new FileBackedTaskManager(file);
        }
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id,type,name,status,description,epic")) {
                    continue; // Пропускаем заголовок
                }
                Task task = Task.fromString(line);
                if (task != null) {
                    manager.createTask(task); // Создаем задачу в менеджере
                    if (task.getId() > manager.maxId) {
                        manager.maxId = task.getId(); // Обновляем maxId
                    }
                } else {
                    System.out.println("Ignoring invalid task line: " + line);
                }
            }
        } catch (IOException e) {
            throw new ManagerReadFileException("Ошибка загрузки данных", e);
        }
        return manager;
    }
}