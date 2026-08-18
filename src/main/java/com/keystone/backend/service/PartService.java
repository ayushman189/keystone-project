package com.keystone.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.PartRequest;
import com.keystone.backend.dto.PartResponse;
import com.keystone.backend.entity.Part;
import com.keystone.backend.exception.ResourceNotFoundException;
import com.keystone.backend.repository.PartRepository;

@Service
public class PartService {

    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts() {
        return partRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartResponse getPartById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Part not found with id: " + id));
        return toResponse(part);
    }

    @Transactional
    public PartResponse createPart(PartRequest request) {
        Part part = new Part();
        part.setName(request.getName());
        part.setStockQuantity(request.getStockQuantity());
        part.setUnitCost(request.getUnitCost());
        Part saved = partRepository.save(part);
        return toResponse(saved);
    }

    @Transactional
    public PartResponse updatePart(Long id, PartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Part not found with id: " + id));
        part.setName(request.getName());
        part.setStockQuantity(request.getStockQuantity());
        part.setUnitCost(request.getUnitCost());
        Part saved = partRepository.save(part);
        return toResponse(saved);
    }

    @Transactional
    public void deletePart(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Part not found with id: " + id));
        partRepository.delete(part);
    }

    private PartResponse toResponse(Part part) {
        return new PartResponse(
                part.getId(),
                part.getName(),
                part.getStockQuantity(),
                part.getUnitCost()
        );
    }
}
