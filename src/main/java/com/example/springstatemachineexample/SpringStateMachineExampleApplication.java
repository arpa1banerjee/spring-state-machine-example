package com.example.springstatemachineexample;

import com.example.springstatemachineexample.payload.OfferEvent;
import com.example.springstatemachineexample.payload.OfferState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import static com.example.springstatemachineexample.payload.OfferEvent.*;

@Slf4j
@SpringBootApplication
public class SpringStateMachineExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringStateMachineExampleApplication.class, args);
	}

}

@Slf4j
@Component
class Runner implements ApplicationRunner {
	private final StateMachineFactory<OfferState, OfferEvent> factory;

	Runner(StateMachineFactory<OfferState, OfferEvent> factory) {
		this.factory = factory;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		StateMachine<OfferState, OfferEvent> stateMachine = this.factory.getStateMachine();

		stateMachine.start();
		log.info("Current state is {}", stateMachine.getState().getId().name());

		stateMachine.sendEvent(MessageBuilder.withPayload(IMAGE_ANALYSIS).build());
		log.info("Current state is {}", stateMachine.getState().getId().name());

		stateMachine.sendEvent(MessageBuilder.withPayload(VIDEO_ANALYSIS).build());
		log.info("Current state is {}", stateMachine.getState().getId().name());

		stateMachine.sendEvent(MessageBuilder.withPayload(PUBLISH).build());
		log.info("Current state is {}", stateMachine.getState().getId().name());
	}
}
