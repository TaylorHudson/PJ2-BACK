package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.domain.MonitoringDomainDetail;
import br.com.pj2.back.core.domain.StudentDomain;
import br.com.pj2.back.core.domain.TeacherDomain;
import br.com.pj2.back.core.domain.enumerated.MonitoringScheduleStatus;
import br.com.pj2.back.core.exception.ForbiddenException;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.dataprovider.database.entity.MonitoringEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringScheduleEntity;
import br.com.pj2.back.dataprovider.database.entity.StudentEntity;
import br.com.pj2.back.dataprovider.database.entity.TeacherEntity;
import br.com.pj2.back.dataprovider.database.repository.MonitoringRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.*;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringAdapterTest {
    @Mock
    private MonitoringRepository monitoringRepository;
    @Mock
    private TeacherAdapter teacherAdapter;
    @Mock
    private StudentAdapter studentAdapter;
    @Mock
    private MonitoringSessionAdapter monitoringSessionAdapter;
    @InjectMocks
    private MonitoringAdapter monitoringAdapter;

    @Test
    void shouldFindByNameSuccessfully() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 1L)
                .set(field(MonitoringEntity::getName), "Math")
                .set(field(MonitoringEntity::getSchedules), List.of(Instancio.create(MonitoringScheduleEntity.class)))
                .create();
        when(monitoringRepository.findByName(anyString())).thenReturn(Optional.of(entity));
        MonitoringDomain result = monitoringAdapter.findByName("Math");
        assertEquals(1, result.getId());
        assertEquals("Math", result.getName());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenMonitoringNotFoundByName() {
        when(monitoringRepository.findByName(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> monitoringAdapter.findByName("Physics"));
    }

    @Test
    void shouldCreateMonitoringSuccessfully() {
        MonitoringDomain domain = Instancio.of(MonitoringDomain.class)
                .set(field(MonitoringDomain::getId), 2L)
                .set(field(MonitoringDomain::getName), "Biology")
                .set(field(MonitoringDomain::getTopics), List.of("Genetics"))
                .create();
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 2L)
                .set(field(MonitoringEntity::getName), "Biology")
                .set(field(MonitoringEntity::getTopics), "Genetics")
                .set(field(MonitoringEntity::getSchedules), List.of(Instancio.create(MonitoringScheduleEntity.class)))
                .create();
        when(teacherAdapter.findByRegistration(anyString())).thenReturn(Instancio.create(TeacherDomain.class));
        when(studentAdapter.findByRegistration(anyString())).thenReturn(Instancio.create(StudentDomain.class));
        when(monitoringRepository.save(any(MonitoringEntity.class))).thenReturn(entity);
        MonitoringDomain result = monitoringAdapter.create(domain);
        assertEquals("Biology", result.getName());
        assertEquals(List.of("Genetics"), result.getTopics());
    }

    @Test
    void shouldReturnAllMonitoringsByStudentRegistration() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 3L)
                .set(field(MonitoringEntity::getName), "Chemistry")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T3").build())
                .set(field(MonitoringEntity::getTopics), "Organic")
                .set(field(MonitoringEntity::getSchedules), List.of(Instancio.create(MonitoringScheduleEntity.class)))
                .create();
        when(monitoringRepository.findAllByStudentRegistration(anyString())).thenReturn(List.of(entity));
        List<MonitoringDomain> result = monitoringAdapter.findAllByStudentRegistration("S1");
        assertEquals(1, result.size());
        assertEquals("Chemistry", result.get(0).getName());
    }

    @Test
    void shouldReturnAllMonitoringsByTeacherRegistrationAndCountTopics() {
        final var topicsInSession = new HashMap<String, Integer>();
        topicsInSession.put("Ancient", 5);
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 4L)
                .set(field(MonitoringEntity::getName), "History")
                .set(field(MonitoringEntity::getTopics), "Ancient")
                .set(field(MonitoringEntity::getSchedules), List.of(Instancio.create(MonitoringScheduleEntity.class)))
                .create();
        when(monitoringRepository.findAllByTeacherRegistration(anyString())).thenReturn(List.of(entity));
        when(monitoringSessionAdapter.countTopicsInSessionMonitoring(any())).thenReturn(topicsInSession);
        List<MonitoringDomain> result = monitoringAdapter.findAllByTeacherRegistration("T4");
        assertEquals(1, result.size());
        assertEquals("History", result.get(0).getName());
        assertEquals(5, result.get(0).getCountTopicsInSession().get("Ancient"));
    }

    @Test
    void shouldReturnEmptyListWhenNoMonitoringsForTeacher() {
        when(monitoringRepository.findAllByTeacherRegistration("T5")).thenReturn(Collections.emptyList());
        List<MonitoringDomain> result = monitoringAdapter.findAllByTeacherRegistration("T5");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteMonitoringSuccessfullyWhenTeacherMatches() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 6L)
                .set(field(MonitoringEntity::getName), "Geo")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T6").build())
                .create();
        when(monitoringRepository.findById(anyLong())).thenReturn(Optional.of(entity));
        doNothing().when(monitoringRepository).deleteById(anyLong());
        assertDoesNotThrow(() -> monitoringAdapter.deleteById(6L, "T6"));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenTeacherDoesNotMatchOnDelete() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 7L)
                .set(field(MonitoringEntity::getName), "Geo")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T7").build())
                .create();
        when(monitoringRepository.findById(7L)).thenReturn(Optional.of(entity));
        assertThrows(ForbiddenException.class, () -> monitoringAdapter.deleteById(7L, "T8"));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenMonitoringNotFoundOnDelete() {
        when(monitoringRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> monitoringAdapter.deleteById(8L, "T8"));
    }

    @Test
    void shouldFindByIdSuccessfullyWhenTeacherMatches() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 9L)
                .set(field(MonitoringEntity::getName), "Port")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T9").build())
                .set(field(MonitoringEntity::getTopics), "Grammar")
                .set(field(MonitoringEntity::getSchedules), List.of(Instancio.create(MonitoringScheduleEntity.class)))
                .create();
        when(monitoringRepository.findById(anyLong())).thenReturn(Optional.of(entity));
        MonitoringDomain result = monitoringAdapter.findById(9L, "T9");
        assertEquals("Port", result.getName());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenTeacherDoesNotMatchOnFindById() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 10L)
                .set(field(MonitoringEntity::getName), "Port")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T10").build())
                .create();
        when(monitoringRepository.findById(10L)).thenReturn(Optional.of(entity));
        assertThrows(ForbiddenException.class, () -> monitoringAdapter.findById(10L, "T11"));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenMonitoringNotFoundOnFindById() {
        when(monitoringRepository.findById(11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> monitoringAdapter.findById(11L, "T11"));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenTeacherDoesNotMatchOnUpdate() {
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 13L)
                .set(field(MonitoringEntity::getName), "OldName")
                .set(field(MonitoringEntity::getTeacher), TeacherEntity.builder().registration("T13").build())
                .create();
        MonitoringDomain domain = Instancio.of(MonitoringDomain.class)
                .set(field(MonitoringDomain::getId), 13L)
                .set(field(MonitoringDomain::getName), "NewName")
                .set(field(MonitoringDomain::getTeacher), "T13")
                .create();
        when(monitoringRepository.findById(13L)).thenReturn(Optional.of(entity));
        assertThrows(ForbiddenException.class, () -> monitoringAdapter.update(13L, domain, "T14"));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenMonitoringNotFoundOnUpdate() {
        MonitoringDomain domain = Instancio.of(MonitoringDomain.class)
                .set(field(MonitoringDomain::getId), 14L)
                .set(field(MonitoringDomain::getName), "NewName")
                .set(field(MonitoringDomain::getTeacher), "T14")
                .create();
        when(monitoringRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> monitoringAdapter.update(14L, domain, "T14"));
    }

    @Test
    void shouldConvertListToStringAndStringToListCorrectly() {
        List<String> topics = Arrays.asList("A", "B", "C");
        String joined = MonitoringAdapter.convertListToString(topics);
        assertEquals("A,B,C", joined);
        List<String> split = MonitoringAdapter.convertStringToList("A,B,C");
        assertEquals(topics, split);
        assertNull(MonitoringAdapter.convertListToString(Collections.emptyList()));
        assertEquals(Collections.emptyList(), MonitoringAdapter.convertStringToList(""));
    }

    @Test
    void shouldFindByIdDetailsSuccessfullyWhenTeacherMatches() {
        StudentEntity studentEntity = Instancio.of(StudentEntity.class)
                .set(field(StudentEntity::getRegistration), "S100")
                .create();
        TeacherEntity teacherEntity = TeacherEntity.builder()
                .registration("T100")
                .name("Teacher Name")
                .build();
        MonitoringScheduleEntity schedule = Instancio.of(MonitoringScheduleEntity.class)
                .set(field(MonitoringScheduleEntity::getMonitor), studentEntity)
                .set(field(MonitoringScheduleEntity::getStatus), MonitoringScheduleStatus.APPROVED)
                .set(field(MonitoringScheduleEntity::getDayOfWeek), DayOfWeek.MONDAY)
                .create();
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 100L)
                .set(field(MonitoringEntity::getName), "Monitoring Test")
                .set(field(MonitoringEntity::getTeacher), teacherEntity)
                .set(field(MonitoringEntity::getStudents), List.of(studentEntity))
                .set(field(MonitoringEntity::getSchedules), List.of(schedule))
                .create();

        when(monitoringRepository.findById(100L)).thenReturn(Optional.of(entity));
        MonitoringDomainDetail result = monitoringAdapter.findByIdDetails(100L, "T100");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Monitoring Test", result.getName());
        assertEquals("Teacher Name", result.getTeacher());

        assertEquals(1, result.getStudents().size());
        assertEquals("S100", result.getStudents().get(0).getRegistration());
        assertTrue(result.getStudents().get(0).getDaysOfWeek().contains("MONDAY"));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenTeacherDoesNotMatchInFindByIdDetails() {
        TeacherEntity teacherEntity = TeacherEntity.builder()
                .registration("T200")
                .name("Teacher Name")
                .build();
        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 200L)
                .set(field(MonitoringEntity::getTeacher), teacherEntity)
                .create();

        when(monitoringRepository.findById(200L)).thenReturn(Optional.of(entity));

        assertThrows(
                ForbiddenException.class,
                () -> monitoringAdapter.findByIdDetails(200L, "T201")
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenMonitoringNotFoundInFindByIdDetails() {
        when(monitoringRepository.findById(300L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> monitoringAdapter.findByIdDetails(300L, "T300")
        );
    }

    @Test
    void shouldNotAddDayOfWeekWhenScheduleNotApproved() {
        StudentEntity studentEntity = Instancio.of(StudentEntity.class)
                .set(field(StudentEntity::getRegistration), "S400")
                .create();

        TeacherEntity teacherEntity = TeacherEntity.builder()
                .registration("T400")
                .name("Teacher Name")
                .build();

        MonitoringScheduleEntity schedule = Instancio.of(MonitoringScheduleEntity.class)
                .set(field(MonitoringScheduleEntity::getMonitor), studentEntity)
                .set(field(MonitoringScheduleEntity::getStatus), MonitoringScheduleStatus.PENDING)
                .set(field(MonitoringScheduleEntity::getDayOfWeek), DayOfWeek.FRIDAY)
                .create();

        MonitoringEntity entity = Instancio.of(MonitoringEntity.class)
                .set(field(MonitoringEntity::getId), 400L)
                .set(field(MonitoringEntity::getTeacher), teacherEntity)
                .set(field(MonitoringEntity::getStudents), List.of(studentEntity))
                .set(field(MonitoringEntity::getSchedules), List.of(schedule))
                .create();

        when(monitoringRepository.findById(400L)).thenReturn(Optional.of(entity));

        MonitoringDomainDetail result = monitoringAdapter.findByIdDetails(400L, "T400");

        assertNotNull(result);
        assertEquals(1, result.getStudents().size());
        assertTrue(result.getStudents().get(0).getDaysOfWeek().isEmpty());
    }

}
