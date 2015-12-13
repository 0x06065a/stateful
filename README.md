![travis](https://travis-ci.org/0x06065a/stateful.svg?branch=master)

### stateful

FSM implementation

#### Installation
 
 
```gradle
repositories {
    maven {
        url  'http://dl.bintray.com/0x06065a/maven' 
    }
}

dependencies {
    compile 'ru.stereohorse:stateful:1.0.1'
}
```

#### Example

```java
@Service
public class StateService {

    @Autowired
    private ApplicationContext ctx;

    public StateMachine<TelegramTrigger> createStateMachineFor(User user) {
        InitialState initialState = new InitialState(user);

        return StateMachine

                // bootstrap state that will transit to initial one
                // as soon as first telegram is received
                .withInitial(new State<TelegramTrigger>()
                        .onTrigger(trigger -> initialState))

                // filters will intercept every trigger in declared order
                // possibly transforming or even forcibly changing the machine state
                .addFilters((trigger, state) -> Optional.ofNullable(trigger.getMsg())
                        .map(Message::text)
                        .filter("/start"::equals)
                        .map(txt -> forceState(initialState))
                        .orElse(null))

                // this transformation will be applied to every state
                // even initial one
                // in this case transformer is used to inject beans into
                // non-managed state objects
                .setStateTransformer(state -> {
                    ctx.getAutowireCapableBeanFactory().autowireBeanProperties(
                            state, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true
                    );

                    return state;
                })

                .build();
    }
}


public class InitialState {

    public InitialState(User user) {
        // ..
    }

    @Override
    protected void onEntry(TelegramTrigger trigger) {
        // ..

    }

    @Override
    protected State<TelegramTrigger> onTrigger(TelegramTrigger trigger) {
        // ..
    }
}


public class TelegramTrigger {

    private Message msg;
}


@Service
public class TelegramService {

    public void handleMessage(Message msg) throws ExecutionException {
        getUserFor(msg).triggerTelegram(msg);
    }
}


public class User {

    private StateMachine<TelegramTrigger> stateMachine = stateService.createStateMachineFor(this);

    public void triggerTelegram(Message msg) {
        stateMachine.fire(new TelegramTrigger().setMsg(msg));
    }
}

```