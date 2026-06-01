package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.domain.MonitoringScheduleDomain;
import br.com.pj2.back.core.domain.StudentDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.core.gateway.StudentGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubtractMissingWorkloadUseCaseTest {
    @Mock
    private StudentGateway studentGateway;
    @InjectMocks
    private SubtractMissingWorkloadUseCase subtractMissingWorkloadUseCase;

    @Test
    void shouldSubtractMissingWorkloadWhenScheduleIsValid() {
        var schedule = mock(MonitoringScheduleDomain.class);
        var student = mock(StudentDomain.class);

        when(schedule.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(schedule.getEndTime()).thenReturn(LocalTime.of(12, 0));
        when(schedule.getMonitorRegistration()).thenReturn("student123");

        when(studentGateway.findByRegistration("student123"))
                .thenReturn(student);

        subtractMissingWorkloadUseCase.execute(schedule);

        verify(student).subtractMissingWorkload(120);
        verify(studentGateway).save(student);
    }

    @Test
    void shouldNotSubtractWhenStudentNotFound() {
        var schedule = mock(MonitoringScheduleDomain.class);

        when(schedule.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(schedule.getEndTime()).thenReturn(LocalTime.of(12, 0));
        when(schedule.getMonitorRegistration()).thenReturn("student123");

        when(studentGateway.findByRegistration("student123"))
                .thenThrow(
                        new ResourceNotFoundException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> subtractMissingWorkloadUseCase.execute(schedule)
        );

        verify(studentGateway, never()).save(any());
    }

    @Test
    void shouldHandleZeroDurationGracefully() {
        var schedule = mock(MonitoringScheduleDomain.class);
        var student = mock(StudentDomain.class);

        when(schedule.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(schedule.getEndTime()).thenReturn(LocalTime.of(10, 0));
        when(schedule.getMonitorRegistration()).thenReturn("student123");

        when(studentGateway.findByRegistration("student123"))
                .thenReturn(student);

        subtractMissingWorkloadUseCase.execute(schedule);

        verify(student).subtractMissingWorkload(0);
        verify(studentGateway).save(student);
    }
}
