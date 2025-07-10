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
import static org.hamcrest.Matchers.array;
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

    @Test
    @DisplayName("유저의 ID와 충전할 포인트 금액을 입력받아 입력받은 값으로 포인트를 충전해야한다.")
    public void testUserPointCharge() {
        // given
        long userId = 1L;
        long amount = 100L;

        UserPoint expectedUserPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedUserPoint);


        // when
        UserPoint actualUserPoint = pointService.chargeUserPoint(userId, amount);

        // then
        verify(userPointTable).insertOrUpdate(expectedUserPoint.getId(), expectedUserPoint.getPoint());
        assertThat(actualUserPoint.getId(), is(expectedUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedUserPoint.getPoint()));

    }

    @Test
    @DisplayName("충전하는 포인트는 0보다 큰 정수가 아니라면 IllegalArgumentException이 발생한다.")
    public void testUserPointChargeInvalid() {
        // given
        long userId = 1L;
        long amount = 0L;

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, amount);
        });

    }

    @Test
    @DisplayName("충전하는 포인트 금액은 유저가 갖고있는 현재 포인트에 더해져야 한다.")
    public void testUserPointChargeAdd() {
        // given
        long userId = 1L;
        long initialAmount = 100L;
        long chargeAmount = 50L;

        UserPoint expectedAmount = new UserPoint(userId, initialAmount + chargeAmount, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, initialAmount, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, initialAmount + chargeAmount)).thenReturn(expectedAmount);

        // when
        UserPoint actualUserPoint = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, initialAmount + chargeAmount);
        assertThat(actualUserPoint.getId(), is(expectedAmount.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedAmount.getPoint()));
        assertThat(actualUserPoint.getPoint(), is(expectedAmount.getPoint()));
    }


    @Test
    @DisplayName("충전 후 포인트 내역에 충전한 금액과 현재 시간을 기록해야 한다.")
    public void testUserPointChargeWithHistory() {
        // given
        long userId = 1L;
        long initialAmount = 100L;
        long chargeAmount = 50L;
        long chargeTime = System.currentTimeMillis();

        UserPoint expectedUserPoint = new UserPoint(userId, initialAmount + chargeAmount, chargeTime);
        PointHistory expectedPointHistory = new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, chargeTime);

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, initialAmount, chargeTime-1000));
        when(userPointTable.insertOrUpdate(userId, initialAmount + chargeAmount)).thenReturn(expectedUserPoint);
        when(pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE, chargeTime))
                .thenReturn(expectedPointHistory);

        // when
        UserPoint actualUserPoint = pointService.chargeUserPoint(userId, chargeAmount);


        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, initialAmount + chargeAmount);
        verify(pointHistoryTable).insert(userId, chargeAmount, TransactionType.CHARGE, chargeTime);

        assertThat(actualUserPoint.getId(), is(expectedUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(expectedUserPoint.getPoint()));
    }
}