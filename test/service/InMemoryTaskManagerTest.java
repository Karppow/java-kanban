package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private InMemoryTaskManager taskManager;

    @Override
    protected InMemoryTaskManager createTaskManager() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        assertNotNull(manager, "Created TaskManager should not be null.");
        return manager;
    }

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager(); // Initialize taskManager
    }

    @Test
    public void testCreateTask() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.now()); // Set start time
        task.setDuration(Duration.ofHours(1)); // Set duration
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
        subtask2.setStartTime(LocalDateTime.now().plusHours(1)); // Set start time later
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
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofHours(1));

        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.DONE, epicId);
        subtask2.setStartTime(LocalDateTime.now().plusHours(1)); // Ensure this is set
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
        task2.setStartTime(LocalDateTime.now().plusHours(1)); // Overlaps with task1
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
        subtask2.setStartTime(LocalDateTime.now().plusHours(3)); // Убедитесь, что время не пересекается
        subtask2.setDuration(Duration.ofHours(2));

        // Теперь subtask2 не должно вызывать исключение
        int subtaskId = taskManager.createSubtask(subtask2);
        assertNotNull(taskManager.getSubtaskById(subtaskId));
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

        // Обновляем задачу с новым временем, которое не пересекается
        task.setStartTime(LocalDateTime.now().plusHours(2)); // Установите новое время, чтобы избежать пересечения
        taskManager.updateTask(task); // Обновляем задачу

        assertEquals("Task to update", taskManager.getTaskById(taskId).getTitle());
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
        taskManager.updateSubtask(subtask); // Update the subtask

        assertEquals("Updated Subtask", taskManager.getSubtaskById(subtaskId).getTitle());
    }


    @Test
    public void testUpdateEpic() {
        Epic epic = new Epic("Epic to update", "Description");
        int epicId = taskManager.createEpic(epic);

        epic.setTitle("Updated Epic");
        taskManager.updateEpic(epic); // Update the epic

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

    @Test
    public void testHistoryManagerPreservesPreviousTaskState() {
        Task originalTask = new Task("Task 1", "Description", TaskStatus.NEW);
        originalTask.setStartTime(LocalDateTime.now()); // Ensure start time is set
        originalTask.setDuration(Duration.ofHours(1)); // Ensure duration is set
        int taskId = taskManager.createTask(originalTask);
        taskManager.getTaskById(taskId); // Add to history

        List<Task> history = taskManager.getHistory(); // Get history from taskManager
        assertNotNull(history, "History should not be empty.");
        assertEquals(1, history.size(), "History should contain one task.");

        Task historyTask = history.get(0);
        assertEquals(taskId, historyTask.getId(), "History task should match the original task.");
        assertEquals(originalTask.getDescription(), historyTask.getDescription(), "History task description should match the original task description.");
    }

    @Test
    public void testHistoryAfterTaskDeletion() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now()); // Ensure start time is set
        int taskId1 = taskManager.createTask(task1);
        taskManager.getTaskById(taskId1); // Add to history
        assertEquals(1, taskManager.getHistory().size(), "History should contain one task.");

        taskManager.deleteTask(taskId1); // Delete the task

        assertEquals(0, taskManager.getHistory().size(), "History should be empty after deletion.");
    }

    @Test
    public void testHistoryAfterEpicDeletion() {
        Epic epic = new Epic("Epic for history", "Description");
        int epicId = taskManager.createEpic(epic);
        taskManager.getEpicById(epicId); // Add to history

        assertEquals(1, taskManager.getHistory().size(), "History should contain one epic.");

        taskManager.deleteEpic(epicId); // Delete the epic

        assertEquals(0, taskManager.getHistory().size(), "History should be empty after deletion.");
    }

    @Test
    public void testHistoryAfterSubtaskDeletion() {
        Epic epic = new Epic("Epic for subtask history", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask for history", "Description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now()); // Ensure start time is set
        int subtaskId = taskManager.createSubtask(subtask);
        taskManager.getSubtaskById(subtaskId); // Add to history

        assertEquals(1, taskManager.getHistory().size(), "History should contain one subtask.");

        taskManager.deleteSubtask(subtaskId); // Delete the subtask

        assertEquals(0, taskManager.getHistory().size(), "History should be empty after deletion.");
    }

    @Test
    public void testGetHistoryReturnsCorrectOrder() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now()); // Ensure start time is set
        task1.setDuration(Duration.ofHours(1)); // Ensure duration is set
        int taskId1 = taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.now().plusHours(2)); // Ensure start time is set
        task2.setDuration(Duration.ofHours(1)); // Ensure duration is set
        int taskId2 = taskManager.createTask(task2);

        taskManager.getTaskById(taskId1); // Add to history
        taskManager.getTaskById(taskId2); // Add to history

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "History should contain two tasks.");
        assertEquals(taskId1, history.get(0).getId(), "First in history should be Task 1.");
        assertEquals(taskId2, history.get(1).getId(), "Second in history should be Task 2.");
    }

    // Метод для получения специального экземпляра InMemoryTaskManager для тестирования
    public static InMemoryTaskManager getTestInstance() {
        return new InMemoryTaskManager() {
            @Override
            public Task getTaskById(int id) {
                return new Task(id, "Test Task", TaskStatus.NEW, "This is a test task");
            }

            @Override
            public Subtask getSubtaskById(int id) {
                return new Subtask(id, "Test Subtask", TaskStatus.NEW, "This is a test subtask", 1);
            }

            @Override
            public void updateTask(Task task) {
                System.out.println("Updating task: " + task);
            }
        };
    }
}

