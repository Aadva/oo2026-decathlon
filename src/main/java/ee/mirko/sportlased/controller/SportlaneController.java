package ee.mirko.sportlased.controller;

import ee.mirko.sportlased.dto.Asukoht;
import ee.mirko.sportlased.dto.Kohtunik;
import ee.mirko.sportlased.entity.Sportlane;
import ee.mirko.sportlased.repository.SportlaseRepository;
import ee.mirko.sportlased.service.OutAPIService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("sportlased")
public class SportlaneController {

    private final SportlaseRepository sportlaseRepository;
    private final OutAPIService outAPIService;

    public SportlaneController(SportlaseRepository sportlaseRepository, OutAPIService outAPIService) {
        this.sportlaseRepository = sportlaseRepository;
        this.outAPIService = outAPIService;
    }

    @GetMapping
    public Page<Sportlane> getSportlased(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String riik,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        var pageable = PageRequest.of(page, size);
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return sportlaseRepository.leiaLehekuljAsc(riik, pageable);
        }
        return sportlaseRepository.leiaLehekuljDesc(riik, pageable);
    }

    @PostMapping
    public Sportlane lisaSportlane(@RequestBody SportlaseLisaminePäring päring) {
        valideeriNimi(päring.nimi());
        valideeriTulemus(päring.tulemus());

        Sportlane sportlane = new Sportlane();
        sportlane.setNimi(päring.nimi().trim());
        sportlane.setRiik(päring.riik() != null ? päring.riik().trim() : null);
        sportlane.getTulemused().add(päring.tulemus());

        return sportlaseRepository.save(sportlane);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kustutaSportlane(@PathVariable Long id) {
        if (!sportlaseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sportlast id-ga " + id + " ei leitud.");
        }
        sportlaseRepository.deleteById(id);
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

    @GetMapping("/kohtunikud")
    public List<Kohtunik> getKohtunikud() {
        return outAPIService.getKohtunikud();
    }

    @GetMapping("/asukohad")
    public List<Asukoht> getAsukohad() {
        return outAPIService.getAsukohad();
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

    public record SportlaseLisaminePäring(String nimi, String riik, Integer tulemus) {
    }

    public record TulemuseLisaminePäring(Integer tulemus) {
    }

    public record TulemusteSummaVastus(Long sportlaneId, String nimi, Integer tulemusteKogusumma) {
    }
}