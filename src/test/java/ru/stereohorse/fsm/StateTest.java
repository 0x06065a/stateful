package ru.stereohorse.fsm;

import org.junit.Test;

public class StateTest {

    @Test
    public void shouldCreateStateByOverrides() {
        new State<Trigger>() {
            @Override
            protected State<Trigger> onTrigger(Trigger trigger) {
                return null;
            }

            @Override
            protected void onEntry(Trigger trigger) {
            }

            @Override
            protected void onExit(Trigger trigger) {
            }

            @Override
            protected void onForceExit(Trigger trigger) {
            }
        };
    }

    @Test
    public void shouldCreateStateBySetters() {
        new State<Trigger>()
                .onTrigger(trigger -> null)
                .onExit(trigger -> {
                })
                .onForceExit(trigger -> {
                })
                .onEntry(trigger -> {
                });
    }


    private static class Trigger {

    }
}