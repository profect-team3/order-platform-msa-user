package app.manager.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderDetailResponse {

	public OrderDetailResponse(List<Menu> menuList, Long totalPrice, String deliveryAddress, String paymentMethod, String orderChannel, String receiptMethod, String orderStatus, String requestMessage) {
		this.menuList = menuList;
		this.totalPrice = totalPrice;
		this.deliveryAddress = deliveryAddress;
		this.paymentMethod = paymentMethod;
		this.orderChannel = orderChannel;
		this.receiptMethod = receiptMethod;
		this.orderStatus = orderStatus;
		this.requestMessage = requestMessage;
	}

	private List<Menu> menuList;
	private Long totalPrice;
	private String deliveryAddress;
	private String paymentMethod;
	private String orderChannel;
	private String receiptMethod;
	private String orderStatus;
	private String requestMessage;

	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
	public static class Menu {
		private String menuName;
		private int quantity;
		private Long price;

		public Menu(String menuName, int quantity, Long price) {
			this.menuName = menuName;
			this.quantity = quantity;
			this.price = price;
		}

	}
}
