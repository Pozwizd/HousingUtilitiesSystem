package org.spacelab.housingutilitiessystemadmin.service;


import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.mappers.StreetMapper;
import org.spacelab.housingutilitiessystemadmin.models.location.StreetResponse;
import org.spacelab.housingutilitiessystemadmin.repository.StreetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StreetService {

    private final StreetRepository streetRepository;
    private final StreetMapper streetMapper;


    public Street save(Street street) {
        return streetRepository.save(street);
    }

    public Optional<Street> findById(String id) {
        return streetRepository.findById(id);
    }
    
    public Optional<Street> findById(ObjectId id) {
        return streetRepository.findById(id.toString());
    }

    public List<Street> findAll() {
        return streetRepository.findAll();
    }

    public List<Street> findByCityId(String cityId) {
        return streetRepository.findByCityId(cityId);
    }

    public List<StreetResponse> searchByCityAndName(String cityId, String name) {
        List<Street> streets;
        if (name == null || name.trim().isEmpty()) {
            return streetMapper.toResponse(findByCityId(cityId));
        }
        return streetMapper.toResponse(streetRepository.findByCityIdAndNameContainingIgnoreCase(cityId, name.trim()));
    }
    
    public List<StreetResponse> searchByName(String name) {
        List<Street> streets;
        if (name == null || name.trim().isEmpty()) {
            return streetMapper.toResponse(findAll());
        }
        return streetMapper.toResponse(streetRepository.findByNameContainingIgnoreCase(name.trim()));
    }


    public void deleteById(String id) {
        streetRepository.deleteById(id);
    }
    
    public void deleteById(ObjectId id) {
        streetRepository.deleteById(id.toString());
    }

    public Street update(Street street) {
        return streetRepository.save(street);
    }

    public List<Street> saveAll(List<Street> streets) {
        return streetRepository.saveAll(streets);
    }

    public void deleteAll() {
        streetRepository.deleteAll();
    }
}
