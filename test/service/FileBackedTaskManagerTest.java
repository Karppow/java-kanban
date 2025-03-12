package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private File testFile;
    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        testFile = new File("test_tasks.csv");
        taskManager = new FileBackedTaskManager(testFile);
    }

    @AfterEach
    void tearDown() {
        testFile.delete(); // Удаляем временный файл после тестов
    }

    @Test
    void createTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        assertNotEquals(0, taskId);
        assertEquals(task.getTitle(), taskManager.getTaskById(taskId).getTitle());
    }

    @Test
    void createSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic.getId());
        int subtaskId = taskManager.createSubtask(subtask); // Corrected to int

        assertNotEquals(0, subtaskId);
        assertEquals(subtask.getTitle(), taskManager.getSubtaskById(subtaskId).getTitle());
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = taskManager.createEpic(epic).getId();

        assertNotEquals(0, epicId);
        assertEquals(epic.getTitle(), taskManager.getEpicById(epicId).getTitle());
    }

    @Test
    void deleteTask() {
        Task task = new Task("Задача для удаления", "Описание задачи", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        taskManager.deleteTask(taskId);
        assertNull(taskManager.getTaskById(taskId));
    }

    @Test
    void deleteSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Эпик для удаления", "Описание эпика"));
        Subtask subtask = new Subtask("Подзадача для удаления", "Описание подзадачи", TaskStatus.NEW, epic.getId());
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteSubtask(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void deleteEpic() {
        Epic epic = new Epic("Эпик для удаления", "Описание эпика");
        int epicId = taskManager.createEpic(epic).getId();

        taskManager.deleteEpic(epicId);
        assertNull(taskManager.getEpicById(epicId));
    }

    @Test
    void updateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Эпик для обновления", "Описание эпика"));
        Subtask subtask = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epic.getId());
        int subtaskId = taskManager.createSubtask(subtask);

        subtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);

        assertEquals(TaskStatus.DONE, taskManager.getSubtaskById(subtaskId).getStatus());
    }

    @Test
    void getHistory() {
        Task task = new Task("История задачи", "Описание", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        // Доступ к задаче, чтобы убедиться, что она добавлена в историю
        taskManager.getTaskById(taskId);

        assertEquals(1, taskManager.getHistory().size());
        assertEquals(taskId, taskManager.getHistory().get(0).getId());
    }

    @Test
    void save() {
        Task task = new Task("Сохранить задачу", "Описание", TaskStatus.NEW);
        taskManager.createTask(task);

        taskManager.save(); // Явно вызываем сохранение
        assertTrue(testFile.exists());
    }

    @Test
    void loadFromFile() {
        // Создаем и сохраняем задачу в файл
        Task task = new Task("Задача для загрузки", "Описание задачи", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        taskManager.save(); // Сохраняем задачи в файл

        // Создаем новый менеджер задач, который будет загружать данные из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что задача была загружена корректно
        Task loadedTask = loadedManager.getTaskById(taskId); // Используем taskId
        System.out.println("Loaded task: " + loadedTask); // Отладочное сообщение
        Assertions.assertNotNull(loadedTask, "Loaded task should not be null");
        assertEquals(task.getTitle(), loadedTask.getTitle());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
    }
}

