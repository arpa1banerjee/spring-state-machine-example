package com.example.springstatemachineexample.config;

import com.example.springstatemachineexample.payload.OfferEvent;
import com.example.springstatemachineexample.payload.OfferState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import static com.example.springstatemachineexample.payload.OfferEvent.*;
import static com.example.springstatemachineexample.payload.OfferState.*;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class OfferReviewStateMachineConfiguration extends StateMachineConfigurerAdapter<OfferState, OfferEvent> {

    private static final String PASSED_STEP_COUNT = "passedStepCount";

    @Override
    public void configure(StateMachineConfigurationConfigurer<OfferState, OfferEvent> config) throws Exception {
        StateMachineListenerAdapter<OfferState, OfferEvent> listener = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<OfferState, OfferEvent> from, State<OfferState, OfferEvent> to) {
                log.info("State changed from {} to {}", Objects.isNull(from) ? "none" : from.getId(), to.getId());
            }
        };
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(listener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OfferState, OfferEvent> states) throws Exception {
        states
                .withStates()
                .initial(NEW_OFFER_CREATED)
                .end(OFFER_PUBLISHED)
                .states(new HashSet<>(Arrays.asList(OFFER_IMAGE_ANALYSED, OFFER_VIDEO_ANALYSED)))
                .state(OFFER_IMAGE_ANALYSED, executeAction(), errorAction())
                .state(OFFER_VIDEO_ANALYSED, executeAction(), errorAction());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OfferState, OfferEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(NEW_OFFER_CREATED).target(OFFER_IMAGE_ANALYSED).event(IMAGE_ANALYSIS).and()
                .withExternal()
                .source(OFFER_IMAGE_ANALYSED).target(OFFER_VIDEO_ANALYSED).event(VIDEO_ANALYSIS).and()
                .withExternal()
                .source(OFFER_VIDEO_ANALYSED).target(OFFER_PUBLISHED).event(PUBLISH).guard(publishGuard());
    }

    @Bean
    public Guard<OfferState, OfferEvent> publishGuard() {
        return ctx -> {
            int passedStepCount = (int) ctx
                    .getExtendedState()
                    .getVariables()
                    .getOrDefault(PASSED_STEP_COUNT, 0);
            return passedStepCount > 0;
        };
    }

    @Bean
    public Action<OfferState, OfferEvent> executeAction() {
        return ctx -> {
            log.info("Execute {}. Do something here....", ctx.getTarget().getId());
            int passedStepCount = (int) ctx
                    .getExtendedState()
                    .getVariables()
                    .getOrDefault(PASSED_STEP_COUNT, 0);
            passedStepCount++;
            ctx
                    .getExtendedState()
                    .getVariables()
                    .put(PASSED_STEP_COUNT, passedStepCount);
        };
    }

    @Bean
    public Action<OfferState, OfferEvent> errorAction() {
        return ctx -> {
            if(!Objects.isNull(ctx.getException())) {
                log.error("Error {} {}", ctx
                        .getSource()
                        .getId(), ctx.getException());
            }
        };
    }
}
