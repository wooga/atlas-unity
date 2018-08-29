package wooga.gradle.unity.utils

import groovy.transform.InheritConstructors
import wooga.gradle.unity.utils.internal.UnityAssetFile

/**
 * A generic object which gives key/value access to unity .asset files.
 *
 * You can create an object with a {@code File} or {@code String} object. It implements
 * the {@code Map<String,Object>} interface for easy usage.
 *
 * <pre>
 * {@code
 *    def customUnityAsset = new GenericUnityAsset(new File("path/to/custom.asset"))
 *    def someValue = customUnityAsset['someKey']
 * }
 * </pre>
 */
@InheritConstructors
class GenericUnityAsset extends UnityAssetFile implements Map<String, Object> {

    @Override
    int size() {
        return getContent().size()
    }

    @Override
    boolean isEmpty() {
        return getContent().isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        return getContent().containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        return getContent().containsValue(value)
    }

    @Override
    Object get(Object key) {
        return getContent().get(key)
    }

    @Override
    Object put(String key, Object value) {
        return getContent().put(key, value)
    }

    @Override
    Object remove(Object key) {
        return getContent().remove(key)
    }

    @Override
    void putAll(Map<? extends String, ?> m) {
        getContent().putAll(m)
    }

    @Override
    void clear() {
        getContent().clear()
    }

    @Override
    Set<String> keySet() {
        return getContent().keySet()
    }

    @Override
    Collection<Object> values() {
        return getContent().values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        return getContent().entrySet()
    }
}
