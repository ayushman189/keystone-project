package com.keystone.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keystone.backend.entity.Part;

public interface PartRepository extends JpaRepository<Part, Long> {
}