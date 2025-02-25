package service;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testAddTaskToHistory() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = 1; // Предполагаем, что ID задачи равен 1
        task.setId(taskId);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "History should not be empty.");
        assertEquals(1, history.size(), "History should contain one task.");
        assertEquals(taskId, history.get(0).getId(), "The task in history should match the added task.");
    }

    @Test
    void testHistoryDoesNotContainDuplicates() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = 1; // Задаем ID задачи
        task.setId(taskId);

        historyManager.add(task);
        historyManager.add(task); // Добавляем ту же задачу снова

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain only one instance of the task.");
    }

    @Test
    void testRemoveTaskFromHistory() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = 1; // Задаем ID задачи
        task.setId(taskId);

        historyManager.add(task);
        historyManager.remove(taskId); // Удаляем задачу из истории

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty after task removal.");
    }

    @Test
    void testHistoryPreservesOrder() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain two tasks.");
        assertEquals(1, history.get(0).getId(), "First task in history should be task1.");
        assertEquals(2, history.get(1).getId(), "Second task in history should be task2.");
    }

    @Test
    void testHistoryAfterTaskModification() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task.setId(1);

        Task historyTask = new Task(task.getTitle(), task.getDescription(), task.getStatus());
        historyTask.setId(task.getId());
        historyManager.add(historyTask);

        task.setDescription("New Description");

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should still contain one task.");
        assertNotEquals(task.getDescription(), history.get(0).getDescription(), "Task in history should not change when original task is modified.");
    }

    @Test
    void testHistoryAfterTaskDeletion() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task.setId(1);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "History should contain one task.");

        historyManager.remove(task.getId()); // Удаляем задачу из истории
        assertTrue(historyManager.getHistory().isEmpty(), "History should be empty after deletion.");
    }
}