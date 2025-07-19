package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<User> createnewUser(@RequestBody User user) {

        User newUser = this.userService.handleCreateUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by ID")
    public ResponseEntity<User> getuserById(@PathVariable("id") long id) {
        User newUser = this.userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            // @RequestParam("current") Optional<String> currentOptional,
            // @RequestParam("pageSize") Optional<String> pageSizeOptional
            Pageable pageable) {

        // cách 1:
        /*
         *
         * 
         * String sCurrent = currentOptional.isPresent() ? currentOptional.get() : "";
         * String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() :
         * 
         * "";
         * 
         * int current = Integer.parseInt(sCurrent) - 1;
         * int pageSize = Integer.parseInt(sPageSize);
         * 
         * Pageable pageable = PageRequest.of(current, pageSize);
         */

        // cách 2:

        ResultPaginationDTO rs = this.userService.getAllUsers(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(rs);

    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user by ID")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        if (id > 100) {
            throw new IdInvalidException("Id " + id + " is invalid. Id must be less than or equal to 100.");
        }
        // Logic to delete user by id
        this.userService.deleteUserById(id);
        // For now, just return a success message
        return ResponseEntity.status(HttpStatus.OK).body("User with ID " + id + " deleted successfully.");
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") long id, @RequestBody User entity) {
        User updatedUser = this.userService.hanldeUpdateUser(id, entity);
        return ResponseEntity.ok(updatedUser);
    }
}
