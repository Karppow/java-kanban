import java.util.List;

public interface TaskManager {
    int createTask(Task task);
    Subtask createSubtask(Subtask subtask);
    Epic createEpic(Epic epic);
    Task getTaskById(int id);
    Subtask getSubtaskById(int id);
    Epic getEpicById(int id);
    List<Task> getAllTasks();
    List<Subtask> getAllSubtasks();
    List<Epic> getAllEpics();
    void deleteTask(int id);
    void deleteSubtask(int id);
    void deleteEpic(int id);
    List<Task> getHistory(); // Новый метод для получения истории
}
