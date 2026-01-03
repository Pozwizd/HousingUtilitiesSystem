package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.mappers.CityMapper;
import org.spacelab.housingutilitiessystemadmin.models.location.CityResponse;
import org.spacelab.housingutilitiessystemadmin.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public List<City> saveAll(Iterable<City> cities) {
        return cityRepository.saveAll(cities);
    }

    public City save(City city) {
        return cityRepository.save(city);
    }

    public Optional<City> findById(ObjectId id) {
        return cityRepository.findById(id);
    }

    public List<City> findAll() {
        return cityRepository.findAll();
    }

    public List<CityResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return cityMapper.toResponse(findAll());
        }
        log.info("Searching cities with query: {}", name);
        List<City> cities = cityRepository.findByNameContainingIgnoreCase(name.trim());
        log.info("Found {} cities", cities.size());
        return cityMapper.toResponse(cities);
    }

    public void deleteById(ObjectId id) {
        cityRepository.deleteById(id);
    }

    public City update(City city) {
        return cityRepository.save(city);
    }

    public List<City> saveAll(List<City> cities) {
        return cityRepository.saveAll(cities);
    }

    public void deleteAll() {
        cityRepository.deleteAll();
    }
}