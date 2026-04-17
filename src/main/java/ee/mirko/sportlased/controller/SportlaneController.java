package ee.mirko.sportlased.controller;

import ee.mirko.sportlased.entity.Sportlane;
import ee.mirko.sportlased.repository.SportlaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("sportlased")
public class SportlaneController {

    private final SportlaseRepository sportlaseRepository;

    public SportlaneController(SportlaseRepository sportlaseRepository) {
        this.sportlaseRepository = sportlaseRepository;
    }

    @GetMapping
    public List<Sportlane> getSportlased() {
        return sportlaseRepository.findAll();
    }

    @PostMapping
    public Sportlane lisaSportlane(@RequestBody SportlaseLisaminePäring päring) {
        valideeriNimi(päring.nimi());
        valideeriTulemus(päring.tulemus());

        Sportlane sportlane = new Sportlane();
        sportlane.setNimi(päring.nimi().trim());
        sportlane.getTulemused().add(päring.tulemus());

        return sportlaseRepository.save(sportlane);
    }

    @PostMapping("/{id}/tulemused")
    public Sportlane lisaTulemus(@PathVariable Long id, @RequestBody TulemuseLisaminePäring päring) {
        valideeriTulemus(päring.tulemus());

        Sportlane sportlane = sportlaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sportlast id-ga " + id + " ei leitud."));

        sportlane.getTulemused().add(päring.tulemus());
        return sportlaseRepository.save(sportlane);
    }

    @GetMapping("/{id}/tulemused/summa")
    public TulemusteSummaVastus saaTulemusteSumma(@PathVariable Long id) {
        Sportlane sportlane = sportlaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sportlast id-ga " + id + " ei leitud."));

        int summa = sportlane.getTulemused().stream().mapToInt(Integer::intValue).sum();
        return new TulemusteSummaVastus(sportlane.getId(), sportlane.getNimi(), summa);
    }

    private void valideeriNimi(String nimi) {
        if (nimi == null || nimi.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sportlase nimi on kohustuslik.");
        }
    }

    private void valideeriTulemus(Integer tulemus) {
        if (tulemus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tulemus on kohustuslik.");
        }
        if (tulemus < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tulemus ei tohi olla negatiivne.");
        }
    }

    public record SportlaseLisaminePäring(String nimi, Integer tulemus) {
    }

    public record TulemuseLisaminePäring(Integer tulemus) {
    }

    public record TulemusteSummaVastus(Long sportlaneId, String nimi, Integer tulemusteKogusumma) {
    }
}