package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @InjectMocks
    PointService pointService;

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName("유저 포인트 조회 테스트")
    public void testGetUserPoint() {
        // given
        long userId = 1L;

        // when
        UserPoint actualPoint = pointService.getUserPoint(userId);

        // then
        assertThat(actualPoint.getId(), is(userId));
    }

    @Test
    @DisplayName("유저 포인트 조회 테스트 - when을 통한 동작 검증")
    public void testUserPointTable() {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expectedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

        UserPoint actualPoint = pointService.getUserPoint(userId);

        // then
        assertThat(actualPoint.getId(), is(expectedPoint.getId()));
        assertThat(actualPoint.getPoint(), is(expectedPoint.getPoint()));
    }





}