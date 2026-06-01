package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.gateway.MonitoringGateway;
import br.com.pj2.back.core.gateway.TokenGateway;
import br.com.pj2.back.entrypoint.api.dto.request.MonitoringUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMonitoringUseCaseTest {
    @Mock
    private MonitoringGateway monitoringGateway;
    @Mock
    private TokenGateway tokenGateway;
    @InjectMocks
    private UpdateMonitoringUseCase updateMonitoringUseCase;

    @Test
    void shouldUpdateMonitoringWhenTeacherHasPermission() {
        var id = 1L;
        var authHeader = "Bearer token";
        var registration = "teacherRegistration";

        var request = mock(MonitoringUpdateRequest.class);
        var updatedMonitoring = mock(MonitoringDomain.class);

        when(tokenGateway.extractSubjectFromAuthorization(authHeader))
                .thenReturn(registration);

        when(request.getName()).thenReturn("Math");
        when(request.getAllowMonitorsSameTime()).thenReturn(true);
        when(request.getTopics()).thenReturn(List.of("Algebra", "Geometry"));

        when(monitoringGateway.update(
                eq(id),
                any(MonitoringDomain.class),
                eq(registration)
        )).thenReturn(updatedMonitoring);

        var result = updateMonitoringUseCase.execute(id, request, authHeader);

        assertEquals(updatedMonitoring, result);

        verify(monitoringGateway).update(
                eq(id),
                any(MonitoringDomain.class),
                eq(registration)
        );
    }

    @Test
    void shouldPassCorrectValuesToMonitoringGateway() {
        var id = 2L;
        var authHeader = "Bearer token";
        var registration = "teacherRegistration";

        var request = mock(MonitoringUpdateRequest.class);

        when(tokenGateway.extractSubjectFromAuthorization(authHeader))
                .thenReturn(registration);

        when(request.getName()).thenReturn("Physics");
        when(request.getAllowMonitorsSameTime()).thenReturn(false);
        when(request.getTopics()).thenReturn(List.of("Mechanics"));

        updateMonitoringUseCase.execute(id, request, authHeader);

        verify(monitoringGateway).update(
                eq(id),
                argThat(domain ->
                        domain.getName().equals("Physics")
                                && !domain.getAllowMonitorsSameTime()
                                && domain.getTopics().equals(List.of("Mechanics"))
                ),
                eq(registration)
        );
    }

    @Test
    void shouldHandleNullTopicsGracefully() {
        var id = 3L;
        var authHeader = "Bearer token";
        var registration = "teacherRegistration";

        var request = mock(MonitoringUpdateRequest.class);

        when(tokenGateway.extractSubjectFromAuthorization(authHeader))
                .thenReturn(registration);

        when(request.getName()).thenReturn("Chemistry");
        when(request.getAllowMonitorsSameTime()).thenReturn(true);
        when(request.getTopics()).thenReturn(null);

        updateMonitoringUseCase.execute(id, request, authHeader);

        verify(monitoringGateway).update(
                eq(id),
                argThat(domain ->
                        domain.getName().equals("Chemistry")
                                && domain.getAllowMonitorsSameTime()
                                && domain.getTopics() == null
                ),
                eq(registration)
        );
    }
}
