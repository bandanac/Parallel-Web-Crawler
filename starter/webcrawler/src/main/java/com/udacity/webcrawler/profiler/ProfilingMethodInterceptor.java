package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final ZonedDateTime zonedDateTime;
  private final ProfilingState profilingState;
  private final Object targetObject;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(
          Clock clock,
          ZonedDateTime zonedDateTime,
          ProfilingState profilingState,
          Object targetObject) {
    this.clock = Objects.requireNonNull(clock);
    this.zonedDateTime = Objects.requireNonNull(zonedDateTime);
    this.profilingState = Objects.requireNonNull(profilingState);
    this.targetObject = Objects.requireNonNull(targetObject);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    Object invoked;
    Instant start = null;
    Boolean profiled = method.getAnnotation(Profiled.class) != null;

    if (profiled) {
      start = clock.instant();
    }

    try {
      invoked = method.invoke(targetObject, args);
    } catch (InvocationTargetException invocationTargetException) {
      throw invocationTargetException.getTargetException();
    } catch (IllegalAccessException illegalAccessException) {
      throw new RuntimeException(illegalAccessException);
    } finally {
      if (profiled) {
        Duration duration = Duration.between(start, clock.instant());
        profilingState.record(targetObject.getClass(), method, duration);
      }
    }

    return invoked;
  }
}
