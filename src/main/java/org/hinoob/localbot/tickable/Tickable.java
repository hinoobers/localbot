package org.hinoob.localbot.tickable;

import net.dv8tion.jda.api.JDA;

public abstract class Tickable {

    protected JDA jda;

    private int waitTicks;

    public Tickable(JDA jda) {
        this.jda = jda;
    }

    public void tick() {
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        onTick();
    }

    protected void waitSeconds(int seconds) {
        this.waitTicks += seconds * 20;
    }

    protected abstract void onTick();
}
