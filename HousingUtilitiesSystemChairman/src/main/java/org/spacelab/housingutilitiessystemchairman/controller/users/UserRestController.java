package org.spacelab.housingutilitiessystemchairman.controller.users;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.location.Status;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.user.UserRequest;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponse;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemchairman.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
public class UserRestController {
    private final UserService userService;
    @GetMapping("/getUser/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable ObjectId id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @PostMapping("/getAll")
    public ResponseEntity<PageResponse<UserResponseTable>> getUserResponseTable(
            @Valid @RequestBody UserRequestTable userRequestTable) {
        return ResponseEntity.ok(userService.getUsersTable(userRequestTable));
    }
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @ModelAttribute UserRequest userRequest) {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable ObjectId id,
            @Valid @ModelAttribute UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable ObjectId id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
    @GetMapping("/getStatuses")
    public ResponseEntity<List<Status>> getStatuses() {
        return ResponseEntity.ok(List.of(Status.values()));
    }
}
