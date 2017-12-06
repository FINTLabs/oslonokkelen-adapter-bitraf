package no.fint.provider.adapter.oslo.las.backend

import org.springframework.web.client.RestTemplate
import spock.lang.Requires
import spock.lang.Specification

class LasBackendClientSpec extends Specification {

    def env = System.getenv()

    @Requires({ env.BACKEND_URL })
    def "Status for Lås"() {
        given:
        def client = new Client()
        client.restTemplate = new RestTemplate()
        client.baseUrl = "$env.BACKEND_URL"

        when:
        def locks = client.status()

        then:
        locks
        locks.doors
        locks.doors.size() == 3
        locks.doors['virtual-1'].status == 200
    }

    @Requires({ env.BACKEND_URL && env.USERNAME && env.PASSWORD })
    def "Lås opp virtual-1"() {
        given:
        def client = new Client()
        client.restTemplate = new RestTemplate()
        client.baseUrl = "$env.BACKEND_URL"
        client.username = "$env.USERNAME"
        client.password = "$env.PASSWORD"

        when:
        def result = client.unlock("virtual-1")

        then:
        result
    }

    @Requires({ env.BACKEND_URL && env.USERNAME && env.PASSWORD })
    def "Lås virtual-1"() {
        given:
        def client = new Client()
        client.restTemplate = new RestTemplate()
        client.baseUrl = "$env.BACKEND_URL"
        client.username = "$env.USERNAME"
        client.password = "$env.PASSWORD"

        when:
        def result = client.lock("virtual-1")

        then:
        result

    }
}
