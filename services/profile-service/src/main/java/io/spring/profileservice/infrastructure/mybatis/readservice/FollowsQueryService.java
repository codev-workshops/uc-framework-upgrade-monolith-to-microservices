package io.spring.profileservice.infrastructure.mybatis.readservice;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Read model over the `follows` table (extracted from UserRelationshipQueryService). */
@Mapper
public interface FollowsQueryService {
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  List<String> followedUsers(@Param("userId") String userId);
}
