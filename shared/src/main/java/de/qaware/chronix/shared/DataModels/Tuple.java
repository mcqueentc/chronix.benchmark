package de.qaware.chronix.shared.DataModels;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by f.lautenschlager on 05.05.2015.
 */

public final class Tuple<T, R, S, Q> {

    private final T first;
    private final R second;
    private final S third;
    private final Q fourth;

    public static <T, R, S, Q> Tuple<T, R, S, Q> of(T first, R second, S third, Q fourth) {
        return new Tuple<>(first, second, third, fourth);
    }

    private Tuple(T first, R second, S third, Q fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public T getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }

    public S getThird() {
        return third;
    }

    public Q getFourth() { return fourth; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?, ?, ?> pair = (Tuple<?, ?, ?, ?>) o;

        return new EqualsBuilder()
                .append(first, pair.first)
                .append(second, pair.second)
                .append(third, pair.third)
                .append(fourth, pair.fourth)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(first)
                .append(second)
                .append(third)
                .append(fourth)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                ", fourth=" + fourth +
                '}';
    }
}
