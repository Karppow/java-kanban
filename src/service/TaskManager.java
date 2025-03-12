package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    int createTask(Task task);
    Subtask createSubtask(Subtask subtask);
    Epic createEpic(Epic epic);
    Task getTaskById(Task id);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);
    Epic getEpicById(int id);
    List<Task> getAllTasks();
    List<Subtask> getAllSubtasks();
    List<Epic> getAllEpics();
    void deleteTask(int id);
    void deleteSubtask(Subtask id);

    void deleteSubtask(int id);

    void deleteEpic(int id);
    void updateSubtask(Subtask subtask);
    List<Task> getHistory();
    Task getTask(int id);
    Task getSubtask(Subtask id);
}