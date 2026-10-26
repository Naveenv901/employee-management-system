package com.example.demo.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.UserDtos;
import com.example.demo.UserEntity;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    //---------------------- SAVE USER ----------------------
    public UserEntity saveUser(UserDtos user) {
        
        user.setPassword(user.getPassword());
        return userRepository.save(convertToEntity(user));
    }
    //---------------------- SAVE MULTIPLE USERS ----------------------
    public List<UserEntity> saveUsers(List<UserDtos> users) {
        List<UserEntity> entities = users.stream()
                .map(user ->{
                    user.setPassword(user.getPassword());
                    return convertToEntity(user);
                })
                .toList();
        return userRepository.saveAll(entities);
    }

    //---------------------- GET ALL ----------------------
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }
    //--------------------------exisiting user check----------------------
    public boolean isExistingUser(String email) {
        return userRepository.existsByEmail(email);
    }
    //---------------------- UPDATE ----------------------
    public UserEntity updateUser(Long id, UserDtos newUser) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userRepository.save(updateUserEntity(existingUser, newUser));
    }

    //---------------------- DELETE ----------------------
    public void deleteUser(Long id) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(existingUser);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    //---------------------- GET BY ID ----------------------
    public UserDtos getById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDtos)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //---------------------- BULK FETCH ----------------------
    public List<UserEntity> getAllByIdList(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    //---------------------- PAGINATION ----------------------
    public Page<UserDtos> getPaginatedUsers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository
                    .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search, pageable)
                    .map(this::convertToDtos);
        }
        return userRepository.findAll(pageable).map(this::convertToDtos);
    }

    public Page<UserDtos> searchByName(String keyword, Pageable pageable) {
        return userRepository
                .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::convertToDtos);
    }

    //---------------------- CSV SAVE ----------------------
    public void saveCsvData(MultipartFile file) throws Exception {

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader()
                             .withIgnoreHeaderCase()
                             .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    UserEntity user = mapRecordToEntity(record);
                    userRepository.save(user);
                } catch (Exception e) {
                    System.out.println("Error in row: " + record.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    //---------------------- REUSABLE CSV MAPPER ----------------------
    private UserEntity mapRecordToEntity(CSVRecord record) {

        UserEntity user = new UserEntity();

        user.setName(record.get("Name"));
        user.setEmail(record.get("Email"));
        user.setPhone(record.get("Phone"));
        user.setCity(record.get("City"));
        user.setState(record.get("State"));
        user.setCountry(record.get("Country"));
        user.setDob(LocalDate.parse(record.get("DOB")));
        user.setAge(Integer.parseInt(record.get("Age")));
        user.setGender(record.get("Gender"));
        user.setAddress(record.get("Address"));

        String skillsData = record.get("Skills");
        if (skillsData != null && !skillsData.isEmpty()) {
            user.setSkills(Arrays.asList(skillsData.split("\\|")));
        }
        user.setPassword(record.get("Password"));
        user.setRole(record.get("Role"));

        return user;
    }

    //---------------------- DTO ↔ ENTITY ----------------------
    private UserDtos convertToDtos(UserEntity userEntity) {
        UserDtos dto = new UserDtos();

        dto.setId(userEntity.getId());
        dto.setName(userEntity.getName());
        dto.setAge(userEntity.getAge());
        dto.setEmail(userEntity.getEmail());
        dto.setPhone(userEntity.getPhone());
        dto.setDob(userEntity.getDob());
        dto.setGender(userEntity.getGender());
        dto.setAddress(userEntity.getAddress());
        dto.setCity(userEntity.getCity());
        dto.setState(userEntity.getState());
        dto.setCountry(userEntity.getCountry());
        dto.setSkills(userEntity.getSkills());
        dto.setPassword(userEntity.getPassword());
        dto.setRole(userEntity.getRole());
        return dto;
    }

    private UserEntity convertToEntity(UserDtos dto) {
        return updateUserEntity(new UserEntity(), dto);
    }

    //---------------------- COMMON UPDATE METHOD ----------------------
    private UserEntity updateUserEntity(UserEntity user, UserDtos dto) {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDob(dto.getDob());
        user.setAge(dto.getAge());
        user.setGender(dto.getGender());
        user.setAddress(dto.getAddress());
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setCountry(dto.getCountry());
        user.setSkills(dto.getSkills());
        if (dto.getPassword() != null) {
            String pwd = dto.getPassword().trim();

            if (!pwd.isEmpty()) {
                user.setPassword(passwordEncoder.encode(pwd));
            }
        }
        // if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
        //     emailService.sendPasswordResetEmail(dto.getEmail(), dto.getPassword());
        //     user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // }
        user.setRole(dto.getRole());
        return user;
    }
}
