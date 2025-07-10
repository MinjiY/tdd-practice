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
import static org.junit.jupiter.api.Assertions.*;
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



}