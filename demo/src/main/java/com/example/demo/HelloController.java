// package com.example.demo;

// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;


// @RestController
// @CrossOrigin(origins = "*") // allow React
// @RequestMapping("/api")
// public class HelloController {

//     @GetMapping("/hello")
//     public String sayHello() {
//         return "Hello this a welcome message";
//     }

//     @GetMapping("/bye")
//     public String sayBye() {
//         return "Good Bye";
//     }

//     @GetMapping("/user")
//     public UserDTO getUser(){
//        return new UserDTO("John Doe", 30);
//     }

//     // @PostMapping("/user")
//     // public UserDTO createUser(@RequestBody UserDTO user) {
//     //     return user;
//     // }

//     // @PostMapping("/submit")
//     // public String postMethodName(@RequestBody UserDtos user) {
//     //     System.out.println("Received data: "+" /nName : " + user.getName() + "/n Age : " + user.getAge() + "/n Email : " + user.getEmail() + "/n Email : " + user.getEmail() + "/n Phone : " + user.getPhone() + "/n DOB : " + user.getDob() + "/n Gender : " + user.getGender() + "/n Address : " + user.getAddress() + "/n City : " + user.getCity() + "/n State : " + user.getState() + "/n Country : " + user.getCountry() + "/n Skills : " + user.getSkills() + "/n Password : " + user.getPassword());
//     //     return "Data received successfully";
//     // }


// @PostMapping("/submit")
// public String submitData(@RequestBody(required = false) UserDtos user) {
//     if (user == null) {
//         return "No data received";
//     }

//     System.out.println(user.toString());
//     return user.toString();
    
    
// }
// }