package org.c4marathon.assignment.domain.service.consumer;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.auth.dto.request.SignUpRequest;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductEntry;
import org.c4marathon.assignment.domain.consumer.dto.request.PurchaseProductRequest;
import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.service.ConsumerReadService;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.domain.delivery.entity.Delivery;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyReadService;
import org.c4marathon.assignment.domain.order.entity.Order;
import org.c4marathon.assignment.domain.orderproduct.entity.OrderProduct;
import org.c4marathon.assignment.domain.product.entity.Product;
import org.c4marathon.assignment.domain.product.service.ProductReadService;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.service.ServiceTestSupport;
import org.c4marathon.assignment.global.constant.DeliveryStatus;
import org.c4marathon.assignment.global.constant.OrderStatus;
import org.c4marathon.assignment.global.error.BaseException;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

public class ConsumerServiceTest extends ServiceTestSupport {

	@MockBean
	private ConsumerReadService consumerReadService;
	@MockBean
	private DeliveryCompanyReadService deliveryCompanyReadService;
	@Autowired
	private ConsumerService consumerService;
	@MockBean
	private ProductReadService productReadService;

	private Consumer consumer;
	private DeliveryCompany deliveryCompany;
	private Product product;

	@BeforeEach
	void setUp() {
		consumer = consumerRepository.save(Consumer.builder()
			.email("email")
			.address("address")
			.build());
		consumer.addBalance(1000000000L);
		deliveryCompany = deliveryCompanyRepository.save(DeliveryCompany.builder()
			.email("email")
			.build());
		Seller seller = sellerRepository.save(Seller.builder()
			.email("email")
			.build());
		product = productRepository.save(Product.builder()
			.stock(100)
			.description("description")
			.amount(1000L)
			.name("product")
			.seller(seller)
			.build());
	}

	@DisplayName("회원가입 시")
	@Nested
	class Signup {

		@DisplayName("address가 null이 아니고, 가입된 email이 존재하지 않는다면 성공한다.")
		@Test
		void success_when_addressIsNotNullAndEmailNotExists() {
			SignUpRequest request = createRequest("address");

			given(consumerReadService.existsByEmail(anyString()))
				.willReturn(false);

			consumerService.signup(request);
			Optional<Consumer> consumer = consumerRepository.findByEmail(request.email());

			assertThat(consumer).isPresent();
			assertThat(consumer.get().getBalance()).isEqualTo(0);
			assertThat(consumer.get().getEmail()).isEqualTo(request.email());
			assertThat(consumer.get().getAddress()).isEqualTo(request.address());
		}

		@DisplayName("address가 null이면 예외를 반환한다.")
		@Test
		void throwException_when_addressIsNull() {
			SignUpRequest request = createRequest(null);

			ErrorCode errorCode = CONSUMER_NEED_ADDRESS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());

			assertThatThrownBy(() -> consumerService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@DisplayName("이미 회원가입된 이메일이 존재하면 예외를 반환한다.")
		@Test
		void throwException_when_alreadyExistsEmail() {
			SignUpRequest request = createRequest("address");
			consumerRepository.save(Consumer.builder()
				.email(request.email())
				.address("address")
				.build());

			given(consumerReadService.existsByEmail(anyString()))
				.willReturn(true);

			ErrorCode errorCode = ALREADY_CONSUMER_EXISTS;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.signup(request))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private SignUpRequest createRequest(String address) {
			return new SignUpRequest("email123", address);
		}
	}

	@DisplayName("상품 구매 시")
	@Nested
	class PurchaseProduct {

		@DisplayName("각 productId와 quantity를 요청하면 Order가 생성된다.")
		@Test
		void createOrder_when_validRequest() {
			PurchaseProductRequest request = createRequest();
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(productReadService.findById(anyLong()))
				.willReturn(product);

			consumerService.purchaseProduct(request, consumer);
			List<Order> orders = orderRepository.findAll();
			List<Delivery> deliveries = deliveryRepository.findAll();

			assertThat(orders).hasSize(1);
			assertThat(orders.get(0).getConsumer().getId()).isEqualTo(consumer.getId());
			assertThat(orders.get(0).getDelivery().getId()).isEqualTo(deliveries.get(0).getId());
			assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.COMPLETE_PAYMENT);
		}

		@DisplayName("각 productId와 quantity를 요청하면 Delivery가 생성된다.")
		@Test
		void createDelivery_when_validRequest() {
			PurchaseProductRequest request = createRequest();
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(productReadService.findById(anyLong()))
				.willReturn(product);

			consumerService.purchaseProduct(request, consumer);
			List<Delivery> deliveries = deliveryRepository.findAll();

			assertThat(deliveries).hasSize(1);
			assertThat(deliveries.get(0).getDeliveryCompany().getId()).isEqualTo(deliveryCompany.getId());
			assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.BEFORE_DELIVERY);
		}

		@DisplayName("각 productId와 quantity를 요청하면 OrderProduct가 생성된다.")
		@Test
		void createOrderProduct_when_validRequest() {
			PurchaseProductRequest request = createRequest();
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(productReadService.findById(anyLong()))
				.willReturn(product);

			consumerService.purchaseProduct(request, consumer);
			List<Order> orders = orderRepository.findAll();
			List<OrderProduct> orderProducts = orderProductRepository.findAll();

			assertThat(orderProducts).hasSize(1);
			assertThat(orderProducts.get(0).getProduct().getId()).isEqualTo(product.getId());
			assertThat(orderProducts.get(0).getOrder().getId()).isEqualTo(orders.get(0).getId());
			assertThat(orderProducts.get(0).getQuantity()).isEqualTo(
				request.purchaseProducts().get(0).quantity());
		}

		@DisplayName("각 productId와 quantity를 요청하면 Product의 quantity가 감소된다.")
		@Test
		void decreaseProductQuantity_when_validRequest() {
			Integer beforeStock = product.getStock();
			PurchaseProductRequest request = createRequest();
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(productReadService.findById(anyLong()))
				.willReturn(product);

			consumerService.purchaseProduct(request, consumer);

			assertThat(product.getStock()).isEqualTo(beforeStock - request.purchaseProducts().get(0).quantity());
		}

		@DisplayName("구매자의 캐시가 부족하다면 실패한다.")
		@Test
		void fail_when_balanceIsLessThanTotalAmount() {
			consumer.decreaseBalance(consumer.getBalance());

			PurchaseProductRequest request = createRequest();
			given(deliveryCompanyReadService.findMinimumCountOfDelivery())
				.willReturn(deliveryCompany);
			given(productReadService.findById(anyLong()))
				.willReturn(product);

			ErrorCode errorCode = NOT_ENOUGH_BALANCE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.purchaseProduct(request, consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		private PurchaseProductRequest createRequest() {
			return new PurchaseProductRequest(List.of(new PurchaseProductEntry(product.getId(), 10)));
		}
	}

	@DisplayName("주문 환불 시")
	@Nested
	class RefundOrder {

		private Order order;
		private Delivery delivery;

		@BeforeEach
		void setUp() {
			delivery = deliveryRepository.save(Delivery.builder()
				.deliveryCompany(deliveryCompany)
				.invoiceNumber("invliceNumber")
				.address("address")
				.build());
			order = orderRepository.save(Order.builder()
				.consumer(consumer)
				.delivery(delivery)
				.orderStatus(OrderStatus.COMPLETE_PAYMENT)
				.build());

		}

		@Transactional
		@DisplayName("배송 상태가 BEFORE_DELIVERY가 아니면 실패한다.")
		@Test
		void fail_when_deliveryStatusIsNotBEFORE_DELIVRY() {
			delivery.updateDeliveryStatus(DeliveryStatus.IN_DELIVERY);

			ErrorCode errorCode = REFUND_NOT_AVAILABLE;
			BaseException exception = new BaseException(errorCode.name(), errorCode.getMessage());
			assertThatThrownBy(() -> consumerService.refundOrder(order.getId(), consumer))
				.isInstanceOf(exception.getClass())
				.hasMessage(exception.getMessage());
		}

		@Transactional
		@DisplayName("orderStatus, deliveryStatus와 Consumer의 balance가 수정된다.")
		@Test
		void updateOrderStatusAndDeliveryStatusAndBalance_when_refundOrder() {
			Long beforeBalance = consumer.getBalance();
			orderProductRepository.save(OrderProduct.builder()
				.product(product)
				.order(order)
				.amount(product.getAmount())
				.quantity(1)
				.build());
			consumerService.refundOrder(order.getId(), consumer);

			assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUND);
			assertThat(order.getConsumer().getBalance()).isEqualTo(beforeBalance + product.getAmount());
		}
	}
}
