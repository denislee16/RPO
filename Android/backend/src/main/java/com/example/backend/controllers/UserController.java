package com.example.backend.controllers;

import com.example.backend.models.Artist;
import com.example.backend.models.Country;
import com.example.backend.models.Museum;
import com.example.backend.models.User;
import com.example.backend.repositories.MuseumRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.tools.DataValidationException;
import com.example.backend.tools.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MuseumRepository museumRepository;

    @GetMapping("/users")
    public List getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody User user)
            throws Exception {
        try {
            User nc = userRepository.save(user);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        }
        catch(Exception ex) {
            String error;
            if (ex.getMessage().contains("users.name_UNIQUE"))
                error = "countyalreadyexists";
            else
                error = "undefinederror";
            Map<String, String>
                    map =  new HashMap<>();
            map.put("error", error);
            //return new ResponseEntity<Object> (map, HttpStatus.OK);
            return ResponseEntity.ok(map);
        }
    }

    @PostMapping("/users/{id}/addmuseums")
    public ResponseEntity<Object> addMuseums(@PathVariable(value = "id") Long userId,
                                             @RequestBody Set<Museum> museums) {
        Optional<User> uu = userRepository.findById(userId);
        int cnt = 0;
        if (uu.isPresent()) {
            User u = uu.get();
            for (Museum m : museums) {
                Optional<Museum>
                        mm = museumRepository.findById(m.id);
                if (mm.isPresent()) {
                    u.addMuseum(mm.get());
                    cnt++;
                }
            }
            userRepository.save(u);
        }
        Map<String, String> response = new HashMap<>();
        response.put("count", String.valueOf(cnt));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{id}/removemuseums")
    public ResponseEntity<Object> removeMuseums(@PathVariable(value = "id") Long userId,
                                                @RequestBody Set<Museum> museums) {
        Optional<User> uu = userRepository.findById(userId);
        int cnt = 0;
        if (uu.isPresent()) {
            User u = uu.get();
            for (Museum m : u.museums) {
                u.removeMuseum(m);
                cnt++;
            }
            userRepository.save(u);
        }
        Map<String, String> response = new HashMap<>();
        response.put("count", String.valueOf(cnt));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity updateUser(@PathVariable(value = "id") Long userId,
                                     @RequestBody User userDetails)
            throws DataValidationException
    {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new DataValidationException(" Пользователь с таким индексом не найден"));
            user.email = userDetails.email;
            String np = userDetails.np;
            if (np != null  && !np.isEmpty()) {
                byte[] b = new byte[32];
                new Random().nextBytes(b);
                String salt = new String(Hex.encode(b));
                user.password = Utils.ComputeHash(np, salt);
                user.salt = salt;
            }
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("users.email_UNIQUE"))
                throw new DataValidationException("Пользователь с такой почтой уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") Long userId) {
        Optional<User> user = userRepository.findById(userId);
        Map<String, Boolean> resp = new HashMap<>();
        if (user.isPresent()) {
            userRepository.delete(user.get());
            resp.put("deleted", Boolean.TRUE);
        } else
            resp.put("deleted", Boolean.FALSE);
        return ResponseEntity.ok(resp);
    }
}
