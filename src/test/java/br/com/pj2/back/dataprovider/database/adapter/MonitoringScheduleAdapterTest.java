package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.MonitoringScheduleDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.domain.enumerated.MonitoringScheduleStatus;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.dataprovider.database.entity.MonitoringEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringScheduleEntity;
import br.com.pj2.back.dataprovider.database.entity.StudentEntity;
import br.com.pj2.back.dataprovider.database.repository.MonitoringRepository;
import br.com.pj2.back.dataprovider.database.repository.MonitoringScheduleRepository;
import br.com.pj2.back.dataprovider.database.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringScheduleAdapterTest {

    @Mock
    private MonitoringScheduleRepository monitoringScheduleRepository;

    @Mock
    private MonitoringRepository monitoringRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private MonitoringScheduleAdapter monitoringScheduleAdapter;

    @Test
    void shouldBringAnEmptyListWhenLookingForRequests(){

        when(monitoringScheduleRepository.findByTeacherRegistrationAndStatus(anyString(), anyString())).thenReturn(new ArrayList<>());
        List<MonitoringScheduleDomain> response = monitoringScheduleAdapter.findByTeacherRegistrationAndStatus("teste", MonitoringScheduleStatus.APPROVED);

        assertEquals(0, response.size());
    }

    @Test
    void mustBringListWithPeddingRequests(){
        List<MonitoringScheduleEntity> mock= new ArrayList<>();
        mock.add(buildMonitoringScheduleEntity());

        when(monitoringScheduleRepository.findByTeacherRegistrationAndStatus(anyString(), anyString())).thenReturn(mock);
        List<MonitoringScheduleDomain> response = monitoringScheduleAdapter.findByTeacherRegistrationAndStatus("teste", MonitoringScheduleStatus.PENDING);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());

        MonitoringScheduleDomain schedule = response.get(0);

        assertEquals(MonitoringScheduleStatus.PENDING.getDescription(), schedule.getStatus().getDescription());
        assertEquals("Monitor Teste", schedule.getMonitor());
        assertEquals("Matemática", schedule.getMonitoring());
        assertEquals(DayOfWeek.MONDAY, schedule.getDayOfWeek());
        assertEquals(LocalTime.of(14, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(15, 0), schedule.getEndTime());
        assertNotNull(schedule.getRequestedAt());
    }

    @Test
    void shouldFindByMonitorRegistration() {
        List<MonitoringScheduleEntity> mock = new ArrayList<>();
        mock.add(buildMonitoringScheduleEntity());

        when(monitoringScheduleRepository.findByMonitorRegistrationAndStatus(
                "123",
                MonitoringScheduleStatus.APPROVED))
                .thenReturn(mock);

        List<MonitoringScheduleDomain> response =
                monitoringScheduleAdapter.findByMonitorRegistration("123");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("123", response.get(0).getMonitorRegistration());
    }

    @Test
    void shouldFindByIdAndMonitorRegistration() {
        MonitoringScheduleEntity entity = buildMonitoringScheduleEntity();
        when(monitoringScheduleRepository.findByIdAndMonitorRegistrationAndStatus(1L, "123", MonitoringScheduleStatus.APPROVED))
                .thenReturn(java.util.Optional.of(entity));

        MonitoringScheduleDomain domain = monitoringScheduleAdapter.findByIdAndMonitorRegistration(1L, "123");

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals("Monitor Teste", domain.getMonitor());
    }

    @Test
    void shouldFindById() {
        MonitoringScheduleEntity entity = buildMonitoringScheduleEntity();
        when(monitoringScheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(entity));

        MonitoringScheduleDomain domain = monitoringScheduleAdapter.findById(1L);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals("Monitor Teste", domain.getMonitor());
    }

    @Test
    void shouldCheckIfExistsByDayOfWeekAndTimeRangeAndStatusIn() {
        when(monitoringScheduleRepository.existsByDayOfWeekAndTimeRangeAndStatusIn(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                List.of(MonitoringScheduleStatus.APPROVED)
        )).thenReturn(true);

        boolean exists = monitoringScheduleAdapter.existsByDayOfWeekAndTimeRangeAndStatusIn(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                List.of(MonitoringScheduleStatus.APPROVED)
        );

        assertTrue(exists);
    }

    @Test
    void shouldThrowWhenFindByIdAndMonitorRegistrationNotFound() {
        when(monitoringScheduleRepository.findByIdAndMonitorRegistrationAndStatus(1L, "123", MonitoringScheduleStatus.APPROVED))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> monitoringScheduleAdapter.findByIdAndMonitorRegistration(1L, "123"));

        assertEquals(ErrorCode.MONITORING_SCHEDULE_NOT_FOUND.getErrorCode(), exception.getErrorCode().getErrorCode());
        assertEquals(ErrorCode.MONITORING_SCHEDULE_NOT_FOUND.getMessage(), exception.getErrorCode().getMessage());

    }

    @Test
    void shouldThrowWhenFindByIdNotFound() {
        when(monitoringScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> monitoringScheduleAdapter.findById(1L));

        assertEquals(ErrorCode.MONITORING_SCHEDULE_NOT_FOUND.getErrorCode(), exception.getErrorCode().getErrorCode());
        assertEquals(ErrorCode.MONITORING_SCHEDULE_NOT_FOUND.getMessage(), exception.getErrorCode().getMessage());

    }

    @Test
    void shouldThrowWhenSaveMonitoringNotFound() {
        MonitoringScheduleDomain domain = MonitoringScheduleDomain.builder()
                .id(1L)
                .monitor("123")
                .monitoring("Matemática")
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status(MonitoringScheduleStatus.APPROVED)
                .build();

        when(monitoringRepository.findByName("Matemática")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> monitoringScheduleAdapter.save(domain));

        assertEquals(ErrorCode.MONITORING_NOT_FOUND.getErrorCode(), exception.getErrorCode().getErrorCode());
        assertEquals(ErrorCode.MONITORING_NOT_FOUND.getMessage(), exception.getErrorCode().getMessage());
    }

    private MonitoringScheduleEntity buildMonitoringScheduleEntity(){
        return  MonitoringScheduleEntity.builder()
                .id(1L)
                .monitor(StudentEntity.builder()
                        .registration("123")
                        .name("Monitor Teste")
                        .build())
                .monitoring(MonitoringEntity.builder()
                        .name("Matemática")
                        .build())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .status(MonitoringScheduleStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
