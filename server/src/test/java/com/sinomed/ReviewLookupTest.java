package com.sinomed;

import com.sinomed.entity.ReviewEntity;
import com.sinomed.repository.ReviewRepository;
import com.sinomed.service.ReviewService;
import com.sinomed.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 每日复盘总结按 id 查询/删除（ReviewService.findViewById / deleteViewById）回归：
 * 原 GET/DELETE /api/v1/review/{id} 为返回空对象的占位——锁死「查得到、删得掉、未知 id 报 400」。
 * 库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/review-lookup-test-${random.uuid}.db"
})
class ReviewLookupTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    /** reviews.day 唯一：用例间错开日期 */
    private static final java.util.concurrent.atomic.AtomicInteger DAY_SEQ = new java.util.concurrent.atomic.AtomicInteger();

    private Long newReview(String good) {
        ReviewEntity review = new ReviewEntity();
        review.setDay(DateUtil.getZeroTime(new Date(System.currentTimeMillis() + DAY_SEQ.getAndIncrement() * 86_400_000L)));
        review.setGood(good);
        review.setImprovement("改进");
        return reviewService.saveView(review).getId();
    }

    @Test
    void findByIdReturnsSavedReview() {
        Long id = newReview("今日亮点");
        ReviewEntity found = reviewService.findViewById(id).orElseThrow();
        assertEquals("今日亮点", found.getGood());
        assertTrue(reviewService.findViewById(99999L).isEmpty());
    }

    @Test
    void deleteRemovesAndRejectsUnknown() {
        Long id = newReview("待删除");
        reviewService.deleteViewById(id);
        assertTrue(reviewRepository.findById(id).isEmpty());

        var e = assertThrows(IllegalArgumentException.class, () -> reviewService.deleteViewById(99999L));
        assertEquals("复盘总结不存在：99999", e.getMessage());
    }
}
