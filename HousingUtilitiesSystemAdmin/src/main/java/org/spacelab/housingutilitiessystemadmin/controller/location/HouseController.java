package org.spacelab.housingutilitiessystemadmin.controller.location;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.models.filters.house.HouseRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseRequest;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponseTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponse;
import org.spacelab.housingutilitiessystemadmin.service.HouseService;
import org.spacelab.housingutilitiessystemadmin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/houses")
@AllArgsConstructor
@Slf4j
@Validated
public class HouseController {

    private final HouseService houseService;
    private final UserService userService;

    @PostMapping("/getAll")
    @ResponseBody
    public ResponseEntity<Page<HouseResponseTable>> getHouseResponseTable(
            @Valid @RequestBody HouseRequestTable houseRequestTable) {
        log.info("Получен запрос на получение домов с фильтрами: page={}, size={}, houseNumber={}, streetName={}, cityName={}, chairmanFullName={}, status={}",
                houseRequestTable.getPage(), houseRequestTable.getSize(),
                houseRequestTable.getHouseNumber(), houseRequestTable.getStreetName(),
                houseRequestTable.getCityName(), houseRequestTable.getChairmanFullName(),
                houseRequestTable.getStatus());
        
        Page<HouseResponseTable> result = houseService.getHousesTable(houseRequestTable);
        log.info("Возвращено {} домов из {}", result.getNumberOfElements(), result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getHouse/{id}")
    @ResponseBody
    public ResponseEntity<HouseResponse> getHouse(@PathVariable String id) {
        return ResponseEntity.ok(houseService.getHouseById(id));
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<HouseResponse> createHouse(
            @Valid @RequestBody HouseRequest houseRequest) {
        return ResponseEntity.ok(houseService.createHouse(houseRequest));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<HouseResponse> updateHouse(
            @PathVariable String id, @ModelAttribute HouseRequest houseRequest) {
        return ResponseEntity.ok(houseService.updateHouse(id, houseRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> deleteHouse(@PathVariable String id) {
        return ResponseEntity.ok(houseService.deleteHouse(id));
    }

    @GetMapping("/getStatuses")
    @ResponseBody
    public ResponseEntity<List<Status>> getStatuses() {
        return ResponseEntity.ok(List.of(Status.values()));
    }

    @GetMapping("/getByStreet/{streetId}")
    @ResponseBody
    public ResponseEntity<List<HouseResponse>> getHousesByStreet(@PathVariable String streetId) {
        return ResponseEntity.ok(houseService.findByStreetId(streetId));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<HouseResponse>> searchHouses(
            @RequestParam String streetId,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(houseService.searchByStreetAndNumber(streetId, q));
    }

    @GetMapping({"/", ""})
    public ModelAndView getHousesPage(Model model) {
        return new ModelAndView("house/houses")
                .addObject("pageTitle", "houses.title")
                .addObject("pageActive", "houses");
    }

    @GetMapping("/create")
    public ModelAndView getHouseCreatePage(Model model) {
        model.addAttribute("pageTitle", "houses.createHouse");
        model.addAttribute("pageActive", "houses");
        model.addAttribute("isEdit", false);
        model.addAttribute("opened", true);
        return new ModelAndView("house/house-edit");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView getHouseEditPage(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "houses.editHouse");
        model.addAttribute("pageActive", "houses");
        model.addAttribute("isEdit", true);
        model.addAttribute("opened", true);
        return new ModelAndView("house/house-edit");
    }

    @GetMapping("/card/{id}")
    public ModelAndView showHouseProfile(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "houses.houseProfile");
        model.addAttribute("pageActive", "houses");
        model.addAttribute("opened", true);
        return new ModelAndView("house/houseCard");
    }

    @GetMapping("/getUsersByHouse/{houseId}")
    @ResponseBody
    public ResponseEntity<Page<UserResponse>> getUsersByHouse(
            @PathVariable String houseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос на получение пользователей для дома с ID: {} (страница: {}, размер: {})", 
                houseId, page, size);
        Page<UserResponse> users = userService.getUsersByHouseIdPaginated(houseId, page, size);
        return ResponseEntity.ok(users);
    }
}
