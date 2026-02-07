package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;

    public CustomOAuth2UserService(
            PatientRepository patientRepository,
            PharmacyRepository pharmacyRepository
    ) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(request);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not provided by Google");
        }

        String registrationType =
                (String) request.getAdditionalParameters().get("role");

        if (registrationType == null) {
            throw new OAuth2AuthenticationException("Missing role parameter");
        }

        Role role = Role.valueOf(registrationType.toUpperCase());

        if (role == Role.PATIENT) {
            patientRepository.findByEmail(email)
                    .orElseGet(() -> {
                        PatientProfile p = new PatientProfile();
                        p.setEmail(email);
                        p.setFullName(name);
                        p.setPassword(UUID.randomUUID().toString());
                        p.setRole(Role.PATIENT);
                        return patientRepository.save(p);
                    });
        }

        if (role == Role.PHARMACIST) {
            pharmacyRepository.findByEmail(email)
                    .orElseGet(() -> {
                        PharmacyProfile ph = new PharmacyProfile();
                        ph.setEmail(email);
                        ph.setFullName(name);
                        ph.setPassword(UUID.randomUUID().toString());
                        ph.setRole(Role.PHARMACIST);
                        return pharmacyRepository.save(ph);
                    });
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())),
                attributes,
                "email"
        );
    }
}
