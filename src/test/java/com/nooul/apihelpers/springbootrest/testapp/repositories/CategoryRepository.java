package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.Category;

import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends BaseRepository<Category, Long> {
}
