package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Optional;

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

    boolean deleteTask(int id); // Удаляет задачу по ID
    void deleteSubtask(int id); // Удаляет подзадачу по ID
    void deleteEpic(int id); // Удаляет эпик по ID


    Optional<Task> getTaskById(Integer taskId);
    Optional<Epic> getEpicById(Integer epicId);
    Optional<Subtask> getSubtaskById(Integer subtaskId);

    void updateSubtask(Subtask subtask); // Обновляет подзадачу
    boolean updateTask(Task task); // Обновляет задачу по ID
    void updateEpic(Epic epic);

    List<Task> getHistory(); // Получает историю задач
    List<Task> getPrioritizedTasks();
    List<Subtask> getSubtasksByEpicId(int epicId);

    boolean validateTask(Task task);
    void addToHistory(int taskId);
}
