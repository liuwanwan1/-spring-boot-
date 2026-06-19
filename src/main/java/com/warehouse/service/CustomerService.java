package com.warehouse.service;

import com.warehouse.entity.Customer;
import com.warehouse.entity.Customer.CustomerStatus;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByName(customer.getName())) {
            throw new BusinessException("客户名称已存在: " + customer.getName());
        }
        customer.setStatus(CustomerStatus.ACTIVE);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByStatus(CustomerStatus.ACTIVE);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        
        if (!customer.getName().equals(customerDetails.getName()) && 
            customerRepository.existsByName(customerDetails.getName())) {
            throw new BusinessException("客户名称已存在: " + customerDetails.getName());
        }

        customer.setName(customerDetails.getName());
        customer.setContactPerson(customerDetails.getContactPerson());
        customer.setPhone(customerDetails.getPhone());
        customer.setEmail(customerDetails.getEmail());
        customer.setAddress(customerDetails.getAddress());
        customer.setCreditLevel(customerDetails.getCreditLevel());
        customer.setCreditLimit(customerDetails.getCreditLimit());
        customer.setRemark(customerDetails.getRemark());
        
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Page<Customer> searchCustomers(String name, String status, Pageable pageable) {
        CustomerStatus customerStatus = status != null ? CustomerStatus.valueOf(status) : null;
        return customerRepository.searchCustomers(name, customerStatus, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return customerRepository.count();
    }
}
