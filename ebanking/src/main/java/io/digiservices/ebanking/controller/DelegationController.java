package io.digiservices.ebanking.controller;

import io.digiservices.ebanking.dto.HttpResponse;
import io.digiservices.ebanking.paylaod.DelegationDto;
import io.digiservices.ebanking.service.DelegationService;
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
public class DelegationController {

    private final DelegationService delegationService;

    @PostMapping("/addDelegation")
    public ResponseEntity<HttpResponse> addDelegation(@RequestBody DelegationDto delegationDto)
    {
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .data(Map.of("delegation", delegationService.saveDelegation(delegationDto)))
                                .message("Delegation Created")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }

    @GetMapping("/allDelegation")
    public ResponseEntity<List<DelegationDto>> getAllDelegation()
    {
        return ResponseEntity.ok(delegationService.getAllDelegation());
    }

    @GetMapping("/{id_delegation}/delegation")
    public ResponseEntity<DelegationDto> getDelegation(@PathVariable(name = "id_delegation") Long id_delegation)
    {
        return ResponseEntity.ok(delegationService.getDelegationById(id_delegation));
    }




}
