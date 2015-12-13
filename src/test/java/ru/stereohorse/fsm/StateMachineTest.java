package ru.stereohorse.fsm;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class StateMachineTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldBuildStateMachine() {
        State<Trigger> initialState = mock(State.class);
        Function transformer = mock(Function.class);

        Filter<Trigger> filter1 = mock(Filter.class);
        Filter<Trigger> filter2 = mock(Filter.class);
        Filter<Trigger> filter3 = mock(Filter.class);

        List<Filter<Trigger>> filters = new ArrayList<>();
        filters.add(filter1);

        when(transformer.apply(eq(initialState))).thenReturn(initialState);

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .setFilters(filters)
                .addFilters(filter2, filter3)
                .addFilters((t, s) -> null)
                .setStateTransformer(transformer)
                .build();

        assertEquals(initialState, machine.getState());
        assertEquals(4, machine.getFilters().size());
        assertEquals(transformer, machine.getStateTransformer());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDoNothingIfStateTriggerHandlerReturnedNull() {
        State<Trigger> initialState = mock(State.class);
        Trigger t = mock(Trigger.class);

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .build();

        when(initialState.handleTrigger(eq(t))).thenReturn(null);

        machine.fire(t);
        verify(initialState, never()).handleEntry(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReenterIfReturnedSameState() {
        State<Trigger> initialState = mock(State.class);
        Trigger t = mock(Trigger.class);

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .build();

        when(initialState.handleTrigger(eq(t))).thenReturn(initialState);

        machine.fire(t);
        verify(initialState).handleEntry(t);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldEnterNewState() {
        State<Trigger> initialState = mock(State.class);
        State<Trigger> newState = mock(State.class);

        Trigger t = mock(Trigger.class);

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .build();

        when(initialState.handleTrigger(eq(t))).thenReturn(newState);

        machine.fire(t);
        verify(initialState).handleExit(t);
        verify(newState).handleEntry(t);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAcceptNullFilterResult() {
        State<Trigger> initialState = mock(State.class);
        Filter<Trigger> filter = mock(Filter.class);

        when(initialState.transformFilters(eq(Collections.singletonList(filter))))
                .thenReturn(Collections.singletonList(filter));

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .addFilters(filter)
                .build();

        Trigger t = mock(Trigger.class);
        machine.fire(t);
        verify(filter).handleFilter(t, initialState);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPermitFilterModificationsByStates() {
        State<Trigger> initialState = mock(State.class);
        Filter<Trigger> filter = mock(Filter.class);

        when(initialState.transformFilters(eq(Collections.singletonList(filter))))
                .thenReturn(Collections.emptyList());

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .addFilters(filter)
                .build();

        machine.fire(mock(Trigger.class));
        verify(filter, never()).handleFilter(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldForcibleChangeStateByFilter() {
        State<Trigger> initialState = mock(State.class);
        State<Trigger> forcedState = mock(State.class);

        Filter<Trigger> filter = mock(Filter.class);
        Trigger t = mock(Trigger.class);

        when(filter.handleFilter(t, initialState)).thenReturn(Filter.forceState(forcedState));

        when(initialState.transformFilters(eq(Collections.singletonList(filter))))
                .thenReturn(Collections.singletonList(filter));

        StateMachine<Trigger> machine = StateMachine.withInitial(initialState)
                .addFilters(filter)
                .build();

        machine.fire(t);
        verify(initialState).handleForceExit(t);
        verify(forcedState).handleEntry(t);
        verify(forcedState, never()).handleTrigger(t);
        verify(initialState, never()).handleTrigger(t);
    }


    private static class Trigger {
    }
}
