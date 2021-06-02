package wooga.gradle.unity


import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

enum UnityPathResolution {
    /**
     * No Unity path will be set
     */
    None,
    /**
     * A mock Unity executable will be set (a batch file that echos back)
     */
    Mock,
    /**
     * Returns an installed Unity version (if none are present, will install one locally)
     */
    Default
}

enum UnityPluginOverrides {
    None,
    AddTestBuildTargets,
    DisableAutoActivateAndLicense,
    AddMockLicense,


    static final EnumSet<UnityPluginOverrides> all = EnumSet.allOf(UnityPluginOverrides.class)
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@ExtensionAnnotation(UnityPluginTestExtension.class)
@interface UnityPluginTestOptions {

    boolean applyPlugin() default true

    UnityPathResolution unityPath() default UnityPathResolution.Mock

    UnityPluginOverrides pluginOverrides() default UnityPluginOverrides.None

    boolean addPluginTestDefaults() default true

    boolean disableAutoActivateAndLicense() default true

    boolean addMockTask() default true

    boolean forceMockTaskRun() default true

    boolean clearMockTaskActions() default false
}

class DefaultUnityPluginTestOptions implements UnityPluginTestOptions {
    Boolean applyPlugin = true
    UnityPathResolution unityPath = UnityPathResolution.Mock
    Boolean addPluginTestDefaults = true
    Boolean disableAutoActivateAndLicense = true

    Boolean addMockTask = true
    Boolean forceMockTaskRun = true
    Boolean clearMockTaskActions = false

    boolean applyPlugin() {
        applyPlugin
    }

    UnityPathResolution unityPath() {
        unityPath
    }

    @Override
    UnityPluginOverrides pluginOverrides() {
        UnityPluginOverrides.None
    }

    boolean addPluginTestDefaults() {
        addPluginTestDefaults
    }

    @Override
    boolean disableAutoActivateAndLicense() {
        disableAutoActivateAndLicense
    }

    boolean addMockTask() {
        addMockTask
    }

    boolean forceMockTaskRun() {
        forceMockTaskRun
    }

    boolean clearMockTaskActions() {
        clearMockTaskActions
    }

    @Override
    Class<? extends Annotation> annotationType() {
        return null
    }
}

class UnityPluginTestExtension implements IAnnotationDrivenExtension<UnityPluginTestOptions> {

    @Override
    void visitSpecAnnotation(UnityPluginTestOptions annotation, SpecInfo spec) {
        spec.addSetupInterceptor({ invocation ->
            def unitySpec = invocation.instance as UnityIntegrationTest
            if (unitySpec) {
                unitySpec.options = annotation
            }
            invocation.proceed()
        })
    }

    @Override
    void visitFeatureAnnotation(UnityPluginTestOptions annotation, FeatureInfo feature) {
        feature.spec.addSetupInterceptor({ invocation ->
            def unitySpec = invocation.instance as UnityIntegrationTest
            if (invocation.feature == feature) {
                if (unitySpec) {
                    unitySpec.options = annotation
                }
            }
            invocation.proceed()
        })
    }

    @Override
    void visitFixtureAnnotation(UnityPluginTestOptions annotation, MethodInfo fixtureMethod) {
    }

    @Override
    void visitFieldAnnotation(UnityPluginTestOptions annotation, FieldInfo field) {

    }

    @Override
    void visitSpec(SpecInfo spec) {
    }
}
