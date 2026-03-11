package com.flowhr.module.onboarding;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;

    @Transactional
    public Long startOnboarding(Long employeeId, String eventType, LocalDate startDate) {
        return onboardingRepository.createProcess(employeeId, eventType, startDate);
    }

    public List<Map<String, Object>> getProcesses(Long employeeId) {
        return onboardingRepository.findByEmployee(employeeId);
    }

    public List<Map<String, Object>> getTasksByProcess(Long processId) {
        return onboardingRepository.findTasksByProcess(processId);
    }

    public void updateTaskStatus(Long taskId, String status, String notes) {
        onboardingRepository.updateTaskStatus(taskId, status, notes);
    }
}
