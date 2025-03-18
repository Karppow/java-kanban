package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected abstract T createTaskManager();

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    public void testCreateTask() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now()); // Установите время начала
        task.setDuration(Duration.ofHours(1)); // Установите длительность
        int taskId = taskManager.createTask(task);
        assertNotNull(taskManager.getTaskById(taskId), "Task should be created successfully.");
    }

    @Test
    public void testCreateSubtask() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);
        assertNotNull(taskManager.getSubtaskById(subtaskId));
        assertEquals(epicId, taskManager.getSubtaskById(subtaskId).getEpicId());
    }

    @Test
    public void testCreateEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        assertNotNull(taskManager.getEpicById(epicId));
    }

    @Test
    public void testEpicStatusAllNew() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(1));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId);
        subtask2.setStartTime(LocalDateTime.now().plusHours(1)); // Устанавливаем время начала позже
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    public void testEpicStatusAllDone() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.DONE, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.DONE, epicId);
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(1));
        subtask2.setStartTime(LocalDateTime.now().plusHours(1));
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    public void testEpicStatusMixed() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.DONE, epicId);
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(1));
        subtask2.setStartTime(LocalDateTime.now().plusHours(1));
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    public void testCreateTaskWithOverlappingTime() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(2));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.now().plusHours(1)); // Перекрытие с task1
        task2.setDuration(Duration.ofHours(2));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertEquals("New task overlaps with existing tasks.", exception.getMessage());
    }

    @Test
    public void testCreateSubtaskWithOverlappingTime() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(2));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId);
        subtask2.setStartTime(LocalDateTime.now().plusHours(1)); // Перекрытие с subtask1
        subtask2.setDuration(Duration.ofHours(2));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask2);
        });
        assertEquals("New subtask overlaps with existing tasks.", exception.getMessage());
    }

    @Test
    public void testGetAllTasks() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.now().plusHours(2));
        task2.setDuration(Duration.ofHours(1));
        taskManager.createTask(task2);

        assertEquals(2, taskManager.getAllTasks().size());
    }

    @Test
    public void testGetAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Description");
        taskManager.createEpic(epic2);

        assertEquals(2, taskManager.getAllEpics().size());
    }

    @Test
    public void testGetAllSubtasks() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId);
        subtask2.setStartTime(LocalDateTime.now().plusHours(1));
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask2);

        assertEquals(2, taskManager.getAllSubtasks().size());
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task("Task to delete", "Description", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);

        assertNotNull(taskManager.getTaskById(taskId));

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTaskById(taskId));
    }

    @Test
    public void testDeleteEpic() {
        Epic epic = new Epic("Epic to delete", "Description");
        int epicId = taskManager.createEpic(epic);

        assertNotNull(taskManager.getEpicById(epicId));

        taskManager.deleteEpic(epicId);

        assertNull(taskManager.getEpicById(epicId));
    }

    @Test
    public void testDeleteSubtask() {
        Epic epic = new Epic("Epic with subtask", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask to delete", "Description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);

        assertNotNull(taskManager.getSubtaskById(subtaskId));

        taskManager.deleteSubtask(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task("Task to update", "Description", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);

        task.setTitle("Updated Task");
        taskManager.updateTask(taskId, task); // Передаем taskId как первый аргумент

        assertEquals("Updated Task", taskManager.getTaskById(taskId).getTitle());
    }

    @Test
    public void testUpdateSubtask() {
        Epic epic = new Epic("Epic for updating subtask", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask to update", "Description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);

        subtask.setTitle("Updated Subtask");
        taskManager.updateSubtask(subtask); // Обновляем подзадачу

        assertEquals("Updated Subtask", taskManager.getSubtaskById(subtaskId).getTitle());
    }

    @Test
    public void testUpdateEpic() {
        Epic epic = new Epic("Epic to update", "Description");
        int epicId = taskManager.createEpic(epic);

        epic.setTitle("Updated Epic");
        taskManager.updateEpic(epicId, epic); // Обновляем эпик

        assertEquals("Updated Epic", taskManager.getEpicById(epicId).getTitle());
    }

    @Test
    public void testGetTaskById() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        int taskId = taskManager.createTask(task);

        assertEquals(task, taskManager.getTaskById(taskId));
    }

    @Test
    public void testGetSubtaskById() {
        Epic epic = new Epic("Epic for subtask", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(subtask, taskManager.getSubtaskById(subtaskId));
    }

    @Test
    public void testGetEpicById() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        assertEquals(epic, taskManager.getEpicById(epicId));
    }
}
