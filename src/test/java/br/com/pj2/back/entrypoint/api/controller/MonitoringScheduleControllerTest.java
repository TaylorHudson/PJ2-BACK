package br.com.pj2.back.entrypoint.api.controller;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.domain.MonitoringScheduleDomain;
import br.com.pj2.back.core.gateway.MonitoringGateway;
import br.com.pj2.back.core.gateway.MonitoringScheduleGateway;
import br.com.pj2.back.core.gateway.TokenGateway;
import br.com.pj2.back.core.usecase.ApproveMonitoringScheduleUseCase;
import br.com.pj2.back.core.usecase.CheckScheduleConflictsUseCase;
import br.com.pj2.back.core.usecase.DenyMonitoringScheduleUseCase;
import br.com.pj2.back.core.usecase.FindSchedulesByFilterUseCase;
import br.com.pj2.back.entrypoint.api.dto.request.MonitoringScheduleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonitoringScheduleControllerTest {
    private static final String AUTH_HEADER = "Bearer token";

    @Mock
    private FindSchedulesByFilterUseCase findSchedulesByFilterUseCase;
    @Mock
    private ApproveMonitoringScheduleUseCase approveMonitoringScheduleUseCase;
    @Mock
    private DenyMonitoringScheduleUseCase denyMonitoringScheduleUseCase;
    @Mock
    private TokenGateway tokenGateway;
    @Mock
    private MonitoringScheduleGateway scheduleGateway;
    @Mock
    private MonitoringGateway monitoringGateway;
    @Mock
    private CheckScheduleConflictsUseCase checkScheduleConflictsUseCase;
    @InjectMocks
    private MonitoringScheduleController monitoringScheduleController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(monitoringScheduleController).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldApproveScheduleSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/monitoring/schedules/teachers/{id}/approve", 1L)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyScheduleSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/monitoring/schedules/teachers/{id}/deny", 2L)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnSchedulesByFilter() throws Exception {
        var scheduleDomain = Instancio.of(MonitoringScheduleDomain.class)
                .set(field(MonitoringScheduleDomain::getId), 3L)
                .create();
        when(findSchedulesByFilterUseCase.execute(anyString(), anyString()))
                .thenReturn(List.of(scheduleDomain));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/monitoring/schedules/teachers/filter")
                        .param("status", "APPROVED")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void shouldRequestScheduleSuccessfully() throws Exception {
        var scheduleRequest = Instancio.of(MonitoringScheduleRequest.class)
                .set(field(MonitoringScheduleRequest::getDayOfWeek), "TUESDAY")
                .create();
        var scheduleDomain = Instancio.of(MonitoringScheduleDomain.class)
                .set(field(MonitoringScheduleDomain::getId), 4L)
                .create();
        when(tokenGateway.extractSubjectFromAuthorization(anyString())).thenReturn("202300123456");
        when(scheduleGateway.save(any(MonitoringScheduleDomain.class))).thenReturn(scheduleDomain);

        mockMvc.perform(post("/monitoring/schedules/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void shouldReturnBadRequestWhenStudentScheduleRequestIsInvalid() throws Exception {
        var invalidRequest = new MonitoringScheduleRequest();

        mockMvc.perform(post("/monitoring/schedules/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissingOnRequestSchedule() throws Exception {
        var scheduleRequest = Instancio.create(MonitoringScheduleRequest.class);

        mockMvc.perform(post("/monitoring/schedules/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnStudentSchedules() throws Exception {
        var scheduleDomain = Instancio.of(MonitoringScheduleDomain.class)
                .set(field(MonitoringScheduleDomain::getId), 5L)
                .set(field(MonitoringScheduleDomain::getMonitoring), "EDA")
                .create();

        var monitoring = MonitoringDomain.builder()
                .topics(List.of("Árvores", "Grafos"))
                .build();

        when(tokenGateway.extractSubjectFromAuthorization(anyString()))
                .thenReturn("202300123456");

        when(scheduleGateway.findByMonitorRegistration("202300123456"))
                .thenReturn(List.of(scheduleDomain));

        when(monitoringGateway.findByName("EDA"))
                .thenReturn(monitoring);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/monitoring/schedules/students/me")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void shouldReturnScheduleByIdForStudent() throws Exception {
        var scheduleDomain = Instancio.of(MonitoringScheduleDomain.class)
                .set(field(MonitoringScheduleDomain::getId), 7L)
                .create();
        when(tokenGateway.extractSubjectFromAuthorization(anyString())).thenReturn("202300123456");
        when(scheduleGateway.findByIdAndMonitorRegistration(anyLong(), anyString())).thenReturn(scheduleDomain);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/monitoring/schedules/students/{id}", 7L)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }
}
