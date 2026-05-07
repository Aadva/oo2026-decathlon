package ee.mirko.sportlased.service;

import ee.mirko.sportlased.dto.Asukoht;
import ee.mirko.sportlased.dto.Kohtunik;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OutAPIService {

    private final RestClient restClient;

    public OutAPIService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://69fd209530ad0a6fd1c07b2c.mockapi.io/api/v1")
                .build();
    }

    public List<Kohtunik> getKohtunikud() {
        return restClient.get()
                .uri("/kohtunikud")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<Asukoht> getAsukohad() {
        return restClient.get()
                .uri("/asukohad")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
