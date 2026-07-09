package io.spring.gateway.graphql.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsTypeResolver;
import io.spring.gateway.graphql.types.Error;
import io.spring.gateway.graphql.types.UserPayload;

/** Resolves the concrete type of the {@code UserResult} union returned by createUser. */
@DgsComponent
public class UserResultTypeResolver {

  @DgsTypeResolver(name = "UserResult")
  public String resolveUserResult(Object result) {
    if (result instanceof UserPayload) {
      return "UserPayload";
    }
    if (result instanceof Error) {
      return "Error";
    }
    throw new IllegalArgumentException("Unknown UserResult type: " + result);
  }
}
