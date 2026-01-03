package org.spacelab.housingutilitiessystemadmin.controller.location;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.models.location.CityResponse;
import org.spacelab.housingutilitiessystemadmin.service.CityService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/cities")
@AllArgsConstructor
@Slf4j
public class CityController {

    private final CityService cityService;

    @GetMapping("/getAll")
    @ResponseBody
    public ResponseEntity<List<City>> getAllCities() {
        List<City> cities = cityService.findAll();
        log.info("All cities count: {}", cities.size());
        for (City city : cities) {
            log.info("City: id={}, name={}, regionId={}", city.getId(), city.getName(), city.getRegion().getId());
        }
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<CityResponse>> searchCities(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(cityService.searchByName(q));
    }
}
