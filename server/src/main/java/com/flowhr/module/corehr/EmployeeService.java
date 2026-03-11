package com.flowhr.module.corehr;

import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<EmployeeDto> getAll(int page, int size, String search) {
        return employeeRepository.findAll(page, size, search);
    }

    public int count(String search) {
        return employeeRepository.countAll(search);
    }

    public EmployeeDto getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public Long create(EmployeeRequest request) {
        return employeeRepository.save(request);
    }

    public void update(Long id, EmployeeRequest request) {
        getById(id); // ensure exists
        employeeRepository.update(id, request);
    }

    public void delete(Long id) {
        getById(id); // ensure exists
        employeeRepository.deleteById(id);
    }
}
