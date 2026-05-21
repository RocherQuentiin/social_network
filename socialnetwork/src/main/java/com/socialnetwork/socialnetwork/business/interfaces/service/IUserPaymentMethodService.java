package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.socialnetwork.socialnetwork.dto.UserPaymentMethodRequestDto;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodResponseDto;
import com.socialnetwork.socialnetwork.entity.UserPaymentMethod;

public interface IUserPaymentMethodService {

    List<UserPaymentMethodResponseDto> listForUser(UUID userId);

    Optional<UserPaymentMethodResponseDto> create(UUID userId, UserPaymentMethodRequestDto dto);

    boolean delete(UUID userId, UUID methodId);

    Optional<UserPaymentMethod> findOwned(UUID userId, UUID methodId);
}
