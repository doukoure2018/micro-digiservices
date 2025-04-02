package io.digiservices.ebanking.controller;

import io.digiservices.ebanking.paylaod.PlanPagosDto;
import io.digiservices.ebanking.service.PlanPagosService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/digi/ebanking")
public class PlanPagosController {

    private PlanPagosService planPagosService;

    public PlanPagosController(PlanPagosService planPagosService)
    {
        this.planPagosService = planPagosService;
    }

    @GetMapping("/{numCredito}/planPagos")
    public ResponseEntity<List<PlanPagosDto>> getAllPlanPagosByCreditos(@PathVariable(name = "numCredito") Long numCredito)
    {
        return ResponseEntity.ok(planPagosService.getAllPlanPagosByCreditos(numCredito));
    }

    @GetMapping("/{codAgencia}/{start}/{end}/TT1")
    public List<Object[]> getAllTT1(
            @PathVariable(name = "codAgencia") String codAgencia,
            @PathVariable(name = "start") @DateTimeFormat(pattern = "dd-MM-yyyy") Date start,
            @PathVariable(name = "end") @DateTimeFormat(pattern = "dd-MM-yyyy") Date end
    )
    {
        return planPagosService.getAllTT1(codAgencia,start,end);
    }

    @GetMapping("/{numCredito}/getListEcheanceParCredit")
    public ResponseEntity<List<PlanPagosDto>> getListEcheanceParCredit(
            @PathVariable(value = "numCredito") Long numCredito
    )
    {
        return ResponseEntity.ok(planPagosService.getEcheancesParCredit(numCredito));
    }

    @GetMapping("{numCredito}/{indEstado}/getListRetardsByCredit")
    public ResponseEntity<List<PlanPagosDto>> getListRetardsByCredit(
            @PathVariable(value = "numCredito") Long numCredito,
            @PathVariable(value = "indEstado") String indEstado,
            @RequestParam(value = "fecCuota") LocalDateTime fecCuota
    )
    {
       return ResponseEntity.ok(planPagosService.getListRetardByNumCredit(numCredito,indEstado,fecCuota));
    }

    @GetMapping("/{indEstado}/getNbreAllCredits")
    public ResponseEntity<Long> getNbreAllCredit(
            @PathVariable(value = "indEstado") String indEstado,
            @RequestParam(value = "fecCuota") LocalDateTime fecCuota
    )
    {
        return ResponseEntity.ok(planPagosService.getNbreCredits(indEstado,fecCuota));
    }

    @GetMapping("/{indEstado}/getSommeRetard")
    public ResponseEntity<BigDecimal> getSommeRetard(
            @PathVariable(value = "indEstado") String indEstado,
            @RequestParam(value = "fecCuota") LocalDateTime fecCuota
    )
    {
        return ResponseEntity.ok(planPagosService.getSommeRetards(indEstado,fecCuota));
    }

    @GetMapping("/{numCredito}/{indEstado}/getLastInsertedCreditByNumCredit")
    public ResponseEntity<PlanPagosDto> getLastInsertedData(
            @PathVariable(value = "numCredito") Long numCredito,
            @PathVariable(value = "indEstado") String indEstado
    )
    {
       return ResponseEntity.ok(planPagosService.getAgeOfRetardCredit(numCredito,indEstado));
    }
}
