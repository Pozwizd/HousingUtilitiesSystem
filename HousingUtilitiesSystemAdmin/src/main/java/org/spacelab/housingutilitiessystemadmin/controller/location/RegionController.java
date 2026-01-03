package org.spacelab.housingutilitiessystemadmin.controller.location;

import lombok.AllArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.location.Region;
import org.spacelab.housingutilitiessystemadmin.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/regions")
@AllArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    public ResponseEntity<List<Region>> getAllRegions() {
        List<Region> regions = regionService.findAll();
        return ResponseEntity.ok(regions);
    }



}
