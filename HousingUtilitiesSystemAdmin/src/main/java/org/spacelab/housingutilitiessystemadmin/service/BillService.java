package org.spacelab.housingutilitiessystemadmin.service;

import lombok.AllArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.Bill;
import org.spacelab.housingutilitiessystemadmin.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BillService {

    private final BillRepository billRepository;

    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public Optional<Bill> findById(String id) {
        return billRepository.findById(id);
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public void deleteById(String id) {
        billRepository.deleteById(id);
    }

    public Bill update(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> saveAll(List<Bill> bills) {
        return billRepository.saveAll(bills);
    }

    public void deleteAll() {
        billRepository.deleteAll();
    }
}