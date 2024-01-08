package org.c4marathon.assignment.domain.service.delivery;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.delivery.service.DeliveryReadService;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DeliveryReadServiceTest extends ServiceTestSupport {

	@Autowired
	private DeliveryReadService deliveryReadService;

	@DisplayName("id로 조회 시")
	@Nested
	class FindByIdJoinFetch {

		private Delivery delivery;

		@BeforeEach
		void setUp() {
			DeliveryCompany deliveryCompany = deliveryCompanyRepository.save(DeliveryCompany.builder()
				.email("email")
				.build());
			delivery = deliveryRepository.save(Delivery.builder()
				.address("address")
				.deliveryCompany(deliveryCompany)
				.invoiceNumber("invoiceNumber")
				.build());
		}

		@DisplayName("id에 해당하는 Delivery가 존재하면 반환한다.")
		@Test
		void returnDelivery_when_exists() {
			Delivery find = deliveryReadService.findByIdJoinFetch(delivery.getId());

			assertThat(find.getId()).isEqualTo(delivery.getId());
			assertThat(find.getDeliveryCompany()).isNotNull();
		}

		@DisplayName("id에 해당하는 Delivery가 존재하면 예외를 반환한다.")
		@Test
		void throwException_when_notExists() {

			ErrorCode errorCode = DELIVERY_NOT_FOUND;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> deliveryReadService.findByIdJoinFetch(delivery.getId() + 1))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}
	}
}
