package no.fint.provider.adapter.oslo.las.handler.service

import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.provider.adapter.event.EventResponseService
import no.fint.provider.adapter.event.EventStatusService
import no.fint.provider.adapter.oslo.las.backend.Client
import spock.lang.Specification

class EventHandlerServiceSpec extends Specification {
    private EventHandlerService eventHandlerService
    private EventStatusService eventStatusService
    private EventResponseService eventResponseService
    private Client backend

    void setup() {
        backend = Mock(Client)
        eventStatusService = Mock(EventStatusService)
        eventResponseService = Mock(EventResponseService)
        eventHandlerService = new EventHandlerService(eventStatusService: eventStatusService, eventResponseService: eventResponseService, backend: backend)
    }

    def "Post response on health check"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH, 'test')

        when:
        eventHandlerService.handleEvent(event)

        then:
        1 * eventResponseService.postResponse(_ as Event)
    }
}
