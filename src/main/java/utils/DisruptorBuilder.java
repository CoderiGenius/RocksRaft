package utils;

import com.alipay.remoting.NamedThreadFactory;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * Created by 周思成 on  2020/4/9 21:11
 */

public class DisruptorBuilder<T> {
    private EventFactory<T> eventFactory;
    private Integer ringBufferSize;
    private ThreadFactory threadFactory = new NamedThreadFactory("Disruptor",true);
    private ProducerType producerType  = ProducerType.MULTI;
    private WaitStrategy waitStrategy  = new BlockingWaitStrategy();

    private DisruptorBuilder() {
    }

    public static <T> DisruptorBuilder<T> newInstance() {
        return new DisruptorBuilder<>();
    }

    public EventFactory<T> getEventFactory() {
        return this.eventFactory;
    }

    public DisruptorBuilder<T> setEventFactory(final EventFactory<T> eventFactory) {
        this.eventFactory = eventFactory;
        return this;
    }

    public int getRingBufferSize() {
        return this.ringBufferSize;
    }

    public DisruptorBuilder<T> setRingBufferSize(final int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        return this;
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public DisruptorBuilder<T> setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ProducerType getProducerType() {
        return this.producerType;
    }

    public DisruptorBuilder<T> setProducerType(final ProducerType producerType) {
        this.producerType = producerType;
        return this;
    }

    public WaitStrategy getWaitStrategy() {
        return this.waitStrategy;
    }

    public DisruptorBuilder<T> setWaitStrategy(final WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
        return this;
    }

    public Disruptor<T> build() {
        Requires.requireNonNull(this.ringBufferSize, " Ring buffer size not set");
        Requires.requireNonNull(this.eventFactory, "Event factory not set");
        return new Disruptor<>(this.eventFactory, this.ringBufferSize, this.threadFactory, this.producerType,
                this.waitStrategy);
    }


}
