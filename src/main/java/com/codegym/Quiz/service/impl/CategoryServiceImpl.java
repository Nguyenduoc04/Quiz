package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Override
    public List<?> getAllCategories() {
        return Collections.emptyList();
    }
}