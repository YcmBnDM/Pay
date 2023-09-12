package com.example.pay.service;

import com.example.exception.BizException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;


/**
 * 通用 Validator
 */
@Service
public class ValidateService {

    @Resource
    private Validator validator;

    public void validate(Object obj) {

        Set<ConstraintViolation<Object>> resultSet = validator.validate(obj);
        if (resultSet == null || resultSet.isEmpty()) {
            return;
        }
        resultSet.stream().forEach(item -> {
            throw new BizException(item.getMessage());
        });
    }

}
