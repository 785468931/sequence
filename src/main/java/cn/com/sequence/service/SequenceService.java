package cn.com.sequence.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import cn.com.sequence.dao.SequenceLog;
import cn.com.sequence.entity.SequenceLogDao;

@Service
public class SequenceService {
	@Autowired
	LockSquence lockSquence;
	final int maxThreadCnt = 50;
	int expand = 200000; //id段位
	Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private SequenceLogDao sequenceLogDao;
	ExecutorService pool = Executors.newFixedThreadPool(maxThreadCnt);
	
	
	public Long getSequenceId(String ip) {
		synchronized (lockSquence) {
			long id = lockSquence.getAtomic().incrementAndGet();
			Long maxId = lockSquence.getCurrentMaxId().get(ip) ;
			if (maxId != null && id < maxId) {
				log.info("生成的id：{}", id);
				saveIdLog( ip, id);
				return id;
			}
		}
		
		Long getId = updateSequenceId(ip);
		Long currentId = getId * expand;
		synchronized (lockSquence) {
			lockSquence.getAtomic().set(currentId);
			lockSquence.getCurrentMaxId().put(ip, (getId + 1) * expand);
			saveIdLog( ip, currentId);
		}
		
		log.info("生成的id：{}", currentId);
		return currentId;
	}
	
	//异步记录日志
	public void saveIdLog(String ip,Long id) {
		pool.execute(new Runnable() {
				@Override
				public void run() {
					SequenceLog sequenceLog = new SequenceLog();
					sequenceLog.setSequenceId(id);
					sequenceLog.setIp(ip);
					sequenceLog.setCreateTime(new Date());
					sequenceLogDao.save(sequenceLog);
				}
			});
		
	}
	
	
// 废弃，存在并发问题
//	AtomicLong atomic = new AtomicLong();
//	private static ConcurrentMap<String, Long> currentMaxId = new ConcurrentHashMap<>();
//	public Long getSequenceId(String ip) {
//		long id = atomic.incrementAndGet();
//		Long maxId = currentMaxId.get(ip);
//		synchronized (maxId) {
//			if (maxId != null && id < maxId) {
//				log.info("生成的id：{}", id);
//				return id;
//			}
//		}
//		
//		Long getId = updateSequenceId(ip);
//		Long currentId = getId * expand;
//		atomic.set(currentId);
//		currentMaxId.put(ip, (getId + 1) * expand);
//		log.info("生成的id：{}", id);
//		return currentId;
//	}

	public long updateSequenceId(final String ip) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				final PreparedStatement ps = connection.prepareStatement(
						"INSERT INTO sequence_generator_table (ip) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, ip);
				return ps;
			}
		}, keyHolder);
		Long id = keyHolder.getKey().longValue();
		jdbcTemplate.update("delete from sequence_generator_table where ip=? and id<? " , new Object[] { ip,id },new int[] { Types.VARCHAR,  Types.BIGINT });
		return id;
	}
}
