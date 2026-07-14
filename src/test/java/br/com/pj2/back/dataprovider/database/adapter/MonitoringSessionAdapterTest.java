package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.domain.MonitoringSessionDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.dataprovider.database.entity.MonitoringEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringScheduleEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringSessionEntity;
import br.com.pj2.back.dataprovider.database.entity.StudentEntity;
import br.com.pj2.back.dataprovider.database.repository.MonitoringRepository;
import br.com.pj2.back.dataprovider.database.repository.MonitoringSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringSessionAdapterTest {

    @Mock
    private MonitoringSessionRepository monitoringSessionRepository;

    @Mock
    private MonitoringRepository monitoringRepository;

    @InjectMocks
    MonitoringSessionAdapter monitoringSessionAdapter;

    @Test
    void shouldSaveMonitoringSessionSuccessfully() {
        MonitoringSessionDomain domain = MonitoringSessionDomain.builder()
                .id(1L)
                .monitor("123")
                .monitoring("Matemática")
                .monitoringSchedule(1L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .topics(List.of("Álgebra", "Geometria"))
                .isStarted(true)
                .build();

        MonitoringEntity monitoringEntity = MonitoringEntity.builder().id(1L).build();
        when(monitoringRepository.findByName("Matemática")).thenReturn(Optional.of(monitoringEntity));
        when(monitoringSessionRepository.save(any(MonitoringSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MonitoringSessionDomain saved = monitoringSessionAdapter.save(domain);

        assertNotNull(saved);
        assertEquals("123", saved.getMonitor());
        assertEquals(2, saved.getTopics().size());
        assertEquals("Álgebra", saved.getTopics().get(0));
        assertEquals("Geometria", saved.getTopics().get(1));

    }

    @Test
    void shouldThrowWhenMonitoringNotFoundOnSave() {
        MonitoringSessionDomain domain = MonitoringSessionDomain.builder()
                .monitor("123")
                .monitoring("NãoExiste")
                .build();

        when(monitoringRepository.findByName("NãoExiste")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> monitoringSessionAdapter.save(domain));

        assertEquals(ErrorCode.MONITORING_NOT_FOUND.getErrorCode(), exception.getErrorCode().getErrorCode());
        assertEquals(ErrorCode.MONITORING_NOT_FOUND.getMessage(), exception.getErrorCode().getMessage());

    }

    @Test
    void shouldFindByMonitorAndIsStartedTrue() {
        MonitoringSessionEntity entity = buildSessionEntity();
        when(monitoringSessionRepository.findByMonitorRegistrationAndIsStartedTrue("123"))
                .thenReturn(Optional.of(entity));

        MonitoringSessionDomain result = monitoringSessionAdapter.findByMonitorAndIsStartedTrue("123");

        assertNotNull(result);
        assertEquals("123", result.getMonitor());
        assertTrue(result.isStarted());
    }

    @Test
    void shouldThrowWhenNoStartedSessionFound() {
        when(monitoringSessionRepository.findByMonitorRegistrationAndIsStartedTrue("123"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> monitoringSessionAdapter.findByMonitorAndIsStartedTrue("123"));

        assertEquals(ErrorCode.NO_STARTED_MONITORING_SESSION_WAS_FOUND.getErrorCode(), exception.getErrorCode().getErrorCode());
        assertEquals(ErrorCode.NO_STARTED_MONITORING_SESSION_WAS_FOUND.getMessage(), exception.getErrorCode().getMessage());

    }

    @Test
    void shouldCountTopicsInSessionMonitoring() {
        MonitoringDomain monitoring = MonitoringDomain.builder()
                .id(1L)
                .topics(List.of("Álgebra", "Geometria", "Trigonometria"))
                .build();

        List<MonitoringSessionEntity> sessions = List.of(
                MonitoringSessionEntity.builder().topics("Álgebra,Geometria").monitoring(MonitoringEntity.builder().id(1L).build()).build(),
                MonitoringSessionEntity.builder().topics("Álgebra").monitoring(MonitoringEntity.builder().id(1L).build()).build()
        );

        when(monitoringSessionRepository.findAll()).thenReturn(sessions);

        HashMap<String, Integer> count = monitoringSessionAdapter.countTopicsInSessionMonitoring(monitoring);

        assertEquals(3, count.size());
        assertEquals(2, count.get("Álgebra"));
        assertEquals(1, count.get("Geometria"));
        assertEquals(0, count.get("Trigonometria"));
    }

    @Test
    void shouldCountTopicsInSessionMonitoringIsEmpty() {
        MonitoringDomain monitoring = MonitoringDomain.builder()
                .id(1L)
                .topics(new ArrayList<>())
                .build();

        List<MonitoringSessionEntity> sessions = List.of(
                MonitoringSessionEntity.builder().topics(null).monitoring(MonitoringEntity.builder().id(1L).build()).build(),
                MonitoringSessionEntity.builder().topics(null).monitoring(MonitoringEntity.builder().id(1L).build()).build()
        );

        when(monitoringSessionRepository.findAll()).thenReturn(sessions);

        HashMap<String, Integer> count = monitoringSessionAdapter.countTopicsInSessionMonitoring(monitoring);

        assertEquals(0, count.size());
    }

    private MonitoringSessionEntity buildSessionEntity() {
        return MonitoringSessionEntity.builder()
                .id(1L)
                .monitor(StudentEntity.builder().registration("123").build())
                .monitoring(MonitoringEntity.builder().name("Matemática").build())
                .monitoringSchedule(Mockito.mock(MonitoringScheduleEntity.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .topics("Álgebra,Geometria")
                .isStarted(true)
                .build();
    }

    @Test
    void shouldReturnWorkedHoursByMonth() {
        // Arrange
        String registration = "202312345";
        Integer month = 7;
        Integer year = 2026;

        when(monitoringSessionRepository.sumMonitoringSecondsByMonth(
                registration, month, year))
                .thenReturn(9000L); // 2h30min

        // Act
        Duration result = monitoringSessionAdapter.getWorkedHoursByMonth(
                registration, month, year);

        // Assert
        assertEquals(Duration.ofHours(2).plusMinutes(30), result);

        verify(monitoringSessionRepository)
                .sumMonitoringSecondsByMonth(registration, month, year);
    }


    @Test
    void shouldReturnZeroDurationWhenNoWorkedHours() {
        // Arrange
        String registration = "202312345";
        Integer month = 7;
        Integer year = 2026;

        when(monitoringSessionRepository.sumMonitoringSecondsByMonth(
                registration, month, year))
                .thenReturn(0L);

        // Act
        Duration result = monitoringSessionAdapter.getWorkedHoursByMonth(
                registration, month, year);

        // Assert
        assertEquals(Duration.ZERO, result);

        verify(monitoringSessionRepository)
                .sumMonitoringSecondsByMonth(registration, month, year);
    }

}
