package johnsmith.enchantingoverhauled.util;

public class ScheduledTask {
    public int ticksRemaining;
    public final Runnable task;

    public ScheduledTask(int ticks, Runnable task) {
        this.ticksRemaining = ticks;
        this.task = task;
    }
}