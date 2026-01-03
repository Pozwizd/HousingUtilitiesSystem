package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.Region;
import org.spacelab.housingutilitiessystemadmin.repository.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    public List<Region> saveAll(Iterable<Region> regions) {
        return regionRepository.saveAll(regions);
    }

    public Region save(Region region) {
        return regionRepository.save(region);
    }

    public Optional<Region> findById(ObjectId id) {
        return regionRepository.findById(id);
    }

    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    public void deleteAll() {
        regionRepository.deleteAll();
    }

    public void delete(Region region) {
        regionRepository.delete(region);
    }

    public boolean existsById(ObjectId id) {
        return regionRepository.existsById(id);
    }
}
