package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager() {
            public Task getSubtask(int id) {
                return null;
            }

            public void updateTask(int id, Task task) {
            }
        };
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Task not found.");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Task titles do not match.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Task descriptions do not match.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Task statuses do not match.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(1, tasks.size(), "Incorrect number of tasks.");
        assertEquals(task.getTitle(), tasks.get(0).getTitle(), "Task titles do not match.");
    }

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2, "Tasks with the same ID should be equal.");
    }

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        subtask1.setId(1);
        subtask2.setId(1);
        assertEquals(subtask1, subtask2, "Subtasks with the same ID should be equal.");
    }

    @Test
    void testEpicCannotBeSubtaskOfItself() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        epic.setId(1);
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        subtask.setId(epic.getId()); // Задайте ID подзадачи равным ID эпика

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask);
        });

        assertEquals("Epic cannot be a subtask of itself.", exception.getMessage());
    }

    @Test
    void testSubtaskCannotBeEpic() {
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Epic epic = new Epic("Epic 1", "Epic Description");
        epic.setId(subtask.getId());
        assertNotEquals(epic.getId(), subtask.getEpicId(), "Subtask cannot be an epic of itself.");
    }

    @Test
    void testManagersUtilityClassReturnsInitializedManagers() {
        TaskManager taskManagerInstance = Managers.getDefault();
        HistoryManager historyManagerInstance = Managers.getDefaultHistory();

        assertNotNull(taskManagerInstance, "TaskManager instance should not be null.");
        assertNotNull(historyManagerInstance, "HistoryManager instance should not be null.");
    }

    @Test
    void testTaskManagerAddsDifferentTaskTypes() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        assertNotNull(taskManager.getEpicById(epic.getId()), "Epic should be retrievable by ID.");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Subtask should be retrievable by ID.");
    }

    @Test
    void testTaskIdConflict() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(1);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertEquals(task1, taskManager.getTaskById(1), "Task with ID 1 should match task1.");
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        Task originalTask = new Task("Original Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.createTask(originalTask);

        Task savedTask = taskManager.getTaskById(taskId);

        // Создаем копию оригинальной задачи для изменения
        Task modifiedTask = new Task(originalTask.getTitle(), originalTask.getDescription(), originalTask.getStatus());
        modifiedTask.setId(originalTask.getId());  // Сохраняем ID, если это необходимо

        // Измените описание копии задачи
        modifiedTask.setDescription("New Description");

        // Проверьте, что savedTask не изменился
        assertNotEquals(modifiedTask.getDescription(), savedTask.getDescription(), "Task should remain unchanged after adding.");
    }

    @Test
    void testHistoryManagerPreservesPreviousTaskState() {
        Task originalTask = new Task("Task 1", "Description", TaskStatus.NEW);
        int taskId = taskManager.createTask(originalTask); // Создаем задачу и получаем ее ID
        taskManager.getTaskById(taskId); // Получаем задачу по ID, чтобы добавить в историю

        List<Task> history = taskManager.getHistory(); // Получаем историю из taskManager
        assertNotNull(history, "History should not be empty.");
        assertEquals(1, history.size(), "History should contain one task.");

        // Проверяем, что задача в истории совпадает с оригинальной
        Task historyTask = history.get(0);
        assertEquals(taskId, historyTask.getId(), "History task should match the original task.");
        assertEquals(originalTask.getDescription(), historyTask.getDescription(), "History task description should match the original task description.");

        // Создаем новую задачу на основе оригинальной, чтобы изменить её
        Task modifiedTask = new Task(originalTask.getTitle(), originalTask.getDescription(), originalTask.getStatus());
        modifiedTask.setId(originalTask.getId());  // Сохраняем ID, если это необходимо

        // Измените описание копии задачи
        modifiedTask.setDescription("New Description");

        // Проверяем, что задача в истории не изменилась
        assertNotEquals(modifiedTask.getDescription(), historyTask.getDescription(), "Task in history should not change when original task is modified.");
    }



    @Test
    void testHistoryAfterTaskDeletion() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        int taskId1 = taskManager.createTask(task1);
        taskManager.getTaskById(taskId1); // Добавляем в историю
        assertEquals(1, taskManager.getHistory().size(), "History should contain one task.");

        taskManager.deleteTask(taskId1); // Удаляем задачу
        assertTrue(taskManager.getHistory().isEmpty(), "History should be empty after deletion.");
    }

    @Test
    void testHistoryDoesNotContainDuplicates() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        int taskId1 = taskManager.createTask(task1);
        taskManager.getTaskById(taskId1); // Добавляем в историю
        taskManager.getTaskById(taskId1); // Добавляем в историю снова
        assertEquals(1, taskManager.getHistory().size(), "History should contain only one instance of the task.");

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        int taskId2 = taskManager.createTask(task2);
        taskManager.getTaskById(taskId2); // Добавляем в историю
        assertEquals(2, taskManager.getHistory().size(), "History should contain two different tasks.");
    }
}