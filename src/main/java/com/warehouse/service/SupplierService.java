package com.warehouse.service;

import com.warehouse.entity.Supplier;
import com.warehouse.entity.Supplier.SupplierStatus;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new BusinessException("供应商名称已存在: " + supplier.getName());
        }
        supplier.setStatus(SupplierStatus.ACTIVE);
        return supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByStatus(SupplierStatus.ACTIVE);
    }

    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = getSupplierById(id);
        
        if (!supplier.getName().equals(supplierDetails.getName()) && 
            supplierRepository.existsByName(supplierDetails.getName())) {
            throw new BusinessException("供应商名称已存在: " + supplierDetails.getName());
        }

        supplier.setName(supplierDetails.getName());
        supplier.setContactPerson(supplierDetails.getContactPerson());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setTaxId(supplierDetails.getTaxId());
        supplier.setBankAccount(supplierDetails.getBankAccount());
        supplier.setRemark(supplierDetails.getRemark());
        
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Page<Supplier> searchSuppliers(String name, String status, Pageable pageable) {
        SupplierStatus supplierStatus = status != null ? SupplierStatus.valueOf(status) : null;
        return supplierRepository.searchSuppliers(name, supplierStatus, pageable);
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return supplierRepository.count();
    }
}
