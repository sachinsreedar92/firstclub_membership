package com.firstclub.membership.subscription.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.firstclub.membership.common.exception.ConflictException;
import org.junit.jupiter.api.Test;

class SubscriptionStateMachineTest {

    private final SubscriptionStateMachine stateMachine = new SubscriptionStateMachine();

    @Test
    void activeAllowsTierChangeAndCancel() {
        assertThat(stateMachine.canApply(SubscriptionStatus.ACTIVE, SubscriptionAction.UPGRADE)).isTrue();
        assertThat(stateMachine.canApply(SubscriptionStatus.ACTIVE, SubscriptionAction.CANCEL)).isTrue();
    }

    @Test
    void cancelledIsTerminal() {
        assertThat(stateMachine.canApply(SubscriptionStatus.CANCELLED, SubscriptionAction.CANCEL))
                .isFalse();
        assertThatThrownBy(() ->
                stateMachine.assertCanApply(SubscriptionStatus.CANCELLED, SubscriptionAction.UPGRADE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void expiredIsTerminal() {
        assertThat(stateMachine.canApply(SubscriptionStatus.EXPIRED, SubscriptionAction.DOWNGRADE))
                .isFalse();
    }
}
