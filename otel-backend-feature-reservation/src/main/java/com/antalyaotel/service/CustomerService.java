package com.antalyaotel.service;

import com.antalyaotel.dto.CustomerDTO;
import com.antalyaotel.model.Customer;
import com.antalyaotel.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (customerRepository.existsByIdNumber(customerDTO.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists");
        }

        Customer customer = convertToEntity(customerDTO);
        return convertToDTO(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        // Email değişikliği varsa kontrol et
        if (!customer.getEmail().equals(customerDTO.getEmail()) && 
            customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // ID number değişikliği varsa kontrol et
        if (!customer.getIdNumber().equals(customerDTO.getIdNumber()) && 
            customerRepository.existsByIdNumber(customerDTO.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists");
        }

        updateCustomerFromDTO(customer, customerDTO);
        return convertToDTO(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDTO updateCustomerProfile(String token, CustomerDTO customerDTO) {
        // Token'dan e-posta adresini çıkar
        String email = token.substring(7); // "Bearer " prefix'ini kaldır
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // Email değişikliği varsa kontrol et
        if (!customer.getEmail().equals(customerDTO.getEmail()) && 
            customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // ID number değişikliği varsa kontrol et
        if (!customer.getIdNumber().equals(customerDTO.getIdNumber()) && 
            customerRepository.existsByIdNumber(customerDTO.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists");
        }

        // Şifre değişikliği varsa güncelle
        if (customerDTO.getPassword() != null && !customerDTO.getPassword().isEmpty()) {
            customer.setPassword(customerDTO.getPassword());
        }

        updateCustomerFromDTO(customer, customerDTO);
        return convertToDTO(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setActive(false);
        customerRepository.save(customer);
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }

    private Customer convertToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        return customer;
    }

    private void updateCustomerFromDTO(Customer customer, CustomerDTO dto) {
        BeanUtils.copyProperties(dto, customer);
    }
} 