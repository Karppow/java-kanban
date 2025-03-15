package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends BaseTaskManagerTest {

    @Override
    protected FileBackedTaskManager createTaskManager(File file) {
        return new FileBackedTaskManager(file);
    }

    @Test
    void createTask() {
        int taskId = createTask("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        assertNotEquals(0, taskId);
        assertEquals("Задача 1", taskManager.getTaskById(taskId).getTitle());
    }

    @Test
    void createSubtask() {
        int epicId = createEpic("Эпик 1", "Описание эпика 1");
        int subtaskId = createSubtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epicId);
        assertNotEquals(0, subtaskId);
        assertEquals("Подзадача 1", taskManager.getSubtaskById(subtaskId).getTitle());
    }

    @Test
    void createEpic() {
        int epicId = createEpic("Эпик 1", "Описание эпика 1");
        assertNotEquals(0, epicId);
        assertEquals("Эпик 1", taskManager.getEpicById(epicId).getTitle());
    }

    @Test
    void deleteTask() {
        int taskId = createTask("Задача для удаления", "Описание задачи", TaskStatus.NEW);
        deleteTask(taskId);
        assertNull(taskManager.getTaskById(taskId));
    }

    @Test
    void deleteSubtask() {
        int epicId = createEpic("Эпик для удаления", "Описание эпика");
        int subtaskId = createSubtask("Подзадача для удаления", "Описание подзадачи", TaskStatus.NEW, epicId);
        deleteSubtask(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void deleteEpic() {
        int epicId = createEpic("Эпик для удаления", "Описание эпика");
        deleteEpic(epicId);
        assertNull(taskManager.getEpicById(epicId));
    }

    @Test
    void updateSubtask() {
        int epicId = createEpic("Эпик для обновления", "Описание эпика");
        int subtaskId = createSubtask("Подзадача", "Описание", TaskStatus.NEW, epicId);

        Subtask subtaskToUpdate = taskManager.getSubtaskById(subtaskId);
        subtaskToUpdate.setStatus(TaskStatus.DONE);
        updateSubtask(subtaskToUpdate);

        assertEquals(TaskStatus.DONE, taskManager.getSubtaskById(subtaskId).getStatus());
    }

    @Test
    void getHistory() {
        int taskId = createTask("История задачи", "Описание", TaskStatus.NEW);
        taskManager.getTaskById(taskId); // Доступ к задаче, чтобы убедиться, что она добавлена в историю

        assertEquals(1, taskManager.getHistory().size());
        assertEquals(taskId, taskManager.getHistory().get(0).getId());
    }

    @Test
    void save() {
        createTask("Сохранить задачу", "Описание", TaskStatus.NEW);
        taskManager.save(); // Явно вызываем сохранение
        assertTrue(testFile.exists());
    }

    @Test
    void loadFromFile() {
        // Создаем и сохраняем задачу в файл
        int taskId = createTask("Задача для загрузки", "Описание задачи", TaskStatus.NEW);
        taskManager.save(); // Сохраняем задачи в файл

        // Создаем новый менеджер задач, который будет загружать данные из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что задача была загружена корректно
        Task loadedTask = loadedManager.getTaskById(taskId);
        Assertions.assertNotNull(loadedTask, "Loaded task should not be null");
        assertEquals("Задача для загрузки", loadedTask.getTitle());
        assertEquals("Описание задачи", loadedTask.getDescription());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());
    }
}