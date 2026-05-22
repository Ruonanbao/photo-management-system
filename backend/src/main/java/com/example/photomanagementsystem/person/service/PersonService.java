package com.example.photomanagementsystem.person.service;

import com.example.photomanagementsystem.person.dto.PersonUpdateDTO;
import com.example.photomanagementsystem.person.vo.PersonPhotoVO;
import com.example.photomanagementsystem.person.vo.PersonVO;

import java.util.List;

/**
 * Person service.
 */
public interface PersonService {

    List<PersonVO> listPersons();

    PersonVO getPerson(Long id);

    PersonVO updatePerson(Long id, PersonUpdateDTO updateDTO);

    void deletePerson(Long id);

    List<PersonPhotoVO> listPersonPhotos(Long id);
}
