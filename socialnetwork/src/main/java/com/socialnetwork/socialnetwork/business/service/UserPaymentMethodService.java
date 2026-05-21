package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserPaymentMethodRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserPaymentMethodService;
import com.socialnetwork.socialnetwork.business.utils.PaymentTestCardUtils;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodRequestDto;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodResponseDto;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.entity.UserPaymentMethod;

@Service
public class UserPaymentMethodService implements IUserPaymentMethodService {

    private final IUserPaymentMethodRepository userPaymentMethodRepository;
    private final IUserRepository userRepository;

    public UserPaymentMethodService(IUserPaymentMethodRepository userPaymentMethodRepository,
                                    IUserRepository userRepository) {
        this.userPaymentMethodRepository = userPaymentMethodRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPaymentMethodResponseDto> listForUser(UUID userId) {
        return userPaymentMethodRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UserPaymentMethodResponseDto> create(UUID userId, UserPaymentMethodRequestDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        String cvv = dto.getCvv() != null && !dto.getCvv().isBlank() ? dto.getCvv().trim() : PaymentTestCardUtils.TEST_CVV;
        if (!PaymentTestCardUtils.validateFullCard(dto.getCardholderName(), dto.getCardNumber(),
                dto.getExpiryMonth(), dto.getExpiryYear(), cvv)) {
            return Optional.empty();
        }
        UserPaymentMethod m = new UserPaymentMethod();
        m.setUser(userOpt.get());
        m.setCardholderName(trim(dto.getCardholderName()));
        m.setCardLast4(PaymentTestCardUtils.lastFourDigits(dto.getCardNumber()));
        m.setExpiryMonth(trim(dto.getExpiryMonth()));
        m.setExpiryYear(trim(dto.getExpiryYear()));
        m.setLabel(trimToNull(dto.getLabel()));
        UserPaymentMethod saved = userPaymentMethodRepository.save(m);
        return Optional.of(toDto(saved));
    }

    @Override
    @Transactional
    public boolean delete(UUID userId, UUID methodId) {
        return userPaymentMethodRepository.deleteOwned(methodId, userId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserPaymentMethod> findOwned(UUID userId, UUID methodId) {
        return userPaymentMethodRepository.findByIdAndUser_Id(methodId, userId);
    }

    private UserPaymentMethodResponseDto toDto(UserPaymentMethod m) {
        String display = (m.getLabel() != null && !m.getLabel().isBlank())
                ? m.getLabel()
                : ("Carte ****" + m.getCardLast4());
        return new UserPaymentMethodResponseDto(
                m.getId(),
                m.getCardholderName(),
                m.getCardLast4(),
                m.getExpiryMonth(),
                m.getExpiryYear(),
                m.getLabel(),
                display);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
