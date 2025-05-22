package com.example.backend.controllers;

import com.example.backend.models.Artist;
import com.example.backend.models.Museum;
import com.example.backend.models.Painting;
import com.example.backend.models.User;
import com.example.backend.repositories.ArtistRepository;
import com.example.backend.repositories.MuseumRepository;
import com.example.backend.repositories.PaintingRepository;
import com.example.backend.tools.DataValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class PaintingController {

    @Autowired
    PaintingRepository paintingRepository;
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    MuseumRepository museumRepository;

    @GetMapping("/paintings")
    public Page getAllPaintings(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return paintingRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/paintings/{id}")
    public ResponseEntity getPainting(@PathVariable(value = "id") Long paintingId)
            throws DataValidationException
    {
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(()-> new DataValidationException("Пользователь с таким индексом не найден"));
        return ResponseEntity.ok(paintingId);
    }

    @PostMapping("/paintings")
    public ResponseEntity<Object> createPainting(@RequestBody Painting painting) throws Exception {
        try {
            Optional<Artist> artist = artistRepository.findById(painting.artist.id);
            Optional<Museum> museum = museumRepository.findById(painting.museum.id);
            artist.ifPresent(value -> painting.artist = value);
            museum.ifPresent(value -> painting.museum = value);

            Painting np = paintingRepository.save(painting);
            return new ResponseEntity<Object>(np, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("uniqname"))
                error = "countyalreadyexists";
            else
                error = "undefinederror";
            Map<String, String>
                    map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<Object>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/paintings/{id}")
    public ResponseEntity<Painting> updatePainting(@PathVariable(value = "id") Long paintingId,
                                                   @RequestBody Painting paintingDetails) {
        Painting painting = null;
        Optional<Painting> pp = paintingRepository.findById(paintingId);
        if (pp.isPresent()) {
            painting = pp.get();
            painting.name = paintingDetails.name;
            painting.year = paintingDetails.year;
            paintingRepository.save(painting);
            return ResponseEntity.ok(painting);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "painting not found");
        }
    }

    @PostMapping("/deletepaintings")
    public ResponseEntity deletePaintings(@RequestBody List<Painting> paintings) {
        paintingRepository.deleteAll(paintings);
        return new ResponseEntity(HttpStatus.OK);
    }

}