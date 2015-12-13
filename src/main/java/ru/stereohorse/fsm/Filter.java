package ru.stereohorse.fsm;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiFunction;


@Slf4j
public class Filter<TTrigger> {

    private BiFunction<TTrigger, State<TTrigger>, Result<TTrigger>> onFilterFunction
            = (trigger, state) -> null;


    Result<TTrigger> handleFilter(TTrigger trigger, State<TTrigger> currentState) {
        log.trace("filtering [{}] of [{}]", currentState, trigger);
        return onFilter(trigger, currentState);
    }

    @SuppressWarnings("UnusedParameters")
    protected Result<TTrigger> onFilter(TTrigger trigger, State<TTrigger> currentState) {
        return onFilterFunction.apply(trigger, currentState);
    }


    public Filter<TTrigger> onFilter(BiFunction<TTrigger, State<TTrigger>, Result<TTrigger>> onFilterFunction) {
        this.onFilterFunction = onFilterFunction;
        return this;
    }


    @SuppressWarnings("unused")
    public static <TTrigger> Result<TTrigger> forceState(State<TTrigger> newState) {
        return new Result<TTrigger>().setForcedState(newState);
    }

    @Data
    @Accessors(chain = true)
    public static class Result<TTrigger> {

        private State<TTrigger> forcedState;
    }
}
