package tracker.model;

import tracker.model.Status;

import java.util.Objects;

public class Subtask extends Task {

    private int epicId;
    protected TaskType type;

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.type = TaskType.SUBTASK;
        this.epicId = (epicId == id) ? -1 : epicId;
    }

    public Subtask(Subtask other) {
        super(other);
        this.epicId = other.epicId;
    }

    public Subtask(int id, String name, String description, int epicId) {
        this(id, name, description, Status.NEW, epicId);
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "tracker.model.Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        Subtask that = (Subtask) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}