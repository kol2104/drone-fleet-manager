package com.example.drone.common.messaging;

public final class DroneFlightMessaging {

    public static final String EXCHANGE_NAME = "drone.flight.exchange";
    public static final String COMMAND_QUEUE = "drone.flight.commands";
    public static final String RESULT_QUEUE = "drone.flight.results";
    public static final String RESULT_DEAD_LETTER_QUEUE = "drone.flight.results.dlq";
    public static final String COMMAND_ROUTING_KEY_PATTERN = "command.#";
    public static final String RESULT_ROUTING_KEY_PATTERN = "result.#";
    public static final String COMMAND_ROUTING_KEY_PREFIX = "command.";
    public static final String RESULT_MESSAGE_NAME = "flight-result";

    private DroneFlightMessaging() {
    }
}
