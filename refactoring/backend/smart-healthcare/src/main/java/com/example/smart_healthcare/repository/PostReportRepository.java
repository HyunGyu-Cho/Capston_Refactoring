package com.example.smart_healthcare.repository;

// TODO: 신고 시스템은 향후 구현 예정
// import com.example.smart_healthcare.entity.PostReport;
// import com.example.smart_healthcare.entity.PostReport.ReportStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;
// import java.util.List;
/*
@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    // 논리삭제 제외한 조회 메서드들
    List<PostReport> findByPostIdAndIsDeletedFalse(Long postId);
    List<PostReport> findByReporterIdAndIsDeletedFalse(Long reporterId);
    List<PostReport> findByStatusAndIsDeletedFalse(ReportStatus status);
    List<PostReport> findByPostIdAndStatusAndIsDeletedFalse(Long postId, ReportStatus status);
    boolean existsByPostIdAndReporterIdAndIsDeletedFalse(Long postId, Long reporterId);
    
    // 게시글 삭제 시 관련 신고 논리삭제
    @Modifying
    @Query("UPDATE PostReport pr SET pr.isDeleted = true WHERE pr.post.id = :postId AND pr.isDeleted = false")
    void softDeleteByPostId(@Param("postId") Long postId);
    
    // 신고자 삭제 시 관련 신고 논리삭제
    @Modifying
    @Query("UPDATE PostReport pr SET pr.isDeleted = true WHERE pr.reporter.id = :reporterId AND pr.isDeleted = false")
    void softDeleteByReporterId(@Param("reporterId") Long reporterId);
}
*/
