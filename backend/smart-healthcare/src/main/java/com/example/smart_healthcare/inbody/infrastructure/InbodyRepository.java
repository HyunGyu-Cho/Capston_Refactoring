package com.example.smart_healthcare.inbody.infrastructure;

import com.example.smart_healthcare.inbody.domain.Inbody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InbodyRepository extends JpaRepository<Inbody, Long> {
    Optional<Inbody> findByIdAndMember_Id(Long id, Long memberId);

    Page<Inbody> findByMember_IdOrderByMeasuredAtDescIdDesc(Long memberId, Pageable pageable);
}
