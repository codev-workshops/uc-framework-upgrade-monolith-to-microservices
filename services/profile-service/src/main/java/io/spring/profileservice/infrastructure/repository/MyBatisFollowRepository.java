package io.spring.profileservice.infrastructure.repository;

import io.spring.profileservice.core.follow.FollowRelation;
import io.spring.profileservice.core.follow.FollowRepository;
import io.spring.profileservice.infrastructure.mybatis.mapper.FollowMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisFollowRepository implements FollowRepository {
  private final FollowMapper followMapper;

  @Autowired
  public MyBatisFollowRepository(FollowMapper followMapper) {
    this.followMapper = followMapper;
  }

  @Override
  public void saveRelation(FollowRelation followRelation) {
    if (!findRelation(followRelation.getUserId(), followRelation.getTargetId()).isPresent()) {
      followMapper.saveRelation(followRelation);
    }
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    return Optional.ofNullable(followMapper.findRelation(userId, targetId));
  }

  @Override
  public void removeRelation(FollowRelation followRelation) {
    followMapper.deleteRelation(followRelation);
  }
}
