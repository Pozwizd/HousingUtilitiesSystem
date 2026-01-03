package org.spacelab.housingutilitiessystemadmin.controller.chairman;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanRequest;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponseTable;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.spacelab.housingutilitiessystemadmin.service.ChairmanService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/chairmen")
@AllArgsConstructor
@Slf4j
@Validated
public class ChairmanController {

    private final ChairmanService chairmanService;

    @PostMapping("/getAll")
    @ResponseBody
    public ResponseEntity<Page<ChairmanResponseTable>> getChairmanResponseTable(
            @Valid @RequestBody ChairmanRequestTable chairmanRequestTable) {
        log.info("Получен запрос на получение председателей с фильтрами: page={}, size={}, fullName={}, phone={}, email={}, login={}, status={}",
                chairmanRequestTable.getPage(), chairmanRequestTable.getSize(),
                chairmanRequestTable.getFullName(), chairmanRequestTable.getPhone(),
                chairmanRequestTable.getEmail(), chairmanRequestTable.getLogin(),
                chairmanRequestTable.getStatus());
        
        Page<ChairmanResponseTable> result = chairmanService.getChairmenTable(chairmanRequestTable);
        log.info("Возвращено {} председателей из {}", result.getNumberOfElements(), result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getChairman/{id}")
    @ResponseBody
    public ResponseEntity<ChairmanResponse> getChairman(@PathVariable String id) {
        return ResponseEntity.ok(chairmanService.getChairmanById(id));
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<ChairmanResponse> createChairman(
            @Valid @RequestBody ChairmanRequest chairmanRequest) {
        return ResponseEntity.ok(chairmanService.createChairman(chairmanRequest));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ChairmanResponse> updateChairman(
            @PathVariable String id, @ModelAttribute ChairmanRequest chairmanRequest) {
        return ResponseEntity.ok(chairmanService.updateChairman(id, chairmanRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> deleteChairman(@PathVariable String id) {
        return ResponseEntity.ok(chairmanService.deleteChairman(id));
    }

    @GetMapping("/getStatuses")
    @ResponseBody
    public ResponseEntity<List<Status>> getStatuses() {
        return ResponseEntity.ok(List.of(Status.values()));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<ChairmanResponse>> searchChairmen(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(chairmanService.searchByName(q));
    }

    @GetMapping({"/", ""})
    public ModelAndView getChairmenPage(Model model) {
        return new ModelAndView("chairman/chairmen")
                .addObject("pageTitle", "chairmen.title")
                .addObject("pageActive", "chairmen");
    }

    @GetMapping("/create")
    public ModelAndView getChairmanCreatePage(Model model) {
        model.addAttribute("pageTitle", "chairmen.createChairman");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("isEdit", false);
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairman-edit");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView getChairmanEditPage(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "chairmen.editChairman");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("isEdit", true);
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairman-edit");
    }

    @GetMapping("/card/{id}")
    public ModelAndView showChairmanProfile(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "chairmen.chairmanProfile");
        model.addAttribute("pageActive", "chairmen");
        model.addAttribute("opened", true);
        return new ModelAndView("chairman/chairmanCard");
    }
}

