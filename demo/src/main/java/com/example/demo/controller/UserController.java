package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.UserDtos;
import com.example.demo.UserEntity;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    //---------------------- LOGIN ----------------------
    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpSession session) {

    String email = data.get("email");
    String password = data.get("password");
    logger.info("Login attempt: email={}, password={}", email, password);
   
    // Admin login
    if ("admin@gmail.com".equals(email) && "admin123".equals(password)) {

        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("userName", "Admin");

        // set authentication
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null,List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(
    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
    SecurityContextHolder.getContext()
);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "role", "ADMIN",
                "email", email,
                "name", "Admin"
        ));
    }
    // DB user login
    UserEntity user = userService.getAll().stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(email) && passwordEncoder.matches(password, u.getPassword()))
            .findFirst()
            .orElse(null);
    logger.info("Login attempt: email={}, success={}", email, user );
    System.out.println(passwordEncoder.matches(passwordEncoder.encode("123456"),("$2a$10$ym7chLmAv/8X957klBQose014vVvJ1TS4nTqM.PPUdZICB2QQNYOS")));
    if (user != null) {
        
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getName());

        // IMPORTANT: set authentication
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null,List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));

        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(
    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
    SecurityContextHolder.getContext()
);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "role", user.getRole(),
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }

    return ResponseEntity.status(401).body("Invalid email or password");
}

    //---------------------- SESSION CHECK ----------------------
    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");
        String name = (String) session.getAttribute("userName");
        if (email == null || role == null ) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        return ResponseEntity.ok(Map.of(
                "email", email,
                "role", role,
                "name", name
        ));
    }

    //---------------------- LOGOUT ----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        session.invalidate();
        logger.info("Logout success: {}", email);
        return ResponseEntity.ok("Logged out successfully");
    }
    // ---------------------- GET USER PROFILE ----------------------
    @GetMapping("/user/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        String email = (String) session.getAttribute("userEmail"); // set this on login
        if (email == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        UserEntity user = userService.getAll().stream()
                                .filter(u -> u.getEmail().equals(email))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    // ---------------------- GET USERS ----------------------
    @GetMapping("/biodata")
    public Page<UserDtos> getAll(@RequestParam(required = false) String search, Pageable pageable) {
        return userService.getPaginatedUsers(search, pageable);
    }

    //---------------------- CREATE USER ----------------------
    @PostMapping("/submit")
    public ResponseEntity<?> submitData(@Valid @RequestBody UserDtos user) {

        userService.saveUser(user);
        return ResponseEntity.ok("Data saved successfully!");
    }

    //---------------------- UPDATE USER ----------------------
    @PutMapping("/biodata/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UserDtos user) {

        userService.updateUser(id, user);
        return ResponseEntity.ok("Data updated successfully!");
    }

    //---------------------- DELETE USER ----------------------
    @DeleteMapping("/biodata/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Deleted ID: " + id);
    }

    //---------------------- DELETE ALL ----------------------
    @DeleteMapping("/biodata/delete-all")
    public ResponseEntity<?> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.ok("All records deleted");
    }

    //---------------------- GET BY ID ----------------------
    @GetMapping("/biodata/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    //---------------------- SEARCH ----------------------
    @GetMapping("/biodata/search")
    public Page<UserDtos> search(@RequestParam String keyword, Pageable pageable) {
        return userService.searchByName(keyword, pageable);
    }

    //---------------------- CSV UPLOAD ----------------------
    
    @PostMapping("/upload/csv")
public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
    logger.info("Received file: {}", file.getOriginalFilename());
    // 1. File validation
    if (file.isEmpty()) {
        logger.warn("File is empty");
        return ResponseEntity.badRequest().body("File is empty");
    }

    if (!file.getOriginalFilename().endsWith(".csv")) {
        logger.warn("Invalid file type: {}", file.getOriginalFilename());
        return ResponseEntity.badRequest().body("Only CSV files are allowed");
    }

    List<UserDtos> validUsers = new ArrayList<>();
    List<String[]> errorRows = new ArrayList<>();
    String[] head = {
        "Name","Age","Email","Phone","DOB",
        "Gender","Address","City","State","Country","Skills","Password","Role", "Error"
    };

    errorRows.add(head);
    
    try (
 BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
    CSVParser parser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader()) ) {

        List<String> headers = new ArrayList<>(parser.getHeaderNames());
        headers.remove("ID");
        List<String> expectedHeaders = List.of(
            "Name","Age","Email","Phone","DOB",
            "Gender","Address","City","State","Country","Skills","Password","Role"
        );

        if (!headers.equals(expectedHeaders)) {
            logger.warn("Invalid CSV header format: {}", headers);
            return ResponseEntity.badRequest().body("Invalid CSV header format");
        }
        int rowNumber = 1;
        Set<String> existingEmails = new HashSet<>();

        
        userService.getAll().forEach(u -> existingEmails.add(u.getEmail()));
      for(CSVRecord record : parser) {
            rowNumber++;
            String name="", ageStr="", email="", phone="", dobStr="", gender="", address="", city="", state="", country="", skillsStr="", password="", role="" ;
            try {
                 name = record.get("Name").trim();
                 ageStr = record.get("Age").trim();
                 email = record.get("Email").trim();
                 phone = record.get("Phone").trim();
                 dobStr = record.get("DOB").trim();
                 gender = record.get("Gender").trim();
                 address = record.get("Address").trim();
                 city = record.get("City").trim();
                 state = record.get("State").trim();
                 country = record.get("Country").trim();
                skillsStr = record.get("Skills").trim();
                password = record.get("Password").trim();
                role = record.get("Role").trim();
                if (name.isEmpty()) throw new RuntimeException("Name is required");

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new RuntimeException("Invalid email");
            }

            if (!phone.matches("^[0-9]{10}$")) {
                throw new RuntimeException("Invalid phone");
            }

            if (existingEmails.contains(email)) {
                logger.warn("Email already exists: {}", email);
                throw new RuntimeException("Email already exists");
            }
            if(password.isEmpty()) throw new RuntimeException("Password is required");
            if(role.isEmpty()) throw new RuntimeException("Role is required");

            // AGE
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0 || age > 120) {
                    throw new RuntimeException("Invalid age");
                }
            } catch (Exception e) {
                throw new RuntimeException("Age must be a number"+ e.getMessage());
            }

            // DOB
            LocalDate dob;
            try {
                dob = LocalDate.parse(dobStr);
                if (dob.isAfter(LocalDate.now())) {
                    throw new RuntimeException("DOB cannot be future");
                }
            } catch (Exception e) {
                throw new RuntimeException("Invalid DOB format (yyyy-MM-dd)"+  e.getMessage());
            }               

                // 6. Convert to Entity
                UserDtos user = new UserDtos();
                user.setName(name);
                user.setAge(age);
                user.setDob(dob);
                user.setEmail(email);
                user.setPhone(phone);
                user.setGender(gender);
                user.setAddress(address);
                user.setCity(city);
                user.setState(state);
                user.setCountry(country);

                List<String> skills = skillsStr.isEmpty() ? new ArrayList<>() : Arrays.asList(skillsStr.split("\\|"));
                user.setSkills(skills);
                user.setPassword(password);
                user.setRole(role);

                validUsers.add(user);
                existingEmails.add(email);

            } catch (Exception e) {
                  String[] errorRow = new String[]{
                name, ageStr, email, phone,dobStr,gender, address, city, state, country, skillsStr, password, role,
                "Row " + rowNumber + ": " + e.getMessage()
            };
                logger.warn("Error in row {}: {}", rowNumber, e.getMessage());
                errorRows.add(errorRow);
            }
        }

        // 8. Save valid users
        if (!validUsers.isEmpty()) {
            userService.saveUsers(validUsers);
        }

        // 9. If errors → send mail
        if (errorRows.size()>1) {

            byte[] csvBytes = generateErrorCsv(errorRows);
            byte[] pdfBytes = generateErrorPdf(errorRows);
            logger.info("Generated error report with {} rows", errorRows.size()-1);
            mailService.sendMailWithAttachments(
                    "naveenvajjiravel@gmail.com",   
                    "CSV Upload Errors Report",
                    "Some records failed during upload. Please check attached CSV and PDF.",
                    csvBytes,
                    pdfBytes
            );
        }

        return ResponseEntity.ok(
                "Upload completed. Success: " + validUsers.size() +
                ", Failed: " + (errorRows.size()-1)
        );

    } catch (Exception e) {
        logger.error("Failed to process CSV file: " + e);
        throw new RuntimeException("Error processing file"+ e.getClass().getSimpleName());
    }
}

//---------------------- GENERATE ERROR REPORTS ----------------------
        //----------------------- GENERATE ERROR PDF ----------------------
        private byte[] generateErrorPdf(List<String[]> errorRows) throws Exception {

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("CSV Upload Errors Report").setFontSize(9).setBold());
            Table table = new Table(UnitValue.createPercentArray(errorRows.get(0).length))
        .useAllAvailableWidth();
            document.setMargins(10, 10, 10, 10);
            for (String[] row : errorRows) {
                for (String col : row) {
                    table.addCell(
                        new Cell().add(
                            new Paragraph(col).setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT)
                        )
                    );
                }
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        }

        //---------------------- GENERATE ERROR CSV ----------------------
        private byte[] generateErrorCsv(List<String[]> errorRows) throws IOException {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            for (String[] row : errorRows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }

            writer.flush();
            return out.toByteArray();
}

    //---------------------- DOWNLOAD CSV (ALL) ----------------------
    @GetMapping("/download/csv")
    public void downloadCsv(HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=biodata.csv");

        List<UserEntity> users = userService.getAll();
        PrintWriter writer = response.getWriter();

        writeCsv(writer, users);

        writer.flush();
        writer.close();
    }

    //---------------------- DOWNLOAD CSV (SELECTED) ----------------------
    @PostMapping("/download/csv/selected")
    public void downloadSelectedCsv(@RequestBody List<Long> ids,
                                    HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=selected.csv");

        List<UserEntity> users = userService.getAllByIdList(ids);
        PrintWriter writer = response.getWriter();

        writeCsv(writer, users);

        writer.flush();
        writer.close();
    }

    //---------------------- SAFE CSV ----------------------
    private String safe(String value) {
        if (value == null) return "";
        return value.replaceAll("^[=+\\-@]", "'$0");
    }

    //---------------------- CSV WRITER ----------------------
    private void writeCsv(PrintWriter writer, List<UserEntity> users) {

        writer.println("ID,Name,Email,Phone,DOB,Age,Gender,Address,City,State,Country,Skills");

        for (UserEntity user : users) {

            String skills = user.getSkills() != null
                    ? String.join("|", user.getSkills())
                    : "";

            writer.println(
                    user.getId() + "," +
                    "\"" + safe(user.getName()) + "\"," +
                    "\"" + safe(user.getEmail()) + "\"," +
                    "\"" + safe(user.getPhone()) + "\"," +
                    "\"" + user.getDob() + "\"," +
                    "\"" + user.getAge() + "\"," +
                    "\"" + safe(user.getGender()) + "\"," +
                    "\"" + safe(user.getAddress()) + "\"," +
                    "\"" + safe(user.getCity()) + "\"," +
                    "\"" + safe(user.getState()) + "\"," +
                    "\"" + safe(user.getCountry()) + "\"," +
                    "\"" + skills + "\""
            );
        }
    }

    //---------------------- DOWNLOAD PDF ----------------------
    @GetMapping("/download/pdf")
    public void downloadPdf(HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=biodata.pdf");

        List<UserEntity> users = userService.getAll();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Biodata List"));
        document.add(createPdfTable(users));

        document.close();
    }
    @PostMapping("/download/pdf/selected")
    public void downloadSelectedPdf(@RequestBody List<Long> ids,
                                HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=selected_biodata.pdf");

        List<UserEntity> users = userService.getAllByIdList(ids);

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Selected Biodata"));
        document.add(createPdfTable(users));

        document.close();
    }
    //---------------------- PDF TABLE ----------------------
    private Table createPdfTable(List<UserEntity> users) throws IOException {

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        float[] widths = {1,2,3,3,3,1,2,4,2,2,2,5};

        Table table = new Table(widths);
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"ID","Name","Email","Phone","DOB","Age","Gender",
                "Address","City","State","Country","Skills"};

        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(bold).setFontSize(9)));
        }

        for (UserEntity user : users) {

            String skills = user.getSkills() != null
                    ? String.join("\n", user.getSkills())
                    : "";

            table.addCell(new Cell().add(new Paragraph(String.valueOf(user.getId())).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getName()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getEmail()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getPhone()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add( new Paragraph( user.getDob() != null    ? user.getDob().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : "" ).setFont(normal).setFontSize(8) ) );

            table.addCell(new Cell().add(new Paragraph(user.getAge().toString()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getGender()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getAddress()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getCity()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getState()).setFont(normal).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(user.getCountry()).setFont(normal).setFontSize(8)));

            table.addCell(new Cell().add(new Paragraph(skills).setFont(normal).setFontSize(8)));
        }

        return table;
    }

  
    
}