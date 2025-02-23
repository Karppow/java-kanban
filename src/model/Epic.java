import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW); // ID не задается, будет установлен в TaskManager
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void removeAllSubtasks() {
        subtaskIds.clear();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }
}
