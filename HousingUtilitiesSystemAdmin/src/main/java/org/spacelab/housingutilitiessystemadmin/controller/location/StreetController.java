package org.spacelab.housingutilitiessystemadmin.controller.location;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.models.location.StreetResponse;
import org.spacelab.housingutilitiessystemadmin.service.StreetService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/streets")
@AllArgsConstructor
@Slf4j
public class StreetController {

    private final StreetService streetService;

    @GetMapping("/getAll")
    @ResponseBody
    public ResponseEntity<List<Street>> getAllStreets() {
        return ResponseEntity.ok(streetService.findAll());
    }

    @GetMapping("/getByCity/{cityId}")
    @ResponseBody
    public ResponseEntity<List<Street>> getStreetsByCity(@PathVariable String cityId) {
        return ResponseEntity.ok(streetService.findByCityId(cityId));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<StreetResponse>> searchStreets(
            @RequestParam(required = false) String cityId,
            @RequestParam(required = false) String q) {
        if (cityId != null && !cityId.isEmpty()) {
            return ResponseEntity.ok(streetService.searchByCityAndName(cityId, q));
        } else {
            return ResponseEntity.ok(streetService.searchByName(q));
        }
    }
}
