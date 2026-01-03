package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.location.Region;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;
    public List<Region> saveAll(Iterable<Region> regions) {
        return regionRepository.saveAll(regions);
    }
    public Region save(Region region) {
        return regionRepository.save(region);
    }
    public Optional<Region> findById(String id) {
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
    public boolean existsById(String id) {
        return regionRepository.existsById(id);
    }
}
