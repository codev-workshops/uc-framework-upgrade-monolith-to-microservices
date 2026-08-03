package io.spring.infrastructure.repository;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.FollowRepository;
import io.spring.infrastructure.mybatis.mapper.FollowMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisFollowRepository implements FollowRepository {
  private final FollowMapper followMapper;

  @Autowired
  public MyBatisFollowRepository(FollowMapper followMapper) {
    this.followMapper = followMapper;
  }

  @Override
  @Transactional
  public void saveRelation(FollowRelation followRelation) {
    if (followMapper.findRelation(followRelation.getUserId(), followRelation.getTargetId())
        == null) {
      followMapper.saveRelation(followRelation);
    }
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    return Optional.ofNullable(followMapper.findRelation(userId, targetId));
  }

  @Override
  @Transactional
  public void removeRelation(FollowRelation followRelation) {
    followMapper.deleteRelation(followRelation);
  }
}
