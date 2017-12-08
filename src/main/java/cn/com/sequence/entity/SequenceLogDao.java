package cn.com.sequence.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.com.sequence.dao.SequenceLog;

public interface SequenceLogDao extends JpaRepository<SequenceLog, Long> {
	
	
}
