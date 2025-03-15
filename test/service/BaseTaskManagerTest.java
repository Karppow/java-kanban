package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public abstract class BaseTaskManagerTest {
    protected FileBackedTaskManager taskManager;
    protected File testFile;

    @BeforeEach
    void setUp() {
        testFile = new File("test_tasks.csv");
        taskManager = createTaskManager(testFile);
    }

    protected abstract FileBackedTaskManager createTaskManager(File file);

    @AfterEach
    void tearDown() {
        testFile.delete(); // Удаляем временный файл после тестов
    }

    protected int createTask(String title, String description, TaskStatus status) {
        Task task = new Task(title, description, status);
        return taskManager.createTask(task);
    }

    protected int createSubtask(String title, String description, TaskStatus status, int epicId) {
        Subtask subtask = new Subtask(title, description, status, epicId);
        return taskManager.createSubtask(subtask);
    }

    protected int createEpic(String title, String description) {
        Epic epic = new Epic(title, description);
        return taskManager.createEpic(epic).getId();
    }

    protected void deleteTask(int taskId) {
        taskManager.deleteTask(taskId);
    }

    protected void deleteSubtask(int subtaskId) {
        taskManager.deleteSubtask(subtaskId);
    }

    protected void deleteEpic(int epicId) {
        taskManager.deleteEpic(epicId);
    }

    protected void updateSubtask(Subtask subtask) {
        taskManager.updateSubtask(subtask);
    }
}