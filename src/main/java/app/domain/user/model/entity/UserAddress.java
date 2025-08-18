package app.domain.user.model.entity;

import java.util.UUID;

import app.domain.customer.dto.request.UpdateCustomerAddressRequest;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "p_user_address")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLDelete(sql = "UPDATE p_user_address SET deleted_at = NOW() WHERE address_id = ?")
@Where(clause = "deleted_at IS NULL")
@Builder
public class UserAddress extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID addressId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(length = 50, nullable = false)
	private String alias;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String addressDetail;

	@Column(nullable = false)
	private boolean isDefault = false;

	public UserAddress update(UpdateCustomerAddressRequest request) {
		if (request.getAlias() != null) {
			this.alias = request.getAlias();
		}
		if (request.getAddress() != null) {
			this.address = request.getAddress();
		}
		if (request.getAddressDetail() != null) {
			this.addressDetail = request.getAddressDetail();
		}
		if (request.getIsDefault() != null) {
			this.isDefault = request.getIsDefault();
		}
		return this;
	}

	public void unsetAsDefault() {
		this.isDefault = false;
	}
}