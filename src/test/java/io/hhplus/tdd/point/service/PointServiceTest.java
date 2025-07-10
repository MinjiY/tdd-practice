package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.hhplus.tdd.exception.IllegalArgumentException;


import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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


    @Test
    @DisplayName("chargeUserPoint 호출 시 pointHistoryTable.insert에 전달된 값이 올바른지 검증한다.")
    void testChargePointHistoryInsertArguments() {
        // given
        long userId = 1L;
        long initialAmount = 500L;
        long chargeAmount = 200L;
        TransactionType type = TransactionType.CHARGE;
        long fixedTime = 123456789L; // 고정된 시간 사용

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TransactionType> typeCaptor = ArgumentCaptor.forClass(TransactionType.class);
        ArgumentCaptor<Long> updateMillisCaptor = ArgumentCaptor.forClass(Long.class);

        UserPoint expectedUserPoint = new UserPoint(userId, initialAmount+chargeAmount, fixedTime);
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, initialAmount, fixedTime - 1000));
        when(userPointTable.insertOrUpdate(userId, initialAmount+chargeAmount)).thenReturn(expectedUserPoint);
        when(pointHistoryTable.insert(userId, chargeAmount, type, fixedTime))
                .thenReturn(new PointHistory(1L, userId, chargeAmount, type, fixedTime));

        // when
        pointService.chargeUserPoint(userId, chargeAmount);

        // then
        verify(pointHistoryTable).insert(
                userIdCaptor.capture(),
                amountCaptor.capture(),
                typeCaptor.capture(),
                updateMillisCaptor.capture()
        );

        assertThat(userIdCaptor.getValue(), is(userId));
        assertThat(amountCaptor.getValue(), is(chargeAmount));
        assertThat(typeCaptor.getValue(), is(type));
        assertThat(updateMillisCaptor.getValue(), is(fixedTime));
    }

    @Test
    @DisplayName("충전 기록보다 포인트 충전 여부가 먼저 결정되어야 한다.")
    void testChargeMethodCallOrder() {
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
        pointService.chargeUserPoint(userId, chargeAmount);

        // then
        InOrder inOrder = inOrder(userPointTable, pointHistoryTable);
        inOrder.verify(userPointTable).insertOrUpdate(userId, initialAmount+chargeAmount);
        inOrder.verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("유저의 ID와 사용할 포인트 금액을 입력받아 입력받은 값으로 포인트를 사용해야한다.")
    public void testUserPointUse() {
        // given
        long userId = 1L;
        long initialPoint = 1000L;
        long useAmount = 500L;

        UserPoint beforeUserPoint = new UserPoint(userId, initialPoint, System.currentTimeMillis());
        UserPoint afterUserPoint = new UserPoint(userId, initialPoint - useAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforeUserPoint);
        when(userPointTable.insertOrUpdate(userId, initialPoint - useAmount)).thenReturn(afterUserPoint);

        // when
        UserPoint actualUserPoint = pointService.useUserPoint(userId, useAmount);

        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, useAmount);
        assertThat(actualUserPoint.getId(), is(afterUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(afterUserPoint.getPoint()));
    }

    @Test
    @DisplayName("유저가 가지고 있는 포인트가 사용하려는 포인트보다 작을때 IllegalArgumentException이 발생한다.")
    public void testUserPointUseInvalid() {
        // given
        long userId = 1L;
        long initialPoint = 100L;
        long useAmount = 1000L;

        UserPoint beforeUserPoint = new UserPoint(userId, initialPoint, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforeUserPoint);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });
    }

    @Test
    @DisplayName("포인트를 사용한 후에는 포인트 내역에 사용 기록을 추가해야 한다.")
    public void testUserPointUseHistory() {
        // given
        long userId = 1L;
        long initialPoint = 1000L;
        long useAmount = 500L;
        long useTime = System.currentTimeMillis();

        UserPoint beforeUserPoint = new UserPoint(userId, initialPoint, useTime - 1000);
        UserPoint afterUserPoint = new UserPoint(userId, initialPoint - useAmount, useTime);

        PointHistory expectedPointHistory = new PointHistory(1L, userId, useAmount, TransactionType.USE, useTime);

        when(userPointTable.selectById(userId)).thenReturn(beforeUserPoint);
        when(userPointTable.insertOrUpdate(userId, initialPoint - useAmount)).thenReturn(afterUserPoint);
        when(pointHistoryTable.insert(userId, useAmount, TransactionType.USE, useTime))
                .thenReturn(expectedPointHistory);

        // when
        UserPoint actualUserPoint = pointService.useUserPoint(userId, useAmount);

        // then
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, initialPoint - useAmount);
        verify(pointHistoryTable).insert(userId, useAmount, TransactionType.USE, useTime);

        assertThat(actualUserPoint.getId(), is(afterUserPoint.getId()));
        assertThat(actualUserPoint.getPoint(), is(afterUserPoint.getPoint()));
    }

}