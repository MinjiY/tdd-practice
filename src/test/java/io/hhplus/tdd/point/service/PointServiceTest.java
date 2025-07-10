package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.hhplus.tdd.exception.IllegalArgumentException;


import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    PointService pointService;

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;


    @Test
    @DisplayName("유저의 ID를 입력받아 해당 ID의 유저에 맞는 포인트를 반환해야한다.")
    public void testGetUserPoint() {
        // given
        long userId = 1L;
        long expectedPoint = 100L;
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, expectedPoint, System.currentTimeMillis()));

        UserPoint actualUserPoint = pointService.getUserPoint(userId);

        // then
        assertThat(actualUserPoint.getId(), is(userId));
        assertThat(actualUserPoint.getPoint(), is(expectedPoint));
    }

    @Test
    @DisplayName("입력받는 유저의 ID는 음수일 때 IllegalArgumentException이 발생한다.")
    public void testGetUserPointWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.getUserPoint(-1L);
        });
    }

    @Test
    @DisplayName("입력받는 유저의 ID는 0일 때 IllegalArgumentException이 발생한다.")
    public void testGetUserPointWithZeroId() {
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.getUserPoint(0);
        });
    }

    @Test
    @DisplayName("포인트 충전/사용 내역을 조회할 때에는 포인트 이용내역이 없을때 에러가 아닌 빈리스트를 반환해야한다.")
    public void testGetUserPointHistoriesEmptyList() {
        // given
        long userId = 1L;

        //when
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of());

        List<PointHistory> actualHistories = pointService.getUserPointHistories(userId);

        // then
        assertNotNull(actualHistories);
        assertTrue(actualHistories.isEmpty());
    }

    @Test
    @DisplayName("유저의 ID를 입력받아 해당 ID에 맞는 포인트 충전/이용 내역을 반환해야한다.")
    public void testGetUserPointHistories() {
        // given
        long pointHistoryId = 1L;
        long userId = 1L;
        long amount = 100L;

        PointHistory expectedPointHistory = new PointHistory(pointHistoryId, userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of(expectedPointHistory));

        //when
        List<PointHistory> actualHistories = pointService.getUserPointHistories(userId);

        // then
        verify(pointHistoryTable).selectAllByUserId(userId);
        assertNotNull(actualHistories);
        assertThat(actualHistories.size(), is(1));
        assertThat(actualHistories.get(0).getId(), is(expectedPointHistory.getId()));
        assertThat(actualHistories.get(0).getUserId(), is(expectedPointHistory.getUserId()));
        assertThat(actualHistories.get(0).getAmount(), is(expectedPointHistory.getAmount()));
        assertThat(actualHistories.get(0).getType(), is(expectedPointHistory.getType()));
    }
}