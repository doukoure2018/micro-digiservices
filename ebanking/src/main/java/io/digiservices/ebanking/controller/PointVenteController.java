package io.digiservices.ebanking.controller;

import io.digiservices.ebanking.dto.HttpResponse;
import io.digiservices.ebanking.paylaod.PointVenteDto;
import io.digiservices.ebanking.service.PointVenteService;
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
public class PointVenteController {

    private final PointVenteService pointVenteService;

    @PostMapping("/addPointVente")
    public ResponseEntity<HttpResponse> addPointVente(@RequestBody PointVenteDto pointVenteDto)
    {
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .data(Map.of("pointVente", pointVenteService.addPointVente(pointVenteDto)))
                                .message("PointVente Created")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }

    @GetMapping("/{agence_id}/pointventes")
    public ResponseEntity<List<PointVenteDto>> pointventes(@PathVariable(name = "agence_id") Long agence_id)
    {
        return ResponseEntity.ok(pointVenteService.getAllPointeVenteByAgence(agence_id));
    }

    @GetMapping("/{idPs}/pointvente")
    public ResponseEntity<PointVenteDto> pointvente(@PathVariable(name = "idPs") Long idPs)
    {
        return ResponseEntity.ok(pointVenteService.getPsById(idPs));
    }
}
