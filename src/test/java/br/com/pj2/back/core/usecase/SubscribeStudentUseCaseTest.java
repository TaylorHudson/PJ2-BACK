package br.com.pj2.back.core.usecase;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.domain.StudentDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.BadRequestException;
import br.com.pj2.back.core.gateway.AuthGateway;
import br.com.pj2.back.core.gateway.MonitoringGateway;
import br.com.pj2.back.core.gateway.StudentGateway;
import br.com.pj2.back.core.gateway.TokenGateway;
import br.com.pj2.back.entrypoint.api.dto.request.SubscribeStudentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscribeStudentUseCaseTest {
    @Mock
    private MonitoringGateway monitoringGateway;
    @Mock
    private StudentGateway studentGateway;
    @Mock
    private TokenGateway tokenGateway;
    @Mock
    private AuthGateway authGateway;
    @InjectMocks
    private SubscribeStudentUseCase subscribeStudentUseCase;

    @Test
    void shouldThrowExceptionWhenTeacherIsNotTutor() {
        var request = mock(SubscribeStudentRequest.class);
        var monitoring = mock(MonitoringDomain.class);
        var studentRegistration = "student123";
        var teacherRegistration = "teacher456";
        var otherTeacher = "otherTeacher";
        var monitoringName = "Math";

        when(request.getStudentRegistration()).thenReturn(studentRegistration);
        when(request.getMonitoringName()).thenReturn(monitoringName);
        when(tokenGateway.extractSubjectFromAuthorization("Bearer token")).thenReturn(teacherRegistration);
        when(monitoringGateway.findByName(monitoringName)).thenReturn(monitoring);
        when(monitoring.getTeacher()).thenReturn(otherTeacher);

        var exception = assertThrows(BadRequestException.class, () -> subscribeStudentUseCase.execute(request, "Bearer token"));
        assertEquals(ErrorCode.TEACHER_IS_NOT_THE_TUTORING_TEACHER, exception.getErrorCode());
        verify(studentGateway, never()).save(any());
        verify(monitoring, never()).subscribeStudent(any());
        verify(monitoringGateway, never()).create(any());
    }

    @Test
    void shouldThrowExceptionWhenStudentAlreadySubscribed() {
        var request = mock(SubscribeStudentRequest.class);
        var monitoring = mock(MonitoringDomain.class);
        var studentRegistration = "student123";
        var teacherRegistration = "teacher456";
        var monitoringName = "Math";

        when(request.getStudentRegistration()).thenReturn(studentRegistration);
        when(request.getMonitoringName()).thenReturn(monitoringName);
        when(tokenGateway.extractSubjectFromAuthorization("Bearer token")).thenReturn(teacherRegistration);
        when(monitoringGateway.findByName(monitoringName)).thenReturn(monitoring);
        when(monitoring.getTeacher()).thenReturn(teacherRegistration);
        when(monitoring.containsStudent(studentRegistration)).thenReturn(true);

        var exception = assertThrows(BadRequestException.class, () -> subscribeStudentUseCase.execute(request, "Bearer token"));
        assertEquals(ErrorCode.STUDENT_ALREADY_SUBSCRIBED, exception.getErrorCode());
        verify(studentGateway, never()).save(any());
        verify(monitoring, never()).subscribeStudent(any());
        verify(monitoringGateway, never()).create(any());
    }
}
