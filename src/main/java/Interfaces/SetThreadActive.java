package Interfaces;

public class SetThreadActive {
    boolean isActive = false;

    public boolean getActiveStatus() {
        return isActive;
    }

    public void setActiveStatus(boolean active) {
        this.isActive = active;
    }
}
