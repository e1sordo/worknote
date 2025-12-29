package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.vacations.UpsertVacationDto;
import es.e1sordo.worknote.dto.vacations.VacationDto;
import es.e1sordo.worknote.mapping.Mappings;
import es.e1sordo.worknote.services.VacationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
public class VacationsController {

    private final VacationsService service;

    @GetMapping("/next")
    public VacationDto getNext() {
        return service.findNextVacation().map(Mappings::mapToDto).orElse(null);
    }

    @GetMapping
    public List<VacationDto> getAll() {
        return service.findAll().stream().map(Mappings::mapToDto).toList();
    }

    @PostMapping
    public VacationDto upsert(@RequestBody UpsertVacationDto request) {
        if (request.id() != null) {
            return Mappings.mapToDto(
                    service.update(
                            request.id(),
                            request.startDate(),
                            request.endDate(),
                            request.confirmed()
                    )
            );
        }
        return Mappings.mapToDto(service.create(request.startDate(), request.endDate()));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
