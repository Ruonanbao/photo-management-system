package com.example.photomanagementsystem.person.controller;

import com.example.photomanagementsystem.common.Result;
import com.example.photomanagementsystem.person.dto.PersonUpdateDTO;
import com.example.photomanagementsystem.person.service.PersonService;
import com.example.photomanagementsystem.person.vo.PersonPhotoVO;
import com.example.photomanagementsystem.person.vo.PersonVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Person interfaces.
 */
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public Result<List<PersonVO>> listPersons() {
        return Result.success(personService.listPersons());
    }

    @GetMapping("/{id}")
    public Result<PersonVO> getPerson(@PathVariable Long id) {
        return Result.success(personService.getPerson(id));
    }

    @PutMapping("/{id}")
    public Result<PersonVO> updatePerson(@PathVariable Long id, @RequestBody PersonUpdateDTO updateDTO) {
        return Result.success(personService.updatePerson(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return Result.success();
    }

    @GetMapping("/{id}/photos")
    public Result<List<PersonPhotoVO>> listPersonPhotos(@PathVariable Long id) {
        return Result.success(personService.listPersonPhotos(id));
    }
}
