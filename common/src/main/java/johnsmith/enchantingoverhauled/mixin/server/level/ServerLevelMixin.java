package johnsmith.enchantingoverhauled.mixin.server.level;

import johnsmith.enchantingoverhauled.util.ScheduledTask;
import johnsmith.enchantingoverhauled.util.WorldScheduler;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements WorldScheduler {

    @Unique
    private final List<ScheduledTask> enchanting_overhauled$scheduledTasks = new ArrayList<>();

    @Override
    public void enchanting_overhauled$schedule(int delayTicks, Runnable task) {
        this.enchanting_overhauled$scheduledTasks.add(new ScheduledTask(delayTicks, task));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void processScheduledTasks(CallbackInfo ci) {
        if (this.enchanting_overhauled$scheduledTasks.isEmpty()) return;

        Iterator<ScheduledTask> iterator = this.enchanting_overhauled$scheduledTasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask entry = iterator.next();
            entry.ticksRemaining--;

            if (entry.ticksRemaining <= 0) {
                entry.task.run();
                iterator.remove();
            }
        }
    }

}