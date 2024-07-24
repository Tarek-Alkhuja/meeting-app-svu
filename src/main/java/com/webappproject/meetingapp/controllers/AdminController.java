package com.webappproject.meetingapp.controllers;



import com.webappproject.meetingapp.models.Role;
import com.webappproject.meetingapp.models.User;
import com.webappproject.meetingapp.repositories.RoleRepository;
import com.webappproject.meetingapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class AdminController {
    private UserService userService;
    private RoleRepository roleRepository;

    @Autowired
    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    // display list of users
    @GetMapping("/admin/home")
    public String viewAdminHomePage(Model model) {

        return findPaginated(1, "fullName", "asc", model);
    }

    @GetMapping("/admin/search")
    public String searchUsersByEmail(@RequestParam("email") String email, Model model) {
        List<User> listUsers = userService.searchUsersByEmail(email);
        model.addAttribute("listUsers", listUsers);
        return "/admin/adminHome";
    }

    @GetMapping("/admin/createUserForm")
    public String createUserForm(Model model) {
        // create model attribute to bind form data
        User user = new User();
        model.addAttribute("user", user);
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "/admin/create-user";
    }

    @PostMapping("/admin/saveUser")
    public String createUser(@ModelAttribute User user, Model model) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            model.addAttribute("error", "Enter your full name");
        } else if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Enter your email");
        } else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Enter your password");
        } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
            model.addAttribute("error", "Select a role");
        } else {
            userService.saveUser(user);
            return "redirect:/admin/home";
        }

        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "/admin/create-user";
    }


    @GetMapping("/admin/updateUser/{id}")
    public String UpdateUserForm(@PathVariable( value = "id") long id, Model model) {
        // get user from the service
        User user = userService.getUserById(id);
        // set user as a model attribute to pre-populate the form
        model.addAttribute("user", user);
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "/admin/update-user";
    }

    @PostMapping("/admin/update")
    public String updateUser(@RequestParam Long userId, @ModelAttribute User user, Model model) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            model.addAttribute("error", "Enter your full name");
        } else if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Enter your email");
        } else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Enter your password");
        } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
            model.addAttribute("error", "Select a role");
        } else {
            userService.updateUser(userId, user);
            return "redirect:/admin/home";
        }
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "/admin/update-user";
    }



    @GetMapping("/admin/deleteUser/{id}")
    public String deleteUser(@PathVariable (value = "id") long id) {
        // call delete user method
        this.userService.deleteUserById(id);
        return "redirect:/admin/home";
    }


    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable (value = "pageNo") int pageNo,
                                @RequestParam("sortField") String sortField,
                                @RequestParam("sortDir") String sortDir,
                                Model model) {
        int pageSize = 5;

        Page<User> page = userService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<User> listUsers= page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listUsers", listUsers);
        return "/admin/adminHome";
    }
}

