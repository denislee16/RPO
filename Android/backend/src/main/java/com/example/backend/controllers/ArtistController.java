package com.example.backend.controllers;

import com.example.backend.models.Artist;
import com.example.backend.models.Country;
import com.example.backend.repositories.ArtistRepository;
import com.example.backend.repositories.CountryRepository;
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

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class ArtistController {
    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    CountryRepository countryRepository;

    @GetMapping("/artists")
    public Page getAllArtists(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return artistRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity getArtist(@PathVariable(value = "id") Long artistId)
            throws DataValidationException
    {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(()-> new DataValidationException("Художник с таким индексом не найден"));
        return ResponseEntity.ok(artist);
    }

    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist)
            throws Exception {
        try {
            Optional<Country>
                    cc = countryRepository.findById(artist.country.id);
            if (cc.isPresent()) {
                artist.country = cc.get();
            }
            Artist nc = artistRepository.save(artist);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
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

    @PutMapping("/artists/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable(value = "id") Long artistId,
                                               @RequestBody Artist artistDetails) {
        Artist artist = null;
        Optional<Artist>
                cc = artistRepository.findById(artistId);
        if (cc.isPresent()) {
            artist = cc.get();
            artist.name = artistDetails.name;
            artistRepository.save(artist);
            return ResponseEntity.ok(artist);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "artist not found");
        }
    }

    @PostMapping("/deleteartists")
    public ResponseEntity deleteArtists(@RequestBody List <Artist>artists) {
        artistRepository.deleteAll(artists);
        return new ResponseEntity(HttpStatus.OK);
    }

//    @DeleteMapping("/artists/{id}")
//    public ResponseEntity<Object> deleteArtist(@PathVariable(value = "id") Long artistId) {
//        Optional<Artist>
//                artist = artistRepository.findById(artistId);
//        Map<String, Boolean>
//                resp = new HashMap<>();
//        if (artist.isPresent()) {
//            artistRepository.delete(artist.get());
//            resp.put("deleted", Boolean.TRUE);
//        }
//        else
//            resp.put("deleted", Boolean.FALSE);
//        return ResponseEntity.ok(resp);
//    }
}
