package wooga.gradle.unity.tasks

import com.wooga.gradle.test.TaskIntegrationSpec
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll
import wooga.gradle.unity.UnityIntegrationSpec

abstract class ProjectManifestTaskSpec<T extends ProjectManifestTask>
    extends UnityIntegrationSpec
    implements TaskIntegrationSpec<T> {

    @Override
    String getSubjectUnderTestName() {
        return "${super.getSubjectUnderTestName()}Test"
    }

    @Unroll
    def "can set property #propertyName with #type"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        propertyName          | type                    | value
        "projectManifestFile" | File                    | TestValue.projectFile("foobar")
        "projectLockFile"     | File                    | TestValue.projectFile("foobar")

        setter = new PropertySetterWriter(subjectUnderTestName, propertyName)
            .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }
}
