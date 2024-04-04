package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import java.time.Duration
import java.util.regex.Pattern

trait RetrySpec implements BaseSpec {

    private final Property<Integer> maxRetries = objects.property(Integer)

    @Input
    Property<Integer> getMaxRetries() {
        return maxRetries
    }

    void setMaxRetries(Integer maxRetries) {
        this.maxRetries.set(maxRetries)
    }

    void setMaxRetries(Provider<Integer> maxRetries) {
        this.maxRetries.set(maxRetries)
    }

    private final Property<Duration> retryWait = objects.property(Duration)

    @Input
    @Optional
    Property<Duration> getRetryWait() {
        return retryWait
    }

    void setRetryWait(long retryWaitMs) {
        this.retryWait.set(Duration.ofMillis(retryWaitMs))
    }

    void setRetryWait(Duration retryWait) {
        this.retryWait.set(retryWait)
    }

    void setRetryWait(Provider<Duration> retryWait) {
        this.retryWait.set(retryWait)
    }

    private final ListProperty<Pattern> retryRegexes = objects.listProperty(Pattern)

    @Input
    @Optional
    ListProperty<Pattern> getRetryRegexes() {
        return retryRegexes
    }

    /**
     * @param retryRegexes - Elements of this collection can be either String or Pattern. Strings will be converted to Pattern on set.
     */
    void setRetryRegexes(Collection<?> retryRegexes) {
        this.retryRegexes.set(retryRegexes.collect {regex ->
            if (regex instanceof String) {
                return Pattern.compile(regex)
            } else if (regex instanceof Pattern){
                return regex
            } else {
                throw new IllegalStateException("retryRegex property should be a java.util.regex.Pattern or a String object")
            }
        })
    }

    void setRetryRegexes(Provider<? extends Iterable<?>> retryRegexes) {
        this.retryRegexes.set(retryRegexes.map { regexLst ->
        regexLst.collect {regex ->
            if (regex instanceof String) {
                return Pattern.compile(regex)
            } else if (regex instanceof Pattern){
                return regex
            } else {
                throw new IllegalStateException("retryRegex property should be a java.util.regex.Pattern or a String object")
            }
        }})
    }

    void setRetryRegex(Pattern retryRegex) {
        retryRegexes.set([retryRegex])
    }

    void setRetryRegex(String retryRegex) {
        retryRegexes.set([Pattern.compile(retryRegex)])
    }

    void setRetryRegex(Provider<?> retryRegex) {
        this.retryRegex.set(retryRegex.map {
            if (it instanceof String) {
                return Pattern.compile(it)
            } else if (it instanceof Pattern){
                return it
            } else {
                throw new IllegalStateException("retryRegex property should be a java.util.regex.Pattern or a String object")
            }
        })
    }
}
