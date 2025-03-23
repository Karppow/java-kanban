package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    int createTask(Task task); // Создает задачу и возвращает её ID
    int createSubtask(Subtask subtask); // Создает подзадачу и возвращает её ID
    int createEpic(Epic epic); // Создает эпик и возвращает его ID

    Task getTaskById(int id); // Получает задачу по ID
    Subtask getSubtaskById(int id); // Получает подзадачу по ID
    Epic getEpicById(int id); // Получает эпик по ID

    List<Task> getAllTasks(); // Получает все задачи
    List<Subtask> getAllSubtasks(); // Получает все подзадачи
    List<Epic> getAllEpics(); // Получает все эпики

    void deleteTask(int id); // Удаляет задачу по ID
    void deleteSubtask(int id); // Удаляет подзадачу по ID
    void deleteEpic(int id); // Удаляет эпик по ID

    void updateSubtask(Subtask subtask); // Обновляет подзадачу
    void updateTask(Task task); // Обновляет задачу по ID
    void updateEpic(Epic epic);

    List<Task> getHistory(); // Получает историю задач
}
