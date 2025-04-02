package io.digiservices.ebanking.controller;


import io.digiservices.ebanking.dto.HttpResponse;
import io.digiservices.ebanking.paylaod.AgenceDto;
import io.digiservices.ebanking.service.AgenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/ebanking")
@RequiredArgsConstructor
public class AgenceController {

    private final AgenceService agenceService;

    @PostMapping("/addAgence")
    public ResponseEntity<HttpResponse> addAgence(@RequestBody AgenceDto agenceDto)
    {
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .data(Map.of("agence", agenceService.addAgence(agenceDto)))
                                .message("Agence Created")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }

    @GetMapping("/{delegation_id}/agences")
    public ResponseEntity<List<AgenceDto>> agences(@PathVariable(name = "delegation_id") Long delegation_id)
    {
        return ResponseEntity.ok(agenceService.getAllAgenceByDelegation(delegation_id));
    }

    @GetMapping("/{agence_id}/agence")
    public ResponseEntity<AgenceDto> agence(@PathVariable(name = "agence_id") Long agence_id)
    {
        return ResponseEntity.ok(agenceService.getAgenceById(agence_id));
    }
}
