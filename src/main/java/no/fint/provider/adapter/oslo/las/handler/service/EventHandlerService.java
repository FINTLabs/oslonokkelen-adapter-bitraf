package no.fint.provider.adapter.oslo.las.handler.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.relation.FintResource;
import no.fint.model.ressurser.lasstyring.Las;
import no.fint.model.ressurser.lasstyring.LasstyringActions;
import no.fint.provider.adapter.event.EventResponseService;
import no.fint.provider.adapter.event.EventStatusService;
import no.fint.provider.adapter.oslo.las.backend.Client;
import no.fint.provider.adapter.oslo.las.model.Lock;
import no.fint.provider.adapter.oslo.las.model.Locks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The EventHandlerService receives the <code>event</code> from SSE endpoint (provider) in the {@link #handleEvent(Event)} method.
 */
@Slf4j
@Service
public class EventHandlerService {

    @Autowired
    private EventResponseService eventResponseService;

    @Autowired
    private EventStatusService eventStatusService;

    @Autowired
    private Client backend;

    /**
     * <p>
     * HandleEvent is responsible of handling the <code>event</code>. This is what should be done:
     * </p>
     * <ol>
     * <li>Verify that the adapter can handle the <code>event</code>. This is done in the {@link EventStatusService#verifyEvent(Event)} method</li>
     * <li>Call the code to handle the action</li>
     * <li>Posting back the handled <code>event</code>. This done in the {@link EventResponseService#postResponse(Event)} method</li>
     * </ol>*
     * @param event The <code>event</code> received from the provider
     */
    public void handleEvent(Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(event);
        } else {
            if (event != null && eventStatusService.verifyEvent(event).getStatus() == Status.ADAPTER_ACCEPTED) {
                LasstyringActions action = LasstyringActions.valueOf(event.getAction());
                Event<FintResource> responseEvent = new Event<>(event);

                switch (action) {
                    case GET_ALL_LAS:
                        onGetAllLas(responseEvent);
                        break;
                    case UPDATE_LAS:
                        onUpdateLas(event, responseEvent);
                        break;
                    default:
                        responseEvent.setStatus(Status.ADAPTER_REJECTED);
                        eventResponseService.postResponse(responseEvent);
                        return;
                }

                responseEvent.setStatus(Status.ADAPTER_RESPONSE);
                eventResponseService.postResponse(responseEvent);
            }
        }
    }

    static Identifikator getIdentifikator(String id) {
        Identifikator result = new Identifikator();
        result.setIdentifikatorverdi(id);
        return result;
    }

    static Las mapLas(String name, Lock state) {
        Las las = new Las();
        las.setSystemId(getIdentifikator(name));
        las.setStatus(getStatus(state.status));
        if (state.last_seen != null) {
            las.setSistsett(new Date(state.last_seen.multiply(BigDecimal.valueOf(1000)).longValue()));
        }
        log.info("Lås: {}", las);
        return las;
    }

    private static String getStatus(int status) {
        switch (status) {
            case 200:
                return "ukjent";
            case 503:
                return "feil";
            default:
                return Long.toString(status);
        }
    }

    /**
     * Example of handling action
     *
     * @param event
     * @param responseEvent Event containing the response
     */
    private void onUpdateLas(Event event, Event<FintResource> responseEvent) {
        log.info("UpdateLas {}", event);
    }

    /**
     * Example of handling action
     *
     * @param responseEvent Event containing the response
     */
    private void onGetAllLas(Event<FintResource> responseEvent) {
        log.info("GetAllLas");
        Locks locks = backend.status();
        log.info("Result: {}", locks);
        if (locks == null || locks.doors == null || locks.doors.isEmpty()) {
            log.error("Invalid result!!");
            responseEvent.setStatus(Status.ERROR);
        } else {
            locks.doors.forEach((k, v) -> {
                log.info("{} -> {}", k, v);
                Las l = mapLas(k, v);
                FintResource r = FintResource.with(l);
                responseEvent.addData(r);
            });//entrySet().stream().map(e -> mapLas(e.getKey(), e.getValue())).map(FintResource::with).forEach(responseEvent::addData);
        }
    }

    /**
     * Checks if the application is healthy and updates the event object.
     *
     * @param event The event object
     */
    public void postHealthCheckResponse(Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        if (healthCheck()) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY.name()));
        } else {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
        }

        eventResponseService.postResponse(healthCheckEvent);
    }

    /**
     * This is where we implement the health check code
     *
     * @return {@code true} if health is ok, else {@code false}
     */
    private boolean healthCheck() {
        /*
         * Check application connectivity etc.
         */
        log.info("Health Check");
        Locks result = backend.status();
        return result != null && result.doors != null;
    }

    /**
     * Data used in examples
     */
    @PostConstruct
    void init() {
        log.info("Velkommen.");
    }
}
