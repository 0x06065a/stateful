package ru.stereohorse.fsm;


import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;


@Slf4j
public class StateMachine<TTrigger> {

    private List<Filter<TTrigger>> filters;
    private Function<State<TTrigger>, State<TTrigger>> stateTransformer;
    private State<TTrigger> state;


    private StateMachine(State<TTrigger> initialState) {
        state = initialState;
    }

    public StateMachine<TTrigger> fire(TTrigger trigger) {
        State<TTrigger> nextState = Optional.ofNullable(filter(trigger))
                .orElseGet(() -> state.handleTrigger(trigger));

        if (nextState == null) {
            return this;
        }

        if (nextState == state) { // reenter
            nextState.handleEntry(trigger);
        } else {
            state.handleExit(trigger);

            nextState = stateTransformer.apply(nextState);
            state = nextState;
            nextState.handleEntry(trigger);
        }

        return this;
    }

    private State<TTrigger> filter(TTrigger trigger) {
        List<Filter<TTrigger>> activeFilters = state.transformFilters(new ArrayList<>(filters));

        for (Filter<TTrigger> filter : Optional.ofNullable(activeFilters).orElse(filters)) {
            Filter.Result<TTrigger> filterResult = filter.handleFilter(trigger, state);
            if (filterResult == null) {
                continue;
            }

            State<TTrigger> forcedState = filterResult.getForcedState();
            if (forcedState != null) {
                log.debug("forcing state {}", forcedState);
                state.handleForceExit(trigger);
                state = stateTransformer.apply(forcedState);
                return state;
            }
        }

        return null;
    }

    public static <TTrigger> Builder<TTrigger> withInitial(State<TTrigger> initialState) {
        return new Builder<>(initialState);
    }


    @Accessors(chain = true)
    public static class Builder<TTrigger> {

        private State<TTrigger> initialState;

        @Setter
        private List<Filter<TTrigger>> filters = new ArrayList<>();

        @Setter
        private Function<State<TTrigger>, State<TTrigger>> stateTransformer = state -> state;


        Builder(State<TTrigger> initialState) {
            this.initialState = initialState;
        }


        @SuppressWarnings("unused")
        @SafeVarargs
        public final Builder<TTrigger> addFilters(Filter<TTrigger>... filters) {
            Optional.ofNullable(filters)
                    .filter(fs -> fs.length > 0)
                    .ifPresent(fs -> this.filters.addAll(Arrays.asList(fs)));

            return this;
        }

        @SuppressWarnings("unused")
        @SafeVarargs
        public final Builder<TTrigger> addFilters(BiFunction<TTrigger,
                State<TTrigger>,
                Filter.Result<TTrigger>>... filterFunctions) {

            Optional.ofNullable(filterFunctions)
                    .filter(fs -> fs.length > 0)
                    .ifPresent(fs -> this.filters.addAll(Arrays.stream(fs)
                            .map(ff -> new Filter<TTrigger>().onFilter(ff))
                            .collect(toList())));

            return this;
        }


        public StateMachine<TTrigger> build() {
            StateMachine<TTrigger> machine = new StateMachine<>(stateTransformer.apply(initialState));
            machine.filters = filters;
            machine.stateTransformer = stateTransformer;

            return machine;
        }
    }

    State<TTrigger> getState() {
        return state;
    }

    List<Filter<TTrigger>> getFilters() {
        return filters;
    }

    Function<State<TTrigger>, State<TTrigger>> getStateTransformer() {
        return stateTransformer;
    }
}
