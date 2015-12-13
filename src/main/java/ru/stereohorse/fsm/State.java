package ru.stereohorse.fsm;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * FSM state.
 * <p>
 * Can be constructed by overriding methods (e.g. {@link #onTrigger(Object)})
 * or providing callbacks (like {@link #onTrigger(Function)})
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class State<TTrigger> {

    private Function<TTrigger, State<TTrigger>> onTriggerFunction = trigger -> null;

    private Consumer<TTrigger> onEntryConsumer = trigger -> {
    };

    private Consumer<TTrigger> onExitConsumer = trigger -> {
    };

    private Consumer<TTrigger> onForceExitConsumer = trigger -> {
    };


    State<TTrigger> handleTrigger(TTrigger trigger) {
        log.debug("== [{}] handling [{}]", this, trigger);
        return onTrigger(trigger);
    }

    void handleEntry(TTrigger trigger) {
        log.debug("-> [{}] entering with [{}]", this, trigger);
        onEntry(trigger);
    }

    void handleExit(TTrigger trigger) {
        log.debug("<- [{}] exiting with [{}]", this, trigger);
        onExit(trigger);
    }

    void handleForceExit(TTrigger trigger) {
        log.debug("<< [{}] force exiting with [{}]", this, trigger);
        onForceExit(trigger);
    }


    protected State<TTrigger> onTrigger(TTrigger trigger) {
        return onTriggerFunction.apply(trigger);
    }

    protected void onEntry(TTrigger trigger) {
        onEntryConsumer.accept(trigger);
    }

    protected void onExit(TTrigger trigger) {
        onExitConsumer.accept(trigger);
    }

    protected void onForceExit(TTrigger trigger) {
        onForceExitConsumer.accept(trigger);
    }

    protected List<Filter<TTrigger>> transformFilters(List<Filter<TTrigger>> filters) {
        return filters;
    }


    public final State<TTrigger> onTrigger(Function<TTrigger, State<TTrigger>> onTriggerFunction) {
        this.onTriggerFunction = onTriggerFunction;
        return this;
    }

    public final State<TTrigger> onEntry(Consumer<TTrigger> onEntryConsumer) {
        this.onEntryConsumer = onEntryConsumer;
        return this;
    }

    public final State<TTrigger> onExit(Consumer<TTrigger> onExitConsumer) {
        this.onExitConsumer = onExitConsumer;
        return this;
    }

    public final State<TTrigger> onForceExit(Consumer<TTrigger> onForceExitConsumer) {
        this.onForceExitConsumer= onForceExitConsumer;
        return this;
    }


    @Override
    public String toString() {
        String hashCode = Integer.toHexString(hashCode()).toUpperCase();

        return Optional.of(getClass().getSimpleName())
                .filter(name -> !name.isEmpty())
                .map(name -> String.format("%s %s", name, hashCode))
                .orElse(hashCode);
    }
}
