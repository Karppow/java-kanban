package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File testFile = new File("testFile.csv"); // Замените на правильный путь к тестовому файлу

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(testFile);
    }

    @Test
    void createTask() {
        // Создаем объект Task
        Task task = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));

        // Теперь вызываем createTask с объектом Task
        int taskId = taskManager.createTask(task);

        // Проверяем, что задача была создана
        assertNotEquals(0, taskId);
        assertEquals("Задача 1", taskManager.getTaskById(taskId).getTitle());
    }

    @Test
    void createSubtask() {
        // Создаем эпик
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = taskManager.createEpic(epic);

        // Создаем объект Subtask
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));

        // Теперь вызываем createSubtask с объектом Subtask
        int subtaskId = taskManager.createSubtask(subtask);

        // Проверяем, что подзадача была создана
        assertNotEquals(0, subtaskId);
        assertEquals("Подзадача 1", taskManager.getSubtaskById(subtaskId).getTitle());
    }

    @Test
    void createEpic() {
        // Создаем объект Epic
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");

        // Теперь вызываем createEpic с объектом Epic
        int epicId = taskManager.createEpic(epic);

        // Проверяем, что эпик был создан
        assertNotEquals(0, epicId);
        assertEquals("Эпик 1", taskManager.getEpicById(epicId).getTitle());
    }

    @Test
    void deleteTask() {
        // Создаем задачу для удаления
        Task task = new Task("Задача для удаления", "Описание задачи", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);

        // Удаляем задачу
        taskManager.deleteTask(taskId);
        assertNull(taskManager.getTaskById(taskId));
    }

    @Test
    void deleteSubtask() {
        // Создаем эпик
        Epic epic = new Epic("Эпик для удаления", "Описание эпика");
        int epicId = taskManager.createEpic(epic);

        // Создаем подзадачу для удаления
        Subtask subtask = new Subtask("Подзадача для удаления", "Описание подзадачи", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);

        // Удаляем подзадачу
        taskManager.deleteSubtask(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void deleteEpic() {
        // Создаем эпик для удаления
        Epic epic = new Epic("Эпик для удаления", "Описание эпика");
        int epicId = taskManager.createEpic(epic);

        // Удаляем эпик
        taskManager.deleteEpic(epicId);
        assertNull(taskManager.getEpicById(epicId));
    }

    @Test
    void updateSubtask() {
        // Создаем эпик для обновления
        Epic epic = new Epic("Эпик для обновления", "Описание эпика");
        int epicId = taskManager.createEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);

        // Обновляем статус подзадачи
        Subtask subtaskToUpdate = taskManager.getSubtaskById(subtaskId);
        subtaskToUpdate.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtaskToUpdate);

        // Проверяем, что статус обновился
        assertEquals(TaskStatus.DONE, taskManager.getSubtaskById(subtaskId).getStatus());
    }

    @Test
    void getHistory() {
        // Создаем задачу для истории
        Task task = new Task("История задачи", "Описание", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);

        // Получаем задачу, чтобы она добавилась в историю
        taskManager.getTaskById(taskId);

        // Проверяем, что история содержит одну задачу
        assertEquals(1, taskManager.getHistory().size());
        assertEquals(taskId, taskManager.getHistory().get(0).getId());
    }

    @Test
    void save() {
        // Создаем задачу для сохранения
        Task task = new Task("Сохранить задачу", "Описание", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        taskManager.createTask(task);

        // Явно вызываем сохранение
        taskManager.save();
        assertTrue(testFile.exists());
    }

    @Test
    void loadFromFile() {
        // Создаем и сохраняем задачу в файл
        Task task = new Task("Задача для загрузки", "Описание задачи", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);
        taskManager.save(); // Сохраняем задачи в файл

        // Загружаем задачи из файла с помощью метода loadFromFile
        FileBackedTaskManager newTaskManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что задача была загружена правильно
        Task loadedTask = newTaskManager.getTaskById(taskId);
        assertNotNull(loadedTask);
        assertEquals("Задача для загрузки", loadedTask.getTitle());
        assertEquals("Описание задачи", loadedTask.getDescription());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());
    }
}
