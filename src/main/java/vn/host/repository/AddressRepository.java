package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Address;
import vn.host.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
    @Query("""
            SELECT a
            FROM Address a
            WHERE a.province = :province
              AND a.district = :district
              AND a.ward = :ward
              AND a.addressDetail = :addressDetail
              AND a.receiverName = :receiverName
              AND a.phone = :phone
              AND a.user = :user
            """)
    Optional<Address> findAddresses(String province, String district, String ward, String addressDetail, String receiverName, String phone, User user);

    @Query("""
            SELECT a
            FROM Address a
            WHERE a.user.userId = :userId AND a.isDefault = 1
            """)
    Optional<Address> findDefaultAddressByUserId(Long userId);
}
