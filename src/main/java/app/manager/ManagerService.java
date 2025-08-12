package app.manager;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.manager.dto.response.OrderDetailResponse;
import app.customer.dto.response.GetCustomerAddressListResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.manager.dto.response.GetCustomerDetailResponse;
import app.manager.dto.response.GetCustomerListResponse;
import app.user.model.UserAddressRepository;
import app.user.model.UserQueryRepository;
import app.user.model.UserRepository;
import app.user.model.entity.User;
import app.user.model.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

	private final UserRepository userRepository;
	private final UserQueryRepository userQueryRepository;
	private final UserAddressRepository userAddressRepository;



	@Transactional(readOnly = true)
	public PagedResponse<GetCustomerListResponse> getAllCustomer(Pageable pageable) {
		Page<GetCustomerListResponse> page = userRepository.findAllByUserRole(UserRole.CUSTOMER, pageable)
			.map(GetCustomerListResponse::from);

		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public GetCustomerDetailResponse getCustomerDetailById(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		List<GetCustomerAddressListResponse> addressList = userAddressRepository.findAllByUserUserId(userId)
			.stream().map(GetCustomerAddressListResponse::from).toList();

		return GetCustomerDetailResponse.from(user, addressList);
	}

	// @Transactional(readOnly = true)
	// public PagedResponse<OrderDetailResponse> getCustomerOrderListById(Long userId, Pageable pageable) {
	// 	User user = userRepository.findById(userId)
	// 		.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
	//
	// 	Page<Orders> ordersPage = ordersRepository.findAllByUserAndDeliveryAddressIsNotNull(user, pageable);
	//
	// 	Page<OrderDetailResponse> mapped = ordersPage.map(order -> {
	// 		List<OrderItem> orderItems = orderItemRepository.findByOrders(order);
	// 		return OrderDetailResponse.from(order, orderItems);
	// 	});
	//
	// 	return PagedResponse.from(mapped);
	// }

	@Transactional(readOnly = true)
	public PagedResponse<GetCustomerListResponse> searchCustomer(String keyWord, Pageable pageable) {
		Page<User> users = userQueryRepository.searchUser(keyWord, pageable);

		Page<GetCustomerListResponse> content = users.map(GetCustomerListResponse::from);

		return PagedResponse.from(content);
	}


}