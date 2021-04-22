package wooga.gradle.unity.traits

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

trait ArgumentsSpec {

    private final Provider<List<String>> arguments

    @Input
    Provider<List<String>> getArguments() {
        arguments
    }

    private final ListProperty<String> additionalArguments

    @Input
    ListProperty<String> getAdditionalArguments() {
        additionalArguments
    }

    void setAdditionalArguments(Iterable<String> value) {
        additionalArguments.set(value)
    }

    void setAdditionalArguments(String value) {
        additionalArguments.set([value])
    }

    void setAdditionalArguments(Provider<? extends Iterable<String>> value) {
        additionalArguments.set(value)
    }

    void argument(String value) {
        additionalArguments.add(value)
    }

    void arguments(Iterable<String> value) {
        additionalArguments.addAll(value)
    }

    void arguments(String... value) {
        arguments(value.toList())
    }

}
