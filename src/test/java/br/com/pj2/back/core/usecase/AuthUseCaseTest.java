//package br.com.pj2.back.core.usecase;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import br.com.pj2.back.core.domain.AuthDomain;
//import br.com.pj2.back.core.domain.StudentDomain;
//import br.com.pj2.back.core.domain.TeacherDomain;
//import br.com.pj2.back.core.domain.enumerated.Role;
//import br.com.pj2.back.core.gateway.AuthGateway;
//import br.com.pj2.back.core.gateway.StudentGateway;
//import br.com.pj2.back.core.gateway.TeacherGateway;
//import br.com.pj2.back.core.gateway.TokenGateway;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AuthUseCaseTest {
//    @Mock
//    private AuthGateway authGateway;
//    @Mock
//    private TeacherGateway teacherGateway;
//    @Mock
//    private StudentGateway studentGateway;
//    @Mock
//    private TokenGateway tokenGateway;
//    @InjectMocks
//    private AuthUseCase authUseCase;
//
//    @Test
//    void shouldAuthenticateTeacherAndSaveUser() {
//        String registration = "1234567";
//        String password = "senha";
//        String expectedAccess = "access-token";
//        String expectedRefresh = "refresh-token";
//
//        when(tokenGateway.generateAccessToken(registration)).thenReturn(expectedAccess);
//        when(tokenGateway.generateRefreshToken(registration)).thenReturn(expectedRefresh);
//
//        AuthDomain auth = authUseCase.execute(registration, password);
//
//        verify(authGateway).validateCredentials(registration, password);
//        verify(teacherGateway).save(any(TeacherDomain.class));
//        verifyNoInteractions(studentGateway);
//        assertEquals(expectedAccess, auth.getAccessToken());
//        assertEquals(expectedRefresh, auth.getRefreshToken());
//    }
//
//    @Test
//    void shouldAuthenticateExistingStudent() {
//        String registration = "000000000000";
//        String name = "Student Name";
//        String password = "senha";
//        String expectedAccess = "access-token";
//        String expectedRefresh = "refresh-token";
//        var studentDomain = StudentDomain.builder().registration(registration).name(name).role(Role.STUDENT).build();
//
//        when(studentGateway.findByRegistrationAndRole(registration, Role.STUDENT)).thenReturn(studentDomain);
//        when(tokenGateway.generateAccessToken(registration)).thenReturn(expectedAccess);
//        when(tokenGateway.generateRefreshToken(registration)).thenReturn(expectedRefresh);
//
//        AuthDomain auth = authUseCase.execute(registration, password);
//
//        verify(authGateway).validateCredentials(registration, password);
//        verify(studentGateway).findByRegistrationAndRole(registration, Role.STUDENT);
//        assertEquals(expectedAccess, auth.getAccessToken());
//        assertEquals(expectedRefresh, auth.getRefreshToken());
//    }
//}
