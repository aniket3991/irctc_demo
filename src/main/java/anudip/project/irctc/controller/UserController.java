package anudip.project.irctc.controller;

import anudip.project.irctc.entity.User;
import anudip.project.irctc.entity.UserVerification;
import anudip.project.irctc.model.Login;
import anudip.project.irctc.service.UserService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") User user, BindingResult result) {

        if(result.hasErrors()) {
            System.out.println(result);
            return "redirect:/registration";
        }

        User existedUser = userService.getUserByEmail(user.getEmail());

        if(existedUser != null)
            user.setUserId(existedUser.getUserId());

        if(existedUser == null || existedUser.getStatus() == 0) {
            userService.saveUserAndSentOtp(user);
            if (user.getRole().equalsIgnoreCase("user")) {
                return "redirect:/verification?email=" + user.getEmail();
            }
            return "redirect:/adminRegistration";
        }
        return "redirect:/verifiedUser";
    }

    @GetMapping("/verify/{email}")
    public String verifyUser(@PathVariable("email") String email,
                             @ModelAttribute("verification") UserVerification verification) {
        boolean isVerified = false;

        System.out.println(verification.getEmail());
        verification.setEmail(email);

        if (userService.verifyUser(verification)) {
            isVerified = true;
            return "index";
        }
        return "verification?email=" + email;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> list = userService.getAllUser();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/details/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
        User user = userService.getUserByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") int userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>("user is deleted Successfully", HttpStatus.OK);
    }
    
    @GetMapping("/login")
	public String login(Model model) {

		Login login = new Login();
		model.addAttribute("login", login);

		return "login";
	}
    
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("login") Login login, BindingResult result){
    	if(result.hasErrors())
    		return "login";
    	
        if(userService.userAuthentication(login)){
            return "home";
        }
        return "login";
    }
}