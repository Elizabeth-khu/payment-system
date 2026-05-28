package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    long countByUserIdAndActiveTrue(String userId);

    long countByUserId(String userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = false, updated_at = CURRENT_TIMESTAMP WHERE id = :cardId", nativeQuery = true)
    void deactivateCardByIdNative(@Param("cardId") Long cardId);
}