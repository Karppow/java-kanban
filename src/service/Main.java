package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);

        Task task1 = new Task("Переезд", "Организовать переезд на новую квартиру", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Купить мебель", "Купить новую мебель для квартиры", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.now().plusDays(1));
        task2.setDuration(Duration.ofHours(2));
        taskManager.createTask(task2);

        int epic1 = taskManager.createEpic(new Epic("Организация праздника", "Подготовить семейный праздник"));
        Subtask subtask1 = new Subtask("Пригласить гостей", "Составить список гостей", TaskStatus.NEW, epic1);
        subtask1.setStartTime(LocalDateTime.now().plusHours(2));
        subtask1.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Купить торт", "Заказать торт для праздника", TaskStatus.NEW, epic1);
        subtask2.setStartTime(LocalDateTime.now().plusDays(1).plusHours(2));
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask2);

        int epic2 = taskManager.createEpic(new Epic("Покупка квартиры", "Подготовить документы для покупки квартиры"));
        Subtask subtask3 = new Subtask("Собрать документы", "Собрать все необходимые документы", TaskStatus.NEW, epic2);
        subtask3.setStartTime(LocalDateTime.now().plusDays(2));
        subtask3.setDuration(Duration.ofHours(3));
        taskManager.createSubtask(subtask3);

        System.out.println("Все задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        System.out.println("Все подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
        System.out.println("Все эпики:");
        taskManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));

        Subtask subtask1Obj = taskManager.getSubtaskById(subtask1.getId());
        if (subtask1Obj != null) {
            subtask1Obj.setStatus(TaskStatus.DONE);
            taskManager.updateSubtask(subtask1Obj);
        }

        taskManager.deleteSubtask(subtask1.getId());
        System.out.println("После удаления подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));

        taskManager.deleteTask(task1.getId());
        System.out.println("После удаления задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println("Загруженные задачи после восстановления:");
        loadedManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        loadedManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));
        loadedManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
    }
}
