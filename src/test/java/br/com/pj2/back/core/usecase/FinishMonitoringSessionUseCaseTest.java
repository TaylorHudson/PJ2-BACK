package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.domain.MonitoringScheduleDomain;
import br.com.pj2.back.core.domain.MonitoringSessionDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.ConflictException;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.core.gateway.MonitoringScheduleGateway;
import br.com.pj2.back.core.gateway.MonitoringSessionGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinishMonitoringSessionUseCaseTest {
    @Mock
    private MonitoringSessionGateway sessionGateway;
    @Mock
    private MonitoringScheduleGateway scheduleGateway;
    @InjectMocks
    private FinishMonitoringSessionUseCase finishMonitoringSessionUseCase;

    @Test
    void shouldFinishSessionSuccessfullyWhenScheduleAndSessionExist() {
        Long scheduleId = 1L;
        String registration = "monitor123";
        List<String> topics = List.of("Topic1", "Topic2");

        var schedule = mock(MonitoringScheduleDomain.class);
        var session = mock(MonitoringSessionDomain.class);

        when(scheduleGateway.findByIdAndMonitorRegistration(scheduleId, registration))
                .thenReturn(schedule);

        when(schedule.getMonitorRegistration())
                .thenReturn(registration);

        when(sessionGateway.findByMonitorAndIsStartedTrue(registration))
                .thenReturn(session);

        finishMonitoringSessionUseCase.execute(scheduleId, topics, registration);

        verify(session).finishSession(topics);
        verify(sessionGateway).save(session);
    }

    @Test
    void shouldThrowConflictExceptionWhenSessionNotFound() {
        Long scheduleId = 1L;
        String registration = "monitor123";
        List<String> topics = List.of("Topic1");

        var schedule = mock(MonitoringScheduleDomain.class);

        when(scheduleGateway.findByIdAndMonitorRegistration(scheduleId, registration))
                .thenReturn(schedule);

        when(schedule.getMonitorRegistration())
                .thenReturn(registration);

        when(sessionGateway.findByMonitorAndIsStartedTrue(registration))
                .thenThrow(
                        new ResourceNotFoundException(
                                ErrorCode.NO_STARTED_MONITORING_SESSION_WAS_FOUND
                        )
                );

        assertThrows(
                ConflictException.class,
                () -> finishMonitoringSessionUseCase.execute(scheduleId, topics, registration)
        );
    }
}
