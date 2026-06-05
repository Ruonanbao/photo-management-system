package com.example.photomanagementsystem.person.service.impl;

import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.common.CurrentUserProvider;
import com.example.photomanagementsystem.person.dto.PersonUpdateDTO;
import com.example.photomanagementsystem.person.entity.Person;
import com.example.photomanagementsystem.person.mapper.PersonMapper;
import com.example.photomanagementsystem.person.mapper.PersonPhotoMapper;
import com.example.photomanagementsystem.person.service.PersonService;
import com.example.photomanagementsystem.person.vo.PersonPhotoVO;
import com.example.photomanagementsystem.person.vo.PersonVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Person service implementation.
 */
@Service
public class PersonServiceImpl implements PersonService {

    private static final int PERSON_NAME_MAX_LENGTH = 100;

    private final PersonMapper personMapper;

    private final PersonPhotoMapper personPhotoMapper;
    private final CurrentUserProvider currentUserProvider;

    public PersonServiceImpl(PersonMapper personMapper, PersonPhotoMapper personPhotoMapper,
            CurrentUserProvider currentUserProvider) {
        this.personMapper = personMapper;
        this.personPhotoMapper = personPhotoMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<PersonVO> listPersons() {
        Long userId = getCurrentUserId();
        return personMapper.selectListByUserId(userId).stream()
                .map(this::convertToPersonVO)
                .toList();
    }

    @Override
    public PersonVO getPerson(Long id) {
        Long userId = getCurrentUserId();
        return convertToPersonVO(getPersonEntity(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonVO updatePerson(Long id, PersonUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Person person = getPersonEntity(id, userId);
        String name = updateDTO == null ? null : updateDTO.getName();
        validatePersonName(name);

        person.setName(name.trim());
        person.setUpdateTime(LocalDateTime.now());
        return convertToPersonVO(personMapper.updateByIdAndUserId(person));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerson(Long id) {
        Long userId = getCurrentUserId();
        getPersonEntity(id, userId);
        personMapper.clearFacesByPersonIdAndUserId(id, userId);
        personMapper.deleteByIdAndUserId(id, userId);
    }

    @Override
    public List<PersonPhotoVO> listPersonPhotos(Long id) {
        Long userId = getCurrentUserId();
        getPersonEntity(id, userId);
        return personPhotoMapper.selectListByPersonIdAndUserId(id, userId);
    }

    private Person getPersonEntity(Long id, Long userId) {
        if (id == null) {
            throw new BizException(400, "人物ID不能为空");
        }
        return personMapper.selectByIdAndUserId(id, userId)
                .orElseThrow(() -> new BizException(404, "人物不存在"));
    }

    private void validatePersonName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BizException(400, "人物名称不能为空");
        }
        if (name.trim().length() > PERSON_NAME_MAX_LENGTH) {
            throw new BizException(400, "人物名称长度不能超过100");
        }
    }

    private Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private PersonVO convertToPersonVO(Person person) {
        PersonVO personVO = new PersonVO();
        personVO.setId(person.getId());
        personVO.setName(person.getName());
        personVO.setCoverFaceId(person.getCoverFaceId());
        personVO.setPhotoCount(personMapper.countPhotosByPersonIdAndUserId(person.getId(), person.getUserId()));
        personVO.setCreateTime(person.getCreateTime());
        personVO.setUpdateTime(person.getUpdateTime());
        return personVO;
    }
}
