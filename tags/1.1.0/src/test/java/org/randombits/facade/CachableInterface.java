package org.randombits.facade;

/**
 * This is an interface is marked as {@link Facadable} and {@link Cachable}. However,
 * because caching is not actually inherited from interfaces or super-classes,
 * the cachable setting is ignored.
 */
@Facadable
@Cachable
public interface CachableInterface {
}
