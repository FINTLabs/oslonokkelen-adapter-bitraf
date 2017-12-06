package no.fint.provider.adapter.oslo.las.backend;


import lombok.extern.slf4j.Slf4j;
import no.fint.provider.adapter.oslo.las.model.Locks;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Slf4j
@Component
public class Client {

    @Value("${oslo.las.backend.url}")
    String baseUrl;

    @Value("${oslo.las.backend.username}")
    String username;

    @Value("${oslo.las.backend.password}")
    String password;

    @Autowired
    RestTemplate restTemplate;

    HttpHeaders createHeaders(){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

    public Locks status() {
        log.info("Status");
        return restTemplate.getForObject(baseUrl + "/status", Locks.class);
    }

    public boolean unlock(String lockId) {
        log.info("Unlock {}", lockId);
        ResponseEntity<String> result = restTemplate.exchange(baseUrl + "/doors/" + lockId + "/unlock", HttpMethod.POST, new HttpEntity<>(createHeaders()), String.class);
        log.info("Response: {} {}", result.getStatusCode(), result.getBody());
        return result.getStatusCode().is2xxSuccessful();
    }

    public boolean lock(String lockId) {
        log.info("Lock {}", lockId);
        ResponseEntity<String> result = restTemplate.exchange(baseUrl + "/doors/" + lockId + "/lock", HttpMethod.POST, new HttpEntity<>(createHeaders()), String.class);
        log.info("Response: {} {}", result.getStatusCode(), result.getBody());
        return result.getStatusCode().is2xxSuccessful();
    }


}
