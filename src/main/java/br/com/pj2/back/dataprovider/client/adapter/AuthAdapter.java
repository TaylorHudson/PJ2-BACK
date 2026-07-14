package br.com.pj2.back.dataprovider.client.adapter;

import br.com.pj2.back.core.common.utils.AesUtils;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.BadRequestException;
import br.com.pj2.back.core.gateway.AuthGateway;
import br.com.pj2.back.dataprovider.client.SuapClient;
import br.com.pj2.back.dataprovider.client.dto.AuthRequest;
import br.com.pj2.back.dataprovider.client.dto.AuthResponse;
import br.com.pj2.back.dataprovider.client.dto.FindStudentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthAdapter implements AuthGateway {
    private final SuapClient suapClient;
    private final String credential;
    private final String key;

    public AuthAdapter(SuapClient suapClient, @Value("${integration.suap.credential}") String credential, @Value("${integration.suap.key}") String key) {
        this.suapClient = suapClient;
        this.credential = credential;
        this.key = key;
    }

    @Override
    public void validateCredentials(String registration, String password) {
        suapClient.obtainToken(new AuthRequest(registration, password));
    }

    @Override
    public FindStudentResponse.UserResponse checkIfStudentExists(String registration) {
        String decryptedData = AesUtils.decrypt(credential, key);
        String[] parts = decryptedData.split(":");
        AuthResponse authResponse = suapClient.obtainToken(new AuthRequest(parts[0], parts[1]));
        FindStudentResponse studentResponse = suapClient.findStudent("Bearer " + authResponse.getAccessToken(), registration);

        if (studentResponse.getCount() == 0 || studentResponse.getResults().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_STUDENT_REGISTRATION);
        }

        var result = studentResponse.getResults().get(0);
        if (!result.getCourse().getName().contains("Monteiro (CAMPUS MONTEIRO)")) {
            throw new BadRequestException(ErrorCode.INVALID_STUDENT_REGISTRATION);
        }

        if (!result.getSituation().equals("Matriculado") && !result.getSituation().equals("Vinculado")) {
            throw new BadRequestException(ErrorCode.INVALID_STUDENT_REGISTRATION);
        }

        return studentResponse.getResults()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_STUDENT_REGISTRATION));
    }

}
